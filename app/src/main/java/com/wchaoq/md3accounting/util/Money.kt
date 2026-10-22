package com.wchaoq.md3accounting.util

import java.util.Locale

/** Format cents as ¥ with thousands separator, e.g. ¥12,345.00 */
fun money(cents: Long): String {
    val yuan = cents / 100.0
    return "¥%,.2f".format(Locale.US, yuan)
}

/** Compact amount for calendar cells. */
fun fmtAmount(cents: Long): String {
    val yuan = cents / 100.0
    val v = (yuan * 100).toLong() / 100.0
    return if (v == v.toLong().toDouble()) v.toLong().toString() else "%.1f".format(Locale.US, v)
}

/** Parse user input like "12.5" to cents; null if invalid. */
fun parseAmountToCents(raw: String): Long? {
    val s = raw.trim()
    if (s.isEmpty()) return null
    val parts = s.split('.')
    if (parts.size > 2) return null
    val whole = parts[0].toLongOrNull() ?: return null
    val frac = when {
        parts.size == 1 -> 0L
        parts[1].isEmpty() -> 0L
        parts[1].length == 1 -> (parts[1].toLongOrNull() ?: return null) * 10
        parts[1].length == 2 -> parts[1].toLongOrNull() ?: return null
        else -> return null
    }
    if (whole < 0 || frac < 0) return null
    val cents = whole * 100 + frac
    return if (cents > 0) cents else null
}
