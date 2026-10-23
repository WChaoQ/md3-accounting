package com.wchaoq.md3accounting.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "md3_theme")

class ThemeStore(private val context: Context) {

    private val hueKey = intPreferencesKey("hue")
    private val darkKey = booleanPreferencesKey("dark")
    private val darkSetKey = booleanPreferencesKey("dark_set")
    private val dynamicColorKey = booleanPreferencesKey("dynamic_color")

    val theme: Flow<ThemePrefs> = context.themeDataStore.data.map { prefs ->
        ThemePrefs(
            hue = prefs[hueKey] ?: 265,
            dark = if (prefs[darkSetKey] == true) prefs[darkKey] ?: false else null,
            dynamicColor = prefs[dynamicColorKey] ?: false
        )
    }

    suspend fun setHue(hue: Int) {
        context.themeDataStore.edit { it[hueKey] = hue }
    }

    suspend fun setDark(dark: Boolean) {
        context.themeDataStore.edit {
            it[darkKey] = dark
            it[darkSetKey] = true
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.themeDataStore.edit { it[dynamicColorKey] = enabled }
    }
}
