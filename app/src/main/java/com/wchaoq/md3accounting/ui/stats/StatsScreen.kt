package com.wchaoq.md3accounting.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    onPickDay: (Int) -> Unit
) {
    val ym = state.yearMonth
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
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
                        Icon(painterResource(R.drawable.ic_chevron_left), "上个月")
                    }
                    IconButton(onClick = { onChangeMonth(1) }) {
                        Icon(painterResource(R.drawable.ic_chevron_right), "下个月")
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Text("总支出 ", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                        Text(money(state.monthTotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                    if (state.selectedDayIndex >= 0) {
                        Row {
                            Text(
                                "${state.selectedDayIndex + 1}日 ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.primary
                            )
                            Text(
                                money(state.dayVals.getOrElse(state.selectedDayIndex) { 0L }),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = scheme.primary
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                BarChart(
                    values = state.dayVals,
                    labels = { i -> if (i % 5 == 0 || i == 0) "${i + 1}" else "" },
                    highlightIndex = state.selectedDayIndex,
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
                        Icon(painterResource(R.drawable.ic_chevron_left), "上一年")
                    }
                    IconButton(onClick = { onStepYear(1) }) {
                        Icon(painterResource(R.drawable.ic_chevron_right), "下一年")
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Text("年支出 ", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                        Text(money(state.yearTotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                    if (state.highlightMonthIndex >= 0) {
                        Row {
                            Text(
                                "${ym.monthValue}月 ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.primary
                            )
                            Text(
                                money(state.monthVals.getOrElse(ym.monthValue - 1) { 0L }),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = scheme.primary
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                BarChart(
                    values = state.monthVals,
                    labels = { i -> "${i + 1}月" },
                    highlightIndex = state.highlightMonthIndex,
                    onBarClick = onOpenMonth
                )
            }
        }
    }
}
