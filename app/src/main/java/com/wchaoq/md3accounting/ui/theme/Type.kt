package com.wchaoq.md3accounting.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Only override styles that differ from M3 defaults. */
val Typography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 22.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 20.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 10.sp)
)
