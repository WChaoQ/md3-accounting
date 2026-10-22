package com.wchaoq.md3accounting.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wchaoq.md3accounting.R
import com.wchaoq.md3accounting.data.Expense
import com.wchaoq.md3accounting.ui.theme.ThemeHues
import com.wchaoq.md3accounting.ui.theme.hsl
import com.wchaoq.md3accounting.util.money

@Composable
fun MdCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        content = content
    )
}

@Composable
fun SummaryCard(monthLabel: String, totalCents: Long) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = scheme.primaryContainer
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = money(totalCents),
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                color = scheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ExpenseItem(
    expense: Expense,
    onDelete: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(scheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = expense.title.firstOrNull()?.toString() ?: "·",
                color = scheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = expense.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = money(expense.amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(painterResource(R.drawable.ic_close), contentDescription = "删除", tint = scheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EmptyDay() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painterResource(R.drawable.ic_receipt),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "该日暂无支出记录",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun BarChart(
    values: LongArray,
    labels: (Int) -> String,
    highlightIndex: Int,
    onBarClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val n = values.size.coerceAtLeast(1)
    val max = values.maxOrNull()?.coerceAtLeast(1L) ?: 1L
    val scheme = MaterialTheme.colorScheme
    val barColor = scheme.secondaryContainer
    val hlColor = scheme.primary
    val labelColor = scheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.merge(
        TextStyle(color = labelColor, fontSize = 10.sp)
    )
    val layouts = remember(values, highlightIndex, labelStyle) {
        Array(n) { i ->
            val t = labels(i)
            if (t.isEmpty()) null else textMeasurer.measure(t, style = labelStyle)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .pointerInput(n) {
                detectTapGestures { offset ->
                    val idx = ((offset.x / size.width) * n).toInt().coerceIn(0, n - 1)
                    onBarClick(idx)
                }
            }
            .drawWithCache {
                val labelArea = 18.dp.toPx()
                val gap = 2.dp.toPx()
                val barAreaH = size.height - labelArea
                val slotW = size.width / n
                val barW = (slotW - gap).coerceAtLeast(1f)
                onDrawBehind {
                    for (i in 0 until n) {
                        val v = if (i < values.size) values[i] else 0L
                        val h = ((v.toFloat() / max) * (barAreaH - 4.dp.toPx())).coerceAtLeast(2.dp.toPx())
                        val left = i * slotW + gap / 2
                        val top = barAreaH - h
                        drawRoundRect(
                            color = if (i == highlightIndex) hlColor else barColor,
                            topLeft = Offset(left, top),
                            size = Size(barW, h),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        layouts.getOrNull(i)?.let { layout ->
                            val tx = left + (barW - layout.size.width) / 2f
                            val ty = barAreaH + 2.dp.toPx()
                            drawText(
                                textLayoutResult = layout,
                                color = labelColor,
                                topLeft = Offset(tx.coerceAtLeast(0f), ty)
                            )
                        }
                    }
                }
            }
    )
}

@Composable
fun ThemeSwatches(
    currentHue: Int,
    dark: Boolean,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ThemeHues.forEach { (name, hue) ->
            val bg = hsl(
                hue.toFloat(),
                0.5f,
                if (dark) 0.72f else 0.42f
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(bg)
                    .clickable { onSelect(hue) },
                contentAlignment = Alignment.Center
            ) {
                if (hue == currentHue) {
                    Icon(
                        painterResource(R.drawable.ic_check),
                        contentDescription = name,
                        tint = if (dark) Color(0xFF222222) else Color.White
                    )
                }
            }
        }
    }
}
