package com.wchaoq.md3accounting.util

import java.util.Locale

/** Format cents as ¥ with thousands separator, e.g. ¥12,345.00. */
fun money(cents: Long): String {
    val yuan = cents / 100.0
    return "¥%,.2f".format(Locale.US, yuan)
}

/** Compact amount for calendar cells, rounded to one decimal using integer arithmetic. */
fun fmtAmount(cents: Long): String {
    val tenths = (cents.coerceAtLeast(0L) + 5L) / 10L
    if (tenths >= 100_000L) {
        val tenThousandsTenths = (tenths + 500L) / 1_000L
        val whole = tenThousandsTenths / 10L
        val decimal = tenThousandsTenths % 10L
        return if (decimal == 0L) "${whole}万" else "${whole}.${decimal}万"
    }
    val yuan = tenths / 10L
    val decimal = tenths % 10L
    return if (decimal == 0L) yuan.toString() else "$yuan.$decimal"
}

/** Parse user input like "12.5" or ".5" to cents; null if invalid. */
fun parseAmountToCents(raw: String): Long? {
    val s = raw.trim()
    if (s.isEmpty()) return null
    val parts = s.split('.')
    if (parts.size > 2) return null
    val wholeText = parts[0]
    if (wholeText.length > 9 || (wholeText.isNotEmpty() && wholeText.any { !it.isDigit() })) return null
    if (parts.size == 2 && parts[1].length > 2) return null
    if (parts.size == 2 && parts[1].any { !it.isDigit() }) return null
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
