package com.wchaoq.md3accounting.util

import java.time.LocalDate
import java.time.YearMonth

/** Pack LocalDate as yyyyMMdd integer for Room indexes and range queries. */
fun LocalDate.toDateInt(): Int = year * 10_000 + monthValue * 100 + dayOfMonth

fun Int.toLocalDate(): LocalDate =
    LocalDate.of(this / 10_000, (this / 100) % 100, this % 100)

fun YearMonth.startDateInt(): Int = year * 10_000 + monthValue * 100 + 1

fun YearMonth.endDateInt(): Int = year * 10_000 + monthValue * 100 + lengthOfMonth()

fun YearMonth.yearMonthInt(): Int = year * 100 + monthValue

/** Extract day-of-month (1..31) from yyyyMMdd. */
fun Int.dayOfMonth(): Int = this % 100

/** Extract month (1..12) from yyyyMMdd. */
fun Int.monthOfYear(): Int = (this / 100) % 100

/** Extract year from yyyyMMdd. */
fun Int.yearOf(): Int = this / 10_000
