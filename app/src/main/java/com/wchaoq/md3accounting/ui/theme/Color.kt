package com.wchaoq.md3accounting.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

fun hsl(h: Float, s: Float, l: Float, a: Float = 1f): Color {
    val argb = ColorUtils.HSLToColor(floatArrayOf(h, s, l))
    return Color(argb).copy(alpha = a)
}

data class AppColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val surface: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val outline: Color,
    val outlineVariant: Color
)

fun buildColors(hue: Int, dark: Boolean): AppColors {
    val h = hue.toFloat()
    return if (dark) {
        AppColors(
            primary = hsl(h, 0.80f, 0.82f),
            onPrimary = hsl(h, 0.60f, 0.20f),
            primaryContainer = hsl(h, 0.40f, 0.30f),
            onPrimaryContainer = hsl(h, 1.00f, 0.92f),
            secondaryContainer = hsl(h, 0.18f, 0.28f),
            onSecondaryContainer = hsl(h, 0.30f, 0.90f),
            surface = hsl(h, 0.15f, 0.08f),
            onSurface = hsl(h, 0.15f, 0.90f),
            onSurfaceVariant = hsl(h, 0.10f, 0.78f),
            surfaceContainer = hsl(h, 0.12f, 0.12f),
            surfaceContainerHigh = hsl(h, 0.10f, 0.17f),
            outline = hsl(h, 0.06f, 0.60f),
            outlineVariant = hsl(h, 0.08f, 0.29f)
        )
    } else {
        AppColors(
            primary = hsl(h, 0.50f, 0.40f),
            onPrimary = Color.White,
            primaryContainer = hsl(h, 0.85f, 0.90f),
            onPrimaryContainer = hsl(h, 0.70f, 0.15f),
            secondaryContainer = hsl(h, 0.35f, 0.89f),
            onSecondaryContainer = hsl(h, 0.35f, 0.15f),
            surface = hsl(h, 0.50f, 0.98f),
            onSurface = hsl(h, 0.12f, 0.12f),
            onSurfaceVariant = hsl(h, 0.10f, 0.30f),
            surfaceContainer = hsl(h, 0.35f, 0.95f),
            surfaceContainerHigh = hsl(h, 0.25f, 0.91f),
            outline = hsl(h, 0.08f, 0.47f),
            outlineVariant = hsl(h, 0.15f, 0.80f)
        )
    }
}

val ThemeHues = listOf(
    "\u7d2b" to 265,
    "\u84dd" to 215,
    "\u9752" to 185,
    "\u7eff" to 145,
    "\u91d1" to 45,
    "\u6a59" to 25,
    "\u7ea2" to 5,
    "\u7c89" to 330
)
