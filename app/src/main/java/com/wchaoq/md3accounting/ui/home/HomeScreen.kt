package com.wchaoq.md3accounting.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wchaoq.md3accounting.R
import com.wchaoq.md3accounting.HomeUiState
import com.wchaoq.md3accounting.data.Expense
import com.wchaoq.md3accounting.ui.components.EmptyDay
import com.wchaoq.md3accounting.ui.components.ExpenseItem
import com.wchaoq.md3accounting.ui.components.MdCard
import com.wchaoq.md3accounting.ui.components.SummaryCard
import com.wchaoq.md3accounting.util.money
import com.wchaoq.md3accounting.util.parseAmountToCents
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onSelectDate: (LocalDate) -> Unit,
    onSave: (Expense?, String, Long) -> Unit,
    onDelete: (String) -> Unit
) {
    val ym = state.yearMonth
    val scheme = MaterialTheme.colorScheme
    var editingExpense by remember(state.selectedDate) { mutableStateOf<Expense?>(null) }
    var showEntryDialog by remember(state.selectedDate) { mutableStateOf(false) }
    var deletingExpense by remember { mutableStateOf<Expense?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.selectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli())

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "month-summary") {
                SummaryCard("${ym.monthValue}月总支出", state.monthTotal)
            }
            item(key = "day-header") {
                MdCard {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${state.selectedDate.year}年${state.selectedDate.monthValue}月${state.selectedDate.dayOfMonth}日",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                IconButton(onClick = {
                                    datePickerState.selectedDateMillis = state.selectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
                                    showDatePicker = true
                                }) {
                                    Icon(painterResource(R.drawable.ic_chevron_right), "选择日期")
                                }
                            }
                            Text("当日支出", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                        }
                        Text(
                            money(state.dayTotal),
                            style = MaterialTheme.typography.headlineSmall,
                            color = scheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (state.dayItems.isEmpty()) {
                item(key = "empty-day") { EmptyDay() }
            } else {
                items(state.dayItems, key = { it.id }) { expense ->
                    ExpenseItem(
                        expense = expense,
                        onEdit = {
                            editingExpense = expense
                            showEntryDialog = true
                        },
                        onLongPress = { deletingExpense = expense }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingExpense = null
                showEntryDialog = true
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 16.dp)
        ) {
            Text("+", style = MaterialTheme.typography.headlineMedium)
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        editingExpense = null
                        onSelectDate(date)
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }
        ) {
            DatePicker(state = datePickerState, title = { Text("选择日期") })
        }
    }

    if (showEntryDialog) {
        AlertDialog(
            onDismissRequest = { showEntryDialog = false; editingExpense = null },
            title = { Text(if (editingExpense == null) "添加支出" else "修改记录") },
            text = {
                AddExpenseForm(
                    editingExpense = editingExpense,
                    onSave = { expense, title, amount ->
                        onSave(expense, title, amount)
                        showEntryDialog = false
                        editingExpense = null
                    },
                    onCancelEdit = {
                        showEntryDialog = false
                        editingExpense = null
                    }
                )
            },
            confirmButton = {},
            shape = RoundedCornerShape(28.dp)
        )
    }
    deletingExpense?.let { expense ->
        AlertDialog(
            onDismissRequest = { deletingExpense = null },
            title = { Text("删除这笔支出？") },
            text = { Text("${expense.title} · ${money(expense.amount)}") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(expense.id)
                        deletingExpense = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingExpense = null }) {
                    Text("取消")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

}

@Composable
private fun AddExpenseForm(
    editingExpense: Expense?,
    onSave: (Expense?, String, Long) -> Unit,
    onCancelEdit: () -> Unit
) {
    var title by remember(editingExpense?.id) {
        mutableStateOf(editingExpense?.title.orEmpty())
    }
    var amount by remember(editingExpense?.id) {
        mutableStateOf(editingExpense?.amount?.let(::amountInput).orEmpty())
    }
    val cents = parseAmountToCents(amount)
    val valid = title.isNotBlank() && cents != null
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    fun submit() {
        val amountCents = parseAmountToCents(amount) ?: return
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        onSave(editingExpense, cleanTitle, amountCents)
        focusManager.clearFocus()
        keyboard?.hide()
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= 30) title = it },
                label = { Text("事项") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { raw ->
                    amount = buildString {
                        var dotSeen = false
                        var decimals = 0
                        for (ch in raw) {
                            when {
                                ch in '0'..'9' -> {
                                    if (dotSeen) {
                                        if (decimals < 2) {
                                            append(ch)
                                            decimals++
                                        }
                                    } else if (count { it in '0'..'9' } < 9) {
                                        append(ch)
                                    }
                                }
                                ch == '.' && !dotSeen -> {
                                    append(ch)
                                    dotSeen = true
                                }
                            }
                        }
                    }
                },
                label = { Text("金额 ¥") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { if (valid) submit() }),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = ::submit,
                enabled = valid,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (editingExpense == null) "添加支出" else "保存修改")
            }
            if (editingExpense != null) {
                TextButton(
                    onClick = {
                        onCancelEdit()
                        focusManager.clearFocus()
                        keyboard?.hide()
                    },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("取消")
                }
            }
        }
    }
}

private fun amountInput(cents: Long): String {
    val yuan = cents / 100L
    val remainder = cents % 100L
    return when {
        remainder == 0L -> yuan.toString()
        remainder % 10L == 0L -> "$yuan.${remainder / 10L}"
        else -> "$yuan.${remainder.toString().padStart(2, '0')}"
    }
}
