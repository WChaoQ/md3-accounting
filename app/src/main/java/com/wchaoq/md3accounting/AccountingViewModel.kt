package com.wchaoq.md3accounting

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wchaoq.md3accounting.data.Expense
import com.wchaoq.md3accounting.data.ExpenseRepository
import com.wchaoq.md3accounting.data.ThemePrefs
import com.wchaoq.md3accounting.data.ThemeStore
import com.wchaoq.md3accounting.util.dayOfMonth
import com.wchaoq.md3accounting.util.endDateInt
import com.wchaoq.md3accounting.util.startDateInt
import com.wchaoq.md3accounting.util.toDateInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

data class SnackEvent(
    val message: String,
    val actionLabel: String? = null,
)

@Immutable
data class HomeUiState(
    val yearMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val monthTotal: Long = 0L,
    val dayTotals: List<Long> = emptyList(),
    val dayItems: List<Expense> = emptyList(),
    val dayTotal: Long = 0L
)

@Immutable
data class StatsUiState(
    val yearMonth: YearMonth = YearMonth.now(),
    val statsYear: Int = LocalDate.now().year,
    val selectedDayIndex: Int = -1,
    val monthTotal: Long = 0L,
    val dayVals: List<Long> = emptyList(),
    val dayItems: List<Expense> = emptyList(),
    val yearTotal: Long = 0L,
    val monthVals: List<Long> = List(12) { 0L },
    val highlightMonthIndex: Int = -1
)

