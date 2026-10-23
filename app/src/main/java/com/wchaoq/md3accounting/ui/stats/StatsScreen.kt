package com.wchaoq.md3accounting.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wchaoq.md3accounting.R
import com.wchaoq.md3accounting.StatsUiState
import com.wchaoq.md3accounting.ui.components.BarChart
import com.wchaoq.md3accounting.ui.components.MdCard
import com.wchaoq.md3accounting.util.money

@Composable
fun StatsScreen(
    state: StatsUiState,
    onChangeMonth: (Int) -> Unit,
    onStepYear: (Int) -> Unit,
    onOpenMonth: (Int) -> Unit,
    onPickDay: (Int) -> Unit,
    onExportCsv: () -> Unit
) {
    val ym = state.yearMonth
    val scheme = MaterialTheme.colorScheme
    var selectedDayIndex by remember(ym) { mutableIntStateOf(-1) }
    LaunchedEffect(state.statsYear) { selectedDayIndex = -1 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MdCard {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${ym.year}年${ym.monthValue}月每日支出",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { selectedDayIndex = -1; onChangeMonth(-1) }) {
                        Icon(painterResource(R.drawable.ic_chevron_left), "上个月")
                    }
                    IconButton(onClick = { selectedDayIndex = -1; onChangeMonth(1) }) {
                        Icon(painterResource(R.drawable.ic_chevron_right), "下个月")
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("本月总支出", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                        Text(money(state.monthTotal), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    if (selectedDayIndex >= 0) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${selectedDayIndex + 1}日支出", style = MaterialTheme.typography.labelSmall, color = scheme.primary)
                            Text(
                                money(state.dayVals.getOrElse(selectedDayIndex) { 0L }),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = scheme.primary
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                BarChart(
                    values = state.dayVals,
                    labels = { i -> if (i == 0 || (i + 1) % 5 == 0) "${i + 1}" else "" },
                    highlightIndex = selectedDayIndex,
                    chartHeight = 136.dp,
                    onBarClick = { index ->
                        selectedDayIndex = if (selectedDayIndex == index) -1 else index
                        if (selectedDayIndex >= 0) onPickDay(index)
                    }
                )
            }
        }

        MdCard {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${state.statsYear}年每月支出",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onStepYear(-1) }) {
                        Icon(painterResource(R.drawable.ic_chevron_left), "上一年")
                    }
                    IconButton(onClick = { onStepYear(1) }) {
                        Icon(painterResource(R.drawable.ic_chevron_right), "下一年")
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("全年总支出", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                        Text(money(state.yearTotal), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    if (state.highlightMonthIndex >= 0) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${ym.monthValue}月支出", style = MaterialTheme.typography.labelSmall, color = scheme.primary)
                            Text(
                                money(state.monthVals.getOrElse(ym.monthValue - 1) { 0L }),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = scheme.primary
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                BarChart(
                    values = state.monthVals,
                    labels = { i -> "${i + 1}月" },
                    highlightIndex = state.highlightMonthIndex,
                    chartHeight = 112.dp,
                    onBarClick = { index ->
                        selectedDayIndex = -1
                        onOpenMonth(index)
                    }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (selectedDayIndex >= 0) "${ym.monthValue}月${selectedDayIndex + 1}日明细"
                    else "支出明细",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (selectedDayIndex >= 0) "点击同一天柱形可收起明细" else "点击上方日期柱形查看当天记录",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }

        if (selectedDayIndex < 0) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    "选择某一天，查看事项和金额",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        } else if (state.dayItems.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    "当天暂无支出记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                state.dayItems.forEach { expense ->
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    expense.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                money(expense.amount),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = scheme.onSurface,
                                maxLines = 1
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = scheme.outlineVariant.copy(alpha = 0.55f),
                            thickness = 0.7.dp
                        )
                    }
                }
            }
        }
    }
}
