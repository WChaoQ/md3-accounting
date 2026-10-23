package com.wchaoq.md3accounting.util

import java.util.Locale

/** Format cents as ¥ with thousands separator, e.g. ¥12,345.00. */
fun money(cents: Long): String {
    val yuan = cents / 100.0
    return "¥%,.2f".format(Locale.US, yuan)
}

/** Compact amount for calendar cells without losing cents to floating-point rounding. */
fun fmtAmount(cents: Long): String {
    val value = cents.coerceAtLeast(0L)
    if (value >= 1_000_000L) {
        val tenthsOfTenThousands = (value + 50_000L) / 100_000L
        val whole = tenthsOfTenThousands / 10L
        val decimal = tenthsOfTenThousands % 10L
        return if (decimal == 0L) "${whole}万" else "${whole}.${decimal}万"
    }

    val yuan = value / 100L
    val remainder = (value % 100L).toInt()
    return when {
        remainder == 0 -> yuan.toString()
        remainder % 10 == 0 -> "$yuan.${remainder / 10}"
        else -> "$yuan.${remainder.toString().padStart(2, '0')}"
    }
}

/** Parse user input like "12.5" or ".5" to cents; null if invalid. */
fun parseAmountToCents(raw: String): Long? {
    val s = raw.trim()
    if (s.isEmpty()) return null
    val parts = s.split('.')
    if (parts.size > 2) return null
    val wholeText = parts[0]
    if (wholeText.length > 9 || (wholeText.isNotEmpty() && wholeText.any { it !in '0'..'9' })) return null
    if (parts.size == 2 && parts[1].length > 2) return null
    if (parts.size == 2 && parts[1].any { it !in '0'..'9' }) return null
    if (wholeText.isEmpty() && (parts.size == 1 || parts[1].isEmpty())) return null
    val whole = if (wholeText.isEmpty()) 0L else wholeText.toLongOrNull() ?: return null
    val fractionText = parts.getOrNull(1).orEmpty()
    val fraction = when (fractionText.length) {
        0 -> 0L
        1 -> (fractionText[0] - '0') * 10L
        2 -> fractionText.toLongOrNull() ?: return null
        else -> return null
    }
    val cents = whole * 100L + fraction
    return cents.takeIf { it > 0L }
}
