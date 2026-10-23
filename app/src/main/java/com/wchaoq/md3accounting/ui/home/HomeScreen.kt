package com.wchaoq.md3accounting.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
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
import com.wchaoq.md3accounting.ui.components.EmptyDay
import com.wchaoq.md3accounting.ui.components.ExpenseItem
import com.wchaoq.md3accounting.ui.components.MdCard
import com.wchaoq.md3accounting.ui.components.SummaryCard
import com.wchaoq.md3accounting.util.fmtAmount
import com.wchaoq.md3accounting.util.money
import com.wchaoq.md3accounting.util.parseAmountToCents
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun HomeScreen(
    state: HomeUiState,
    onChangeMonth: (Int) -> Unit,
    onGoToday: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onAdd: (String, Long) -> Unit,
    onDelete: (String) -> Unit
) {
    val ym = state.yearMonth
    val scheme = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(0.dp))
            SummaryCard("${ym.monthValue}月总支出", state.monthTotal)
        }

        item {
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
                            Icon(painterResource(R.drawable.ic_chevron_left), "上个月")
                        }
                        IconButton(onClick = { onChangeMonth(1) }) {
                            Icon(painterResource(R.drawable.ic_chevron_right), "下个月")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth()) {
                        listOf("日", "一", "二", "三", "四", "五", "六").forEach {
                            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = scheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    CalendarGrid(
                        yearMonth = ym,
                        selected = state.selectedDate,
                        today = LocalDate.now(),
                        dayTotals = state.dayTotals,
                        onSelect = onSelectDate
                    )
                }
            }
        }

        item {
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
                                color = scheme.onSurfaceVariant
                            )
                            Text(
                                money(state.dayTotal),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 22.sp,
                                    color = scheme.primary
                                ),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (state.dayItems.isEmpty()) {
                        EmptyDay()
                    } else {
                        state.dayItems.forEach { e ->
                            ExpenseItem(e) { onDelete(e.id) }
                        }
                    }
                    HorizontalDivider(
                        Modifier.padding(vertical = 12.dp),
                        color = scheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    AddExpenseForm(onAdd = onAdd)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selected: LocalDate,
    today: LocalDate,
    dayTotals: LongArray,
    onSelect: (LocalDate) -> Unit
) {
    val firstDow = yearMonth.atDay(1).dayOfWeek.value % 7
    val days = yearMonth.lengthOfMonth()
    val cells = firstDow + days
    val rows = (cells + 6) / 7
    val scheme = MaterialTheme.colorScheme
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
                        val total = dayTotals.getOrElse(dayNum - 1) { 0L }
                        val isSel = date == selected
                        val isToday = date == today
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(cellHeight)
                                .clip(RoundedCornerShape(14.dp))
                                .then(
                                    if (isSel) Modifier.background(scheme.primary)
                                    else if (isToday) Modifier.border(
                                        1.5.dp, scheme.primary, RoundedCornerShape(14.dp)
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
                                        isSel -> scheme.onPrimary
                                        isToday -> scheme.primary
                                        else -> scheme.onSurface
                                    }
                                )
                                if (total > 0) {
                                    Text(
                                        fmtAmount(total),
                                        fontSize = 12.sp,
                                        color = if (isSel) scheme.onPrimary else scheme.onSurfaceVariant,
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
private fun AddExpenseForm(onAdd: (String, Long) -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val cents = parseAmountToCents(amount)
    val valid = title.trim().isNotEmpty() && cents != null
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    fun submit() {
        val amountCents = parseAmountToCents(amount) ?: return
        if (title.trim().isEmpty()) return
        onAdd(title.trim(), amountCents)
        title = ""
        amount = ""
        focusManager.clearFocus()
        keyboard?.hide()
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= 30) title = it },
                label = { Text("事项名称") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { raw ->
                    val filtered = buildString {
                        var dotSeen = false
                        var decimals = 0
                        for (ch in raw) {
                            when {
                                ch.isDigit() -> {
                                    if (dotSeen) {
                                        if (decimals < 2) {
                                            append(ch)
                                            decimals++
                                        }
                                    } else if (count { it.isDigit() } < 9) append(ch)
                                }
                                ch == '.' && !dotSeen -> {
                                    append(ch)
                                    dotSeen = true
                                }
                            }
                        }
                    }
                    amount = filtered
                },
                label = { Text("金额 ¥") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { if (valid) submit() }),
                modifier = Modifier.weight(0.55f)
            )
        }
        Button(
            onClick = { submit() },
            enabled = valid,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("添加支出", fontWeight = FontWeight.Medium)
        }
    }
}
