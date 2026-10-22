package com.wchaoq.md3accounting.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wchaoq.md3accounting.UiState
import com.wchaoq.md3accounting.ui.components.EmptyDay
import com.wchaoq.md3accounting.ui.components.ExpenseItem
import com.wchaoq.md3accounting.ui.components.MdCard
import com.wchaoq.md3accounting.ui.components.SummaryCard
import com.wchaoq.md3accounting.ui.theme.LocalAppColors
import com.wchaoq.md3accounting.util.fmtAmount
import com.wchaoq.md3accounting.util.key
import com.wchaoq.md3accounting.util.money
import com.wchaoq.md3accounting.util.prefix
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MainScreen(
    state: UiState,
    onChangeMonth: (Int) -> Unit,
    onGoToday: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onAdd: (String, Double) -> Unit,
    onDelete: (String) -> Unit
) {
    val ym = state.yearMonth
    val prefix = ym.prefix()
    val monthTotal = state.expenses
        .filter { it.date.startsWith(prefix) }
        .sumOf { it.amount }
    val dayItems = state.expenses.filter { it.date == state.selectedDate.key() }
    val dayTotal = dayItems.sumOf { it.amount }

    val dayTotals = remember(state.expenses, prefix) {
        state.expenses
            .filter { it.date.startsWith(prefix) }
            .groupBy { it.date }
            .mapValues { (_, v) -> v.sumOf { it.amount } }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SummaryCard("${ym.monthValue}月总支出", monthTotal)

        MdCard {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${ym.year}年${ym.monthValue}月",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onGoToday) { Text("今天") }
                    IconButton(onClick = { onChangeMonth(-1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "上个月")
                    }
                    IconButton(onClick = { onChangeMonth(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "下个月")
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth()) {
                    listOf("日", "一", "二", "三", "四", "五", "六").forEach {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                it,
                                style = MaterialTheme.typography.labelMedium,
                                color = LocalAppColors.current.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                CalendarGrid(
                    yearMonth = ym,
                    selected = state.selectedDate,
                    today = LocalDate.now(),
                    dayTotals = dayTotals,
                    onSelect = onSelectDate
                )
            }
        }

        MdCard {
            Column(Modifier.padding(20.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val d = state.selectedDate
                    Text(
                        "${d.year}年${d.monthValue}月${d.dayOfMonth}日",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "当日支出",
                            style = MaterialTheme.typography.labelMedium,
                            color = LocalAppColors.current.onSurfaceVariant
                        )
                        Text(
                            money(dayTotal),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 22.sp,
                                color = LocalAppColors.current.primary
                            ),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (dayItems.isEmpty()) {
                    EmptyDay()
                } else {
                    Column(modifier = Modifier.heightIn(max = 280.dp)) {
                        dayItems.forEach { e ->
                            ExpenseItem(e) { onDelete(e.id) }
                        }
                    }
                }
                HorizontalDivider(
                    Modifier.padding(vertical = 12.dp),
                    color = LocalAppColors.current.outlineVariant.copy(alpha = 0.5f)
                )
                AddExpenseForm(onAdd = onAdd)
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selected: LocalDate,
    today: LocalDate,
    dayTotals: Map<String, Double>,
    onSelect: (LocalDate) -> Unit
) {
    val firstDow = yearMonth.atDay(1).dayOfWeek.value % 7
    val days = yearMonth.lengthOfMonth()
    val cells = firstDow + days
    val rows = (cells + 6) / 7
    val c = LocalAppColors.current
    val cellHeight = 56.dp

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(rows) { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(7) { col ->
                    val idx = row * 7 + col
                    val dayNum = idx - firstDow + 1
                    if (dayNum in 1..days) {
                        val date = yearMonth.atDay(dayNum)
                        val key = date.key()
                        val total = dayTotals[key]
                        val isSel = date == selected
                        val isToday = date == today
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(cellHeight)
                                .clip(RoundedCornerShape(14.dp))
                                .then(
                                    if (isSel) Modifier.background(c.primary)
                                    else if (isToday) Modifier.border(
                                        1.5.dp, c.primary, RoundedCornerShape(14.dp)
                                    )
                                    else Modifier
                                )
                                .clickable { onSelect(date) }
                                .padding(horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "$dayNum",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = when {
                                        isSel -> c.onPrimary
                                        isToday -> c.primary
                                        else -> c.onSurface
                                    }
                                )
                                if (total != null && total > 0) {
                                    Text(
                                        fmtAmount(total),
                                        fontSize = 10.sp,
                                        color = if (isSel) c.onPrimary else c.onSurfaceVariant,
                                        fontWeight = if (isSel) FontWeight.Medium else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(Modifier.weight(1f).height(cellHeight))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddExpenseForm(onAdd: (String, Double) -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val valid = title.trim().isNotEmpty() && (amount.toDoubleOrNull() ?: 0.0) > 0
    val c = LocalAppColors.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= 30) title = it },
                label = { Text("事项名称") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = c.primary,
                    cursorColor = c.primary,
                    focusedLabelColor = c.primary
                )
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("金额 ¥") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(0.55f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = c.primary,
                    cursorColor = c.primary,
                    focusedLabelColor = c.primary
                )
            )
        }
        Button(
            onClick = {
                onAdd(title.trim(), amount.toDoubleOrNull() ?: 0.0)
                title = ""
                amount = ""
            },
            enabled = valid,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = c.primary,
                contentColor = c.onPrimary,
                disabledContainerColor = c.onSurface.copy(alpha = 0.12f),
                disabledContentColor = c.onSurface.copy(alpha = 0.38f)
            )
        ) {
            Icon(Icons.Default.Check, null, Modifier.size(20.dp))
            Spacer(Modifier.size(8.dp))
            Text("添加支出", fontWeight = FontWeight.Medium)
        }
    }
}
