package com.wchaoq.md3accounting.data

/** Domain expense (date as yyyyMMdd, amount in cents). */
data class Expense(
    val id: String,
    val date: Int,
    val title: String,
    val amount: Long,
    val createdAt: Long = System.currentTimeMillis()
)

data class ThemePrefs(
    val hue: Int = 265,
    /** null = follow system until user toggles */
    val dark: Boolean? = null
)
