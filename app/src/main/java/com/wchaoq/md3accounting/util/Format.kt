package com.wchaoq.md3accounting.util

import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

fun money(n: Double): String = "\u00a5%.2f".format(Locale.US, n)

fun fmtAmount(n: Double): String {
    val v = (n * 100).toLong() / 100.0
    return if (v == v.toLong().toDouble()) v.toLong().toString() else "%.1f".format(Locale.US, v)
}

fun LocalDate.key(): String = "%04d-%02d-%02d".format(year, monthValue, dayOfMonth)

fun YearMonth.prefix(): String = "%04d-%02d".format(year, monthValue)
