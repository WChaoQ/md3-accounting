package com.wchaoq.md3accounting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wchaoq.md3accounting.data.Expense
import com.wchaoq.md3accounting.data.ExpenseRepository
import com.wchaoq.md3accounting.data.ThemePrefs
import com.wchaoq.md3accounting.util.key
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

data class UiState(
    val expenses: List<Expense> = emptyList(),
    val theme: ThemePrefs = ThemePrefs(),
    val yearMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val statsYear: Int = LocalDate.now().year,
    val lastDeleted: Expense? = null,
    val snackMessage: String? = null,
    val snackActionLabel: String? = null,
    val ready: Boolean = false
)

class ExpenseViewModel(private val repo: ExpenseRepository) : ViewModel() {

    private val yearMonth = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val statsYear = MutableStateFlow(LocalDate.now().year)
    private val lastDeleted = MutableStateFlow<Expense?>(null)
    private val snack = MutableStateFlow<Pair<String, String?>?>(null)

    val uiState: StateFlow<UiState> = combine(
        repo.expenses,
        repo.theme,
        yearMonth,
        selectedDate,
        statsYear
    ) { expenses, theme, ym, sel, sy ->
        Triple(expenses, theme, Triple(ym, sel, sy))
    }.combine(lastDeleted) { triple, deleted ->
        triple to deleted
    }.combine(snack) { (triple, deleted), snackPair ->
        val (expenses, theme, inner) = triple
        val (ym, sel, sy) = inner
        UiState(
            expenses = expenses,
            theme = theme,
            yearMonth = ym,
            selectedDate = sel,
            statsYear = sy,
            lastDeleted = deleted,
            snackMessage = snackPair?.first,
            snackActionLabel = snackPair?.second,
            ready = true
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState(ready = false))

    init {
        viewModelScope.launch { repo.ensureInitialized() }
    }

    fun changeMonth(delta: Int) {
        val ym = yearMonth.value.plusMonths(delta.toLong())
        yearMonth.value = ym
        val d = selectedDate.value
        val maxDay = ym.lengthOfMonth()
        selectedDate.value = LocalDate.of(ym.year, ym.month, minOf(d.dayOfMonth, maxDay))
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

    fun addExpense(title: String, amount: Double) {
        if (title.isBlank() || amount <= 0) return
        val e = Expense(
            id = UUID.randomUUID().toString(),
            date = selectedDate.value.key(),
            title = title.trim(),
            amount = amount
        )
        viewModelScope.launch {
            repo.add(e)
            snack.value = "已添加" to null
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            val removed = repo.delete(id) ?: return@launch
            lastDeleted.value = removed
            snack.value = "已删除记录" to "撤销"
        }
    }

    fun undoDelete() {
        val e = lastDeleted.value ?: return
        viewModelScope.launch {
            repo.restore(e)
            lastDeleted.value = null
            snack.value = "已恢复" to null
        }
    }

    fun clearSnack() {
        snack.value = null
    }

    fun setHue(hue: Int) {
        viewModelScope.launch {
            val current = uiState.value.theme
            repo.setTheme(current.copy(hue = hue))
        }
    }

    fun toggleDark() {
        viewModelScope.launch {
            val current = uiState.value.theme
            repo.setTheme(current.copy(dark = !current.dark))
        }
    }

    class Factory(private val repo: ExpenseRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExpenseViewModel(repo) as T
        }
    }
}
