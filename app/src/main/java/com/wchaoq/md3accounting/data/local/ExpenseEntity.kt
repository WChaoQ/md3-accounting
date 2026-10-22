package com.wchaoq.md3accounting.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["date"])]
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    /** yyyyMMdd */
    val date: Int,
    val title: String,
    /** Amount in cents */
    val amount: Long
)

/** Projection for GROUP BY date. */
data class DaySum(
    val date: Int,
    val total: Long
)

/** Projection for GROUP BY year-month (date / 100). */
data class MonthSum(
    val yearMonth: Int,
    val total: Long
)
