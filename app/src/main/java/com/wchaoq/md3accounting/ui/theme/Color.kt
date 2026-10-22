package com.wchaoq.md3accounting.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.ColorUtils

fun hsl(h: Float, s: Float, l: Float, a: Float = 1f): Color {
    val argb = ColorUtils.HSLToColor(floatArrayOf(h, s, l))
    return Color(argb).copy(alpha = a)
}

fun buildColorScheme(hue: Int, dark: Boolean): androidx.compose.material3.ColorScheme {
    val h = hue.toFloat()
    return if (dark) {
        androidx.compose.material3.darkColorScheme(
            primary = hsl(h, 0.80f, 0.82f),
            onPrimary = hsl(h, 0.60f, 0.20f),
            primaryContainer = hsl(h, 0.40f, 0.30f),
            onPrimaryContainer = hsl(h, 1.00f, 0.92f),
            secondary = hsl(h, 0.25f, 0.80f),
            onSecondary = hsl(h, 0.30f, 0.18f),
            secondaryContainer = hsl(h, 0.18f, 0.28f),
            onSecondaryContainer = hsl(h, 0.30f, 0.90f),
            tertiary = hsl((h + 60f) % 360f, 0.40f, 0.80f),
            onTertiary = hsl((h + 60f) % 360f, 0.40f, 0.18f),
            tertiaryContainer = hsl((h + 60f) % 360f, 0.30f, 0.28f),
            onTertiaryContainer = hsl((h + 60f) % 360f, 0.40f, 0.90f),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),
            background = hsl(h, 0.15f, 0.08f),
            onBackground = hsl(h, 0.15f, 0.90f),
            surface = hsl(h, 0.15f, 0.08f),
            onSurface = hsl(h, 0.15f, 0.90f),
            surfaceVariant = hsl(h, 0.12f, 0.20f),
            onSurfaceVariant = hsl(h, 0.10f, 0.78f),
            surfaceContainer = hsl(h, 0.12f, 0.12f),
            surfaceContainerHigh = hsl(h, 0.10f, 0.17f),
            inverseSurface = hsl(h, 0.10f, 0.90f),
            inverseOnSurface = hsl(h, 0.12f, 0.18f),
            inversePrimary = hsl(h, 0.50f, 0.40f),
            outline = hsl(h, 0.06f, 0.60f),
            outlineVariant = hsl(h, 0.08f, 0.29f),
            scrim = Color.Black
        )
    } else {
        androidx.compose.material3.lightColorScheme(
            primary = hsl(h, 0.50f, 0.40f),
            onPrimary = Color.White,
            primaryContainer = hsl(h, 0.85f, 0.90f),
            onPrimaryContainer = hsl(h, 0.70f, 0.15f),
            secondary = hsl(h, 0.25f, 0.40f),
            onSecondary = Color.White,
            secondaryContainer = hsl(h, 0.35f, 0.89f),
            onSecondaryContainer = hsl(h, 0.35f, 0.15f),
            tertiary = hsl((h + 60f) % 360f, 0.40f, 0.40f),
            onTertiary = Color.White,
            tertiaryContainer = hsl((h + 60f) % 360f, 0.50f, 0.90f),
            onTertiaryContainer = hsl((h + 60f) % 360f, 0.40f, 0.15f),
            error = Color(0xFFBA1A1A),
            onError = Color.White,
            errorContainer = Color(0xFFFFDAD6),
            onErrorContainer = Color(0xFF410002),
            background = hsl(h, 0.50f, 0.98f),
            onBackground = hsl(h, 0.12f, 0.12f),
            surface = hsl(h, 0.50f, 0.98f),
            onSurface = hsl(h, 0.12f, 0.12f),
            surfaceVariant = hsl(h, 0.20f, 0.90f),
            onSurfaceVariant = hsl(h, 0.10f, 0.30f),
            surfaceContainer = hsl(h, 0.35f, 0.95f),
            surfaceContainerHigh = hsl(h, 0.25f, 0.91f),
            inverseSurface = hsl(h, 0.12f, 0.18f),
            inverseOnSurface = hsl(h, 0.10f, 0.95f),
            inversePrimary = hsl(h, 0.80f, 0.80f),
            outline = hsl(h, 0.08f, 0.47f),
            outlineVariant = hsl(h, 0.15f, 0.80f),
            scrim = Color.Black
        )
    }
}

val ThemeHues = listOf(
    "紫" to 265,
    "蓝" to 215,
    "青" to 185,
    "绿" to 145,
    "金" to 45,
    "橙" to 25,
    "红" to 5,
    "粉" to 330
)
