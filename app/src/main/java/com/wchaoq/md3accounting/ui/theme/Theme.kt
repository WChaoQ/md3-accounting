package com.wchaoq.md3accounting.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun MD3AccountingTheme(
    hue: Int = 265,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val scheme = remember(hue, darkTheme) { buildColorScheme(hue, darkTheme) }
    MaterialTheme(
        colorScheme = scheme,
        typography = Typography,
        content = content
    )
}
