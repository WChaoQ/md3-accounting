package com.wchaoq.md3accounting.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wchaoq.md3accounting.data.Expense
import com.wchaoq.md3accounting.ui.theme.LocalAppColors
import com.wchaoq.md3accounting.util.money

@Composable
fun MdCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = LocalAppColors.current.surfaceContainer,
        content = content
    )
}

@Composable
fun SummaryCard(monthLabel: String, total: Double) {
    val c = LocalAppColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = c.primaryContainer
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.labelMedium,
                color = c.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = money(total),
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                color = c.onPrimaryContainer,
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
    val c = LocalAppColors.current
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
                .background(c.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = expense.title.firstOrNull()?.toString() ?: "·",
                color = c.onPrimaryContainer,
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
            Icon(Icons.Default.Close, contentDescription = "删除", tint = c.onSurfaceVariant)
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
            Icons.Outlined.ReceiptLong,
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
    values: List<Double>,
    labels: (Int) -> String,
    highlightIndex: Int,
    onBarClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val max = values.maxOrNull()?.coerceAtLeast(0.01) ?: 1.0
    val c = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEachIndexed { i, v ->
            val h = ((v / max) * 112).coerceAtLeast(2.0)
            val isHl = i == highlightIndex
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onBarClick(i) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(h.dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(if (isHl) c.primary else c.secondaryContainer)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = labels(i),
                    style = MaterialTheme.typography.labelSmall,
                    color = c.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ThemeSwatches(
    currentHue: Int,
    dark: Boolean,
    onSelect: (Int) -> Unit
) {
    val hues = com.wchaoq.md3accounting.ui.theme.ThemeHues
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        hues.forEach { (name, hue) ->
            val bg = com.wchaoq.md3accounting.ui.theme.hsl(
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
                        Icons.Default.Check,
                        contentDescription = name,
                        tint = if (dark) Color(0xFF222222) else Color.White
                    )
                }
            }
        }
    }
}
