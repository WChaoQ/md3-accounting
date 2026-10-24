package com.wchaoq.md3accounting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wchaoq.md3accounting.data.ExpenseRepository
import com.wchaoq.md3accounting.data.ThemeStore
import com.wchaoq.md3accounting.ui.AccountingApp
import com.wchaoq.md3accounting.ui.theme.MD3AccountingTheme

class MainActivity : ComponentActivity() {

    private val repo by lazy { ExpenseRepository(applicationContext) }
    private val themeStore by lazy { ThemeStore(applicationContext) }
    private val vm: AccountingViewModel by viewModels {
        AccountingViewModel.Factory(repo, themeStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val theme by vm.themeState.collectAsStateWithLifecycle()
            if (!theme.ready) return@setContent

            val systemDark = isSystemInDarkTheme()
            val dark = theme.prefs.dark ?: systemDark
            val barStyle = if (dark) {
                SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT
                )
            }
            DisposableEffect(dark) {
                enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
                onDispose { }
            }

            MD3AccountingTheme(
                hue = theme.prefs.hue,
                darkTheme = dark,
                dynamicColor = theme.prefs.dynamicColor
            ) {
                AccountingApp(vm = vm, dark = dark, systemDark = systemDark)
            }
        }
    }
}