data class ThemeUiState(
    val prefs: ThemePrefs = ThemePrefs(),
    val ready: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class AccountingViewModel(
    private val repo: ExpenseRepository,
    private val themeStore: ThemeStore
) : ViewModel() {

    private val yearMonth = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow(LocalDate.now())
    // Statistics navigation is deliberately isolated from the accounting calendar.
    private data class StatsNavState(
        val yearMonth: YearMonth = YearMonth.now(),
        val selectedDate: LocalDate = LocalDate.now(),
        val year: Int = LocalDate.now().year
    )
    private val statsNavState = MutableStateFlow(StatsNavState())
    private val statsMonth = statsNavState.map { it.yearMonth }
    private val statsSelectedDate = statsNavState.map { it.selectedDate }
    private val statsYear = statsNavState.map { it.year }

    private val snackChannel = Channel<SnackEvent>(Channel.BUFFERED)
    val snackEvents = snackChannel.receiveAsFlow()

    val themeState: StateFlow<ThemeUiState> = themeStore.theme
        .map { ThemeUiState(prefs = it, ready = true) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeUiState(ready = false))

    private val monthRange = yearMonth.map { it.startDateInt() to it.endDateInt() }

    private val daySumsFlow = monthRange.flatMapLatest { (s, e) ->
        repo.observeDaySums(s, e)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val dayItemsFlow = selectedDate.flatMapLatest { d ->
        repo.observeByDate(d.toDateInt())
    }

    val homeState: StateFlow<HomeUiState> = combine(
        yearMonth,
        selectedDate,
        daySumsFlow,
        dayItemsFlow
    ) { ym, sel, daySums, dayItems ->
        val days = ym.lengthOfMonth()
        val arr = MutableList(days) { 0L }
        daySums.forEach { sum ->
            val day = sum.date.dayOfMonth()
            if (day in 1..days) arr[day - 1] = sum.total
        }
        HomeUiState(
            yearMonth = ym,
            selectedDate = sel,
            monthTotal = daySums.sumOf { it.total },
            dayTotals = arr.toList(),
            dayItems = dayItems,
            dayTotal = dayItems.sumOf { it.amount }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private val yearRange = statsYear.map { y ->
        (y * 10_000 + 101) to (y * 10_000 + 1231)
    }

    private val monthSumsFlow = yearRange.flatMapLatest { (s, e) ->
        repo.observeMonthSums(s, e)
    }

    private val statsMonthRange = statsMonth.map { it.startDateInt() to it.endDateInt() }
    private val statsDaySumsFlow = statsMonthRange.flatMapLatest { (s, e) ->
        repo.observeDaySums(s, e)
    }
    private val statsDayItemsFlow = statsSelectedDate.flatMapLatest { d ->
        repo.observeByDate(d.toDateInt())
    }

    private data class StatsNav(val ym: YearMonth, val sy: Int, val sel: LocalDate)
    private data class StatsMonthData(val daySums: List<com.wchaoq.md3accounting.data.local.DaySum>)
    private data class StatsYearData(val monthSums: List<com.wchaoq.md3accounting.data.local.MonthSum>)

    private val statsNav = statsNavState.map { StatsNav(it.yearMonth, it.year, it.selectedDate) }
    private val statsMonthData = statsDaySumsFlow.map { sums -> StatsMonthData(sums) }
    private val statsYearData = monthSumsFlow.map { sums -> StatsYearData(sums) }

    val statsState: StateFlow<StatsUiState> = combine(
        statsNav, statsMonthData, statsYearData, statsDayItemsFlow
    ) { nav, monthData, yearData, dayItems ->
        val days = nav.ym.lengthOfMonth()
        val dayVals = MutableList(days) { 0L }
        monthData.daySums.forEach { sum ->
            val day = sum.date.dayOfMonth()
            if (day in 1..days) dayVals[day - 1] = sum.total
        }
        val monthVals = MutableList(12) { 0L }
        yearData.monthSums.forEach { ms ->
            val m = ms.yearMonth % 100
            if (m in 1..12) monthVals[m - 1] = ms.total
        }
        val selIdx = if (nav.sel.year == nav.ym.year && nav.sel.month == nav.ym.month)
            nav.sel.dayOfMonth - 1 else -1
        val hlMonth = if (nav.ym.year == nav.sy) nav.ym.monthValue - 1 else -1

        StatsUiState(
            yearMonth = nav.ym,
            statsYear = nav.sy,
            selectedDayIndex = selIdx,
            monthTotal = monthData.daySums.sumOf { it.total },
            dayVals = dayVals.toList(),
            dayItems = dayItems,
            yearTotal = yearData.monthSums.sumOf { it.total },
            monthVals = monthVals.toList(),
            highlightMonthIndex = hlMonth
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    init {
        viewModelScope.launch {
            repo.migrateFromLegacyIfNeeded()
        }
    }

    fun changeMonth(delta: Int) {
        val ym = yearMonth.value.plusMonths(delta.toLong())
        yearMonth.value = ym
        val d = selectedDate.value
        selectedDate.value = LocalDate.of(ym.year, ym.month, minOf(d.dayOfMonth, ym.lengthOfMonth()))
    }

    fun goToday() {
        val n = LocalDate.now()
        yearMonth.value = YearMonth.from(n)
        selectedDate.value = n
    }

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
        yearMonth.value = YearMonth.from(date)
    }

    fun changeStatsMonth(delta: Int) {
        val current = statsNavState.value
        val next = current.yearMonth.plusMonths(delta.toLong())
        statsNavState.value = current.copy(
            yearMonth = next,
            selectedDate = LocalDate.of(next.year, next.month, 1)
        )
    }

    fun stepYear(delta: Int) {
        val current = statsNavState.value
        statsNavState.value = current.copy(year = current.year + delta)
    }

    fun openStatsMonth(monthIndex0: Int) {
        if (monthIndex0 !in 0..11) return
        val current = statsNavState.value
        val next = YearMonth.of(current.year, monthIndex0 + 1)
        statsNavState.value = current.copy(
            yearMonth = next,
            selectedDate = LocalDate.of(next.year, next.month, 1)
        )
    }

    fun pickStatsDay(dayIndex0: Int) {
        val current = statsNavState.value
        val ym = current.yearMonth
        if (dayIndex0 in 0 until ym.lengthOfMonth()) {
            statsNavState.value = current.copy(
                selectedDate = LocalDate.of(ym.year, ym.month, dayIndex0 + 1)
            )
        }
    }

    fun saveExpense(existing: Expense?, title: String, amountCents: Long) {
        if (title.isBlank() || amountCents <= 0L) return
        val expense = existing?.copy(
            title = title.trim(),
            amount = amountCents
        ) ?: Expense(
            id = UUID.randomUUID().toString(),
            date = selectedDate.value.toDateInt(),
            title = title.trim(),
            amount = amountCents
        )
        viewModelScope.launch {
            val ok = if (existing == null) repo.add(expense) else repo.update(expense)
            if (!ok) {
                snackChannel.send(
                    SnackEvent(if (existing == null) "添加失败，请重试" else "修改失败，请重试")
                )
            }
        }
    }

    fun exportCsv(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val expenses = withContext(Dispatchers.IO) { repo.allExpenses() }
            val csv = withContext(Dispatchers.Default) {
                buildString {
                    appendLine("date,title,amount")
                    expenses.forEach { expense ->
                        val date = "${expense.date / 10_000}-${((expense.date / 100) % 100).toString().padStart(2, '0')}-${(expense.date % 100).toString().padStart(2, '0')}"
                        val whole = expense.amount / 100L
                        val fraction = (expense.amount % 100L).toString().padStart(2, '0')
                        append(date).append(',')
                        append(csvCell(expense.title)).append(',')
                        append(whole).append('.').append(fraction).appendLine()
                    }
                }
            }
            onReady(csv)
        }
    }

    fun importCsv(raw: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val lines = raw.removePrefix("\uFEFF").lineSequence().filter { it.isNotBlank() }.toList()
            if (lines.size < 2) {
                snackChannel.send(SnackEvent("CSV 文件没有可导入的记录"))
                return@launch
            }
            var imported = 0
            lines.drop(1).forEach { line ->
                val cells = parseCsvLine(line)
                if (cells.size >= 3) {
                    val date = runCatching {
                        val parts = cells[0].trim().split('-')
                        (parts[0].toInt() * 10_000) + (parts[1].toInt() * 100) + parts[2].toInt()
                    }.getOrNull()
                    val title = cells[1].trim()
                    val amount = cells[2].trim().replace(",", "").toBigDecimalOrNull()
                        ?.movePointRight(2)?.setScale(0, java.math.RoundingMode.HALF_UP)?.longValueExact()
                    if (date != null && title.isNotBlank() && amount != null && amount > 0L) {
                        val expense = Expense(UUID.randomUUID().toString(), date, title, amount)
                        if (repo.add(expense)) imported++
                    }
                }
            }
            snackChannel.send(SnackEvent("已导入 $imported 条记录"))
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val cells = mutableListOf<String>()
        val cell = StringBuilder()
        var quoted = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' && quoted && i + 1 < line.length && line[i + 1] == '"' -> {
                    cell.append('"')
                    i++
                }
                ch == '"' -> quoted = !quoted
                ch == ',' && !quoted -> {
                    cells += cell.toString()
                    cell.clear()
                }
                else -> cell.append(ch)
            }
            i++
        }
        cells += cell.toString()
        return cells
    }

    private fun csvCell(value: String): String {
        if (value.none { it == ',' || it == '"' || it == '\n' || it == '\r' }) return value
        return "\"" + value.replace("\"", "\"\"") + "\""
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch { repo.delete(id) }
    }

    fun deleteAllExpenses() {
        viewModelScope.launch(Dispatchers.IO) {
            val deleted = repo.deleteAll()
            snackChannel.send(SnackEvent(if (deleted > 0) "已删除全部 $deleted 条记录" else "没有可删除的记录"))
        }
    }

    fun setHue(hue: Int) {
        viewModelScope.launch { themeStore.setHue(hue) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { themeStore.setDynamicColor(enabled) }
    }

    fun toggleDark(systemDark: Boolean) {
        viewModelScope.launch {
            val effective = themeState.value.prefs.dark ?: systemDark
            themeStore.setDark(!effective)
        }
    }

    class Factory(
        private val repo: ExpenseRepository,
        private val themeStore: ThemeStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AccountingViewModel(repo, themeStore) as T
    }
}
