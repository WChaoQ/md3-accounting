package com.wchaoq.md3accounting.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.imul

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "md3_accounting")

class ExpenseRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val recordsKey = stringPreferencesKey("records_v2")
    private val themeKey = stringPreferencesKey("theme_v1")

    val expenses: Flow<List<Expense>> = context.dataStore.data.map { prefs ->
        val raw = prefs[recordsKey] ?: return@map emptyList()
        runCatching { json.decodeFromString<List<Expense>>(raw) }.getOrElse { emptyList() }
    }

    val theme: Flow<ThemePrefs> = context.dataStore.data.map { prefs ->
        val raw = prefs[themeKey] ?: return@map ThemePrefs()
        runCatching { json.decodeFromString<ThemePrefs>(raw) }.getOrElse { ThemePrefs() }
    }

    suspend fun ensureInitialized() {
        val prefs = context.dataStore.data.first()
        if (prefs[recordsKey] == null) {
            val demo = generateDemo()
            saveAll(demo)
        }
    }

    suspend fun add(expense: Expense) {
        val current = expenses.first().toMutableList()
        current.add(expense)
        saveAll(current)
    }

    suspend fun delete(id: String): Expense? {
        val current = expenses.first().toMutableList()
        val idx = current.indexOfFirst { it.id == id }
        if (idx < 0) return null
        val removed = current.removeAt(idx)
        saveAll(current)
        return removed
    }

    suspend fun restore(expense: Expense) {
        val current = expenses.first().toMutableList()
        current.add(expense)
        saveAll(current)
    }

    suspend fun setTheme(prefs: ThemePrefs) {
        context.dataStore.edit { it[themeKey] = json.encodeToString(prefs) }
    }

    private suspend fun saveAll(list: List<Expense>) {
        context.dataStore.edit { it[recordsKey] = json.encodeToString(list) }
    }

    /** Fixed-seed demo data matching the web version logic (last year to current month). */
    private fun generateDemo(): List<Expense> {
        var seed = 20260101
        fun rnd(): Double {
            seed += 0x6D2B79F5
            var t = seed
            t = imul(t xor (t ushr 15), t or 1)
            t = t xor (t + imul(t xor (t ushr 7), t or 61))
            return ((t xor (t ushr 14)) ushr 0).toUInt().toDouble() / 4294967296.0
        }
        fun <T> pick(arr: List<T>): T = arr[(rnd() * arr.size).toInt()]

        val demo = mapOf(
            "餐饮" to listOf(
                Triple("早餐", 6.0, 18.0), Triple("午餐外卖", 18.0, 45.0),
                Triple("朋友聚餐", 80.0, 260.0), Triple("咖啡", 15.0, 35.0), Triple("夜宵", 25.0, 70.0)
            ),
            "交通" to listOf(
                Triple("地铁", 3.0, 12.0), Triple("打车", 15.0, 60.0),
                Triple("公交", 2.0, 6.0), Triple("加油", 200.0, 400.0)
            ),
            "购物" to listOf(Triple("超市采购", 60.0, 260.0), Triple("网购", 40.0, 300.0)),
            "日用" to listOf(Triple("纸巾洗衣液", 30.0, 90.0), Triple("厨房用品", 20.0, 120.0)),
            "服饰" to listOf(Triple("T恤", 60.0, 200.0), Triple("运动鞋", 200.0, 600.0)),
            "娱乐" to listOf(Triple("电影票", 40.0, 90.0), Triple("游戏充值", 30.0, 200.0), Triple("KTV", 100.0, 300.0)),
            "居住" to listOf(Triple("房租", 1800.0, 1800.0)),
            "水电" to listOf(Triple("水电燃气", 150.0, 320.0)),
            "通讯" to listOf(Triple("话费", 50.0, 50.0), Triple("宽带", 100.0, 100.0)),
            "医疗" to listOf(Triple("药店", 20.0, 150.0), Triple("门诊", 80.0, 400.0)),
            "教育" to listOf(Triple("网课", 99.0, 399.0), Triple("图书", 30.0, 120.0)),
            "旅行" to listOf(Triple("机票", 400.0, 1500.0), Triple("酒店", 300.0, 900.0)),
            "运动" to listOf(Triple("健身月卡", 150.0, 150.0), Triple("羽毛球", 40.0, 90.0)),
            "美容" to listOf(Triple("理发", 30.0, 80.0), Triple("护肤品", 100.0, 500.0)),
            "数码" to listOf(Triple("数据线", 20.0, 80.0), Triple("耳机", 150.0, 800.0)),
            "宠物" to listOf(Triple("猫粮", 80.0, 200.0), Triple("宠物医院", 100.0, 400.0)),
            "人情" to listOf(Triple("红包", 100.0, 600.0), Triple("生日礼物", 100.0, 500.0)),
            "其他" to listOf(Triple("杂项", 10.0, 100.0))
        )

        val extras = demo.keys.filter { it !in listOf("餐饮", "交通", "居住", "水电", "通讯") }
        val now = LocalDate.now()
        val out = mutableListOf<Expense>()
        var n = 0

        fun add(date: String, item: Triple<String, Double, Double>) {
            val amount = item.second + rnd() * (item.third - item.second)
            out += Expense(id = "d${n++}", date = date, title = item.first, amount = (amount * 10).toInt() / 10.0)
        }

        for (y in (now.year - 1)..now.year) {
            for (m in 1..12) {
                if (y == now.year && m > now.monthValue) break
                val ym = YearMonth.of(y, m)
                val last = if (y == now.year && m == now.monthValue) now.dayOfMonth else ym.lengthOfMonth()
                fun ds(d: Int) = "%04d-%02d-%02d".format(y, m, d)

                listOf("居住" to 1, "通讯" to 5, "水电" to 8).forEach { (c, d) ->
                    if (d <= last) add(ds(d), demo[c]!![0])
                }
                for (d in 1..last) {
                    if (rnd() < 0.8) add(ds(d), pick(demo["餐饮"]!!))
                    if (rnd() < 0.45) add(ds(d), pick(demo["交通"]!!))
                    if (rnd() < 0.35) {
                        var c = pick(extras)
                        if (c == "旅行" && rnd() > 0.1) c = "购物"
                        add(ds(d), pick(demo[c]!!))
                    }
                }
            }
        }
        return out
    }
}
