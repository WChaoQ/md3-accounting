package com.wchaoq.md3accounting.data

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.wchaoq.md3accounting.data.local.AppDatabase
import com.wchaoq.md3accounting.data.local.DaySum
import com.wchaoq.md3accounting.data.local.ExpenseEntity
import com.wchaoq.md3accounting.data.local.MonthSum
import com.wchaoq.md3accounting.util.toDateInt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate

private val Context.legacyStore by preferencesDataStore(name = "md3_accounting")

class ExpenseRepository(private val context: Context) {

    private val dao = AppDatabase.get(context).expenseDao()
    private val json = Json { ignoreUnknownKeys = true }

    fun observeByDate(dateInt: Int): Flow<List<Expense>> =
        dao.observeByDate(dateInt).map { list -> list.map { it.toDomain() } }

    fun observeDaySums(start: Int, end: Int): Flow<List<DaySum>> =
        dao.observeDaySums(start, end)

    fun observeMonthSums(start: Int, end: Int): Flow<List<MonthSum>> =
        dao.observeMonthSums(start, end)

    /** @return true if inserted successfully */
    suspend fun add(expense: Expense): Boolean = runCatching {
        dao.insert(expense.toEntity())
        true
    }.getOrDefault(false)

    suspend fun allExpenses(): List<Expense> = dao.getAll().map { it.toDomain() }

    /** Update an existing record; returns false when the record does not exist or the update fails. */
    suspend fun update(expense: Expense): Boolean = runCatching {
        dao.update(expense.toEntity()) > 0
    }.getOrDefault(false)

    /** @return removed expense, or null if not found */
    suspend fun delete(id: String): Expense? {
        val existing = dao.getById(id) ?: return null
        val n = dao.deleteById(id)
        return if (n > 0) existing.toDomain() else null
    }

    suspend fun restore(expense: Expense): Boolean = runCatching {
        dao.insert(expense.toEntity())
        true
    }.getOrDefault(false)

    /**
     * One-shot migration from legacy DataStore JSON (`records_v2`).
     * Supports both Double and Long amount fields. Clears the key after success.
     */
    suspend fun migrateFromLegacyIfNeeded() {
        if (dao.count() > 0) {
            clearLegacyKey()
            return
        }
        val key = stringPreferencesKey("records_v2")
        val raw = context.legacyStore.data.first()[key] ?: return
        val entities = decodeLegacy(raw) ?: return
        if (entities.isNotEmpty()) {
            dao.insertAll(entities)
        }
        context.legacyStore.edit { it.remove(key) }
    }

    private suspend fun clearLegacyKey() {
        val key = stringPreferencesKey("records_v2")
        context.legacyStore.edit { it.remove(key) }
    }

    private fun decodeLegacy(raw: String): List<ExpenseEntity>? {
        runCatching {
            json.decodeFromString<List<LegacyExpenseLong>>(raw)
                .map {
                    ExpenseEntity(
                        id = it.id,
                        date = parseDate(it.date),
                        title = it.title,
                        amount = it.amount,
                        createdAt = 0L
                    )
                }
        }.getOrNull()?.let { return it }

        return runCatching {
            json.decodeFromString<List<LegacyExpenseDouble>>(raw)
                .map {
                    ExpenseEntity(
                        id = it.id,
                        date = parseDate(it.date),
                        title = it.title,
                        amount = Math.round(it.amount * 100.0),
                        createdAt = 0L
                    )
                }
        }.getOrNull()
    }

    private fun parseDate(s: String): Int {
        return runCatching {
            val parts = s.split('-')
            parts[0].toInt() * 10_000 + parts[1].toInt() * 100 + parts[2].toInt()
        }.getOrElse { LocalDate.now().toDateInt() }
    }

    private fun ExpenseEntity.toDomain() = Expense(id, date, title, amount, createdAt)
    private fun Expense.toEntity() = ExpenseEntity(id, date, title, amount, createdAt)

    @Serializable
    private data class LegacyExpenseLong(
        val id: String,
        val date: String,
        val title: String,
        val amount: Long
    )

    @Serializable
    private data class LegacyExpenseDouble(
        val id: String,
        val date: String,
        val title: String,
        val amount: Double
    )
}
