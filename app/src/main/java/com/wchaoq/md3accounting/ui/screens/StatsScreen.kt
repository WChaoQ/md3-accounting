package com.wchaoq.md3accounting.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wchaoq.md3accounting.UiState
import com.wchaoq.md3accounting.ui.components.BarChart
import com.wchaoq.md3accounting.ui.components.MdCard
import com.wchaoq.md3accounting.ui.theme.LocalAppColors
import com.wchaoq.md3accounting.util.money
import com.wchaoq.md3accounting.util.prefix

@Composable
fun StatsScreen(
    state: UiState,
    onChangeMonth: (Int) -> Unit,
    onStepYear: (Int) -> Unit,
    onOpenMonth: (Int) -> Unit,
    onPickDay: (Int) -> Unit
) {
    val ym = state.yearMonth
    val prefix = ym.prefix()
    val monthList = state.expenses.filter { it.date.startsWith(prefix) }
    val monthTotal = monthList.sumOf { it.amount }
    val daysInMonth = ym.lengthOfMonth()
    val dayVals = DoubleArray(daysInMonth)
    monthList.forEach { e ->
        val d = e.date.substring(8).toInt() - 1
        if (d in 0 until daysInMonth) dayVals[d] += e.amount
    }
    val selIdx = if (state.selectedDate.year == ym.year && state.selectedDate.month == ym.month)
        state.selectedDate.dayOfMonth - 1 else -1

    val yearList = state.expenses.filter { it.date.startsWith("${state.statsYear}-") }
    val yearTotal = yearList.sumOf { it.amount }
    val monthVals = DoubleArray(12)
    yearList.forEach { e ->
        val m = e.date.substring(5, 7).toInt() - 1
        if (m in 0..11) monthVals[m] += e.amount
    }
    val c = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MdCard {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${ym.year}年${ym.monthValue}月",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onChangeMonth(-1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "上个月")
                    }
                    IconButton(onClick = { onChangeMonth(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "下个月")
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Text("总支出 ", style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant)
                        Text(money(monthTotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                    if (selIdx >= 0) {
                        Row {
                            Text("${selIdx + 1}日 ", style = MaterialTheme.typography.bodyMedium, color = c.primary)
                            Text(money(dayVals[selIdx]), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = c.primary)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                BarChart(
                    values = dayVals.toList(),
                    labels = { i -> if (i % 5 == 0 || i == 0) "${i + 1}" else "" },
                    highlightIndex = selIdx,
                    onBarClick = onPickDay
                )
            }
        }

        MdCard {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${state.statsYear}年",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onStepYear(-1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "上一年")
                    }
                    IconButton(onClick = { onStepYear(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "下一年")
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Text("年支出 ", style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant)
                        Text(money(yearTotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                    Row {
                        Text("${ym.monthValue}月 ", style = MaterialTheme.typography.bodyMedium, color = c.primary)
                        Text(money(monthVals[ym.monthValue - 1]), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = c.primary)
                    }
                }
                Spacer(Modifier.height(12.dp))
                BarChart(
                    values = monthVals.toList(),
                    labels = { i -> "${i + 1}月" },
                    highlightIndex = ym.monthValue - 1,
                    onBarClick = onOpenMonth
                )
            }
        }
    }
}
