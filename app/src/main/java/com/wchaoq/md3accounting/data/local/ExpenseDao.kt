package com.wchaoq.md3accounting.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE date = :dateInt ORDER BY createdAt, id")
    fun observeByDate(dateInt: Int): Flow<List<ExpenseEntity>>

    @Query(
        """
        SELECT date, SUM(amount) AS total FROM expenses
        WHERE date >= :start AND date <= :end
        GROUP BY date
        """
    )
    fun observeDaySums(start: Int, end: Int): Flow<List<DaySum>>

    @Query(
        """
        SELECT (date / 100) AS yearMonth, SUM(amount) AS total FROM expenses
        WHERE date >= :start AND date <= :end
        GROUP BY date / 100
        """
    )
    fun observeMonthSums(start: Int, end: Int): Flow<List<MonthSum>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ExpenseEntity)

    @Update
    suspend fun update(entity: ExpenseEntity): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ExpenseEntity>)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ExpenseEntity?

    @Query("SELECT * FROM expenses ORDER BY date DESC, createdAt DESC, id DESC")
    suspend fun getAll(): List<ExpenseEntity>

    @Query("DELETE FROM expenses")
    suspend fun deleteAll(): Int

    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun count(): Int
}
