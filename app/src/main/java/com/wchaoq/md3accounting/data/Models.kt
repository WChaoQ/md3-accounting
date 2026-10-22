package com.wchaoq.md3accounting.data

import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val id: String,
    val date: String, // yyyy-MM-dd
    val title: String,
    val amount: Double
)

@Serializable
data class ThemePrefs(
    val hue: Int = 265,
    val dark: Boolean = false
)
