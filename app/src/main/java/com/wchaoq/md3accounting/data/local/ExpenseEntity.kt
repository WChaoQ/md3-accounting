package com.wchaoq.md3accounting.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["date", "createdAt"])]
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    /** yyyyMMdd */
    val date: Int,
    val title: String,
    /** Amount in cents */
    val amount: Long,
    /** Creation time in epoch milliseconds. Existing rows migrate with 0 and retain deterministic id ordering. */
    @ColumnInfo(defaultValue = "0") val createdAt: Long = 0L
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
