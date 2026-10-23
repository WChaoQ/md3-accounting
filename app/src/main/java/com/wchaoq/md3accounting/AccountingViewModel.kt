package com.wchaoq.md3accounting

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
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

data class SnackEvent(
    val message: String,
    val actionLabel: String? = null,
    val isUndoDelete: Boolean = false
)

data class HomeUiState(
    val yearMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val monthTotal: Long = 0L,
    val dayTotals: LongArray = LongArray(0),
    val dayItems: List<Expense> = emptyList(),
    val dayTotal: Long = 0L
)

data class StatsUiState(
    val yearMonth: YearMonth = YearMonth.now(),
    val statsYear: Int = LocalDate.now().year,
    val selectedDayIndex: Int = -1,
    val monthTotal: Long = 0L,
    val dayVals: LongArray = LongArray(0),
    val yearTotal: Long = 0L,
    val monthVals: LongArray = LongArray(12),
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
    private val statsYear = MutableStateFlow(LocalDate.now().year)
    private val lastDeleted = MutableStateFlow<Expense?>(null)

    private val snackChannel = Channel<SnackEvent>(Channel.BUFFERED)
    val snackEvents = snackChannel.receiveAsFlow()

    val themeState: StateFlow<ThemeUiState> = themeStore.theme
        .map { ThemeUiState(prefs = it, ready = true) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeUiState(ready = false))

    private val monthRange = yearMonth.map { it.startDateInt() to it.endDateInt() }

    private val daySumsFlow = monthRange.flatMapLatest { (s, e) ->
        repo.observeDaySums(s, e)
    }

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
        val arr = LongArray(days)
        daySums.forEach { sum ->
            val day = sum.date.dayOfMonth()
            if (day in 1..days) arr[day - 1] = sum.total
        }
        HomeUiState(
            yearMonth = ym,
            selectedDate = sel,
            monthTotal = daySums.sumOf { it.total },
            dayTotals = arr,
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

    private val statsDaySumsFlow = monthRange.flatMapLatest { (s, e) ->
        repo.observeDaySums(s, e)
    }

    private data class StatsNav(val ym: YearMonth, val sy: Int, val sel: LocalDate)
    private data class StatsMonthData(val daySums: List<com.wchaoq.md3accounting.data.local.DaySum>)
    private data class StatsYearData(val monthSums: List<com.wchaoq.md3accounting.data.local.MonthSum>)

    private val statsNav = combine(yearMonth, statsYear, selectedDate) { ym, sy, sel ->
        StatsNav(ym, sy, sel)
    }
    private val statsMonthData = statsDaySumsFlow.map { sums -> StatsMonthData(sums) }
    private val statsYearData = monthSumsFlow.map { sums -> StatsYearData(sums) }

    val statsState: StateFlow<StatsUiState> = combine(
        statsNav, statsMonthData, statsYearData
    ) { nav, monthData, yearData ->
        val days = nav.ym.lengthOfMonth()
        val dayVals = LongArray(days)
        monthData.daySums.forEach { sum ->
            val day = sum.date.dayOfMonth()
            if (day in 1..days) dayVals[day - 1] = sum.total
        }
        val monthVals = LongArray(12)
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
            dayVals = dayVals,
            yearTotal = yearData.monthSums.sumOf { it.total },
            monthVals = monthVals,
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

    fun stepYear(delta: Int) {
        statsYear.value += delta
    }

    fun openMonth(monthIndex0: Int) {
        yearMonth.value = YearMonth.of(statsYear.value, monthIndex0 + 1)
        selectedDate.value = LocalDate.of(statsYear.value, monthIndex0 + 1, 1)
    }

    fun pickDay(dayIndex0: Int) {
        val ym = yearMonth.value
        selectedDate.value = LocalDate.of(ym.year, ym.month, dayIndex0 + 1)
    }

    fun addExpense(title: String, amountCents: Long) {
        if (title.isBlank() || amountCents <= 0) return
        val e = Expense(
            id = UUID.randomUUID().toString(),
            date = selectedDate.value.toDateInt(),
            title = title.trim(),
            amount = amountCents
        )
        viewModelScope.launch {
            val ok = repo.add(e)
            if (ok) snackChannel.send(SnackEvent("已添加"))
            else snackChannel.send(SnackEvent("添加失败，请重试"))
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            val removed = repo.delete(id) ?: return@launch
            lastDeleted.value = removed
            snackChannel.send(SnackEvent("已删除记录", actionLabel = "撤销", isUndoDelete = true))
        }
    }

    fun undoDelete() {
        val e = lastDeleted.value ?: return
        viewModelScope.launch {
            if (repo.restore(e)) {
                lastDeleted.value = null
                snackChannel.send(SnackEvent("已恢复"))
            }
        }
    }

    fun setHue(hue: Int) {
        viewModelScope.launch { themeStore.setHue(hue) }
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
