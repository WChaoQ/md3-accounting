package com.wchaoq.md3accounting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wchaoq.md3accounting.data.ExpenseRepository
import com.wchaoq.md3accounting.ui.components.ThemeSwatches
import com.wchaoq.md3accounting.ui.screens.MainScreen
import com.wchaoq.md3accounting.ui.screens.StatsScreen
import com.wchaoq.md3accounting.ui.theme.LocalAppColors
import com.wchaoq.md3accounting.ui.theme.MD3AccountingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = ExpenseRepository(applicationContext)
        setContent {
            val vm: ExpenseViewModel = viewModel(factory = ExpenseViewModel.Factory(repo))
            val state by vm.uiState.collectAsState()
            MD3AccountingTheme(hue = state.theme.hue, darkTheme = state.theme.dark) {
                AccountingApp(vm, state)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountingApp(vm: ExpenseViewModel, state: UiState) {
    var tab by remember { mutableIntStateOf(0) }
    var showTheme by remember { mutableStateOf(false) }
    val snackHost = remember { SnackbarHostState() }
    val c = LocalAppColors.current

    LaunchedEffect(state.snackMessage) {
        val msg = state.snackMessage ?: return@LaunchedEffect
        val result = snackHost.showSnackbar(
            message = msg,
            actionLabel = state.snackActionLabel,
            duration = if (state.snackActionLabel != null) SnackbarDuration.Long else SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            vm.undoDelete()
        }
        vm.clearSnack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = c.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(if (tab == 0) "记账" else "统计")
                    }
                },
                actions = {
                    IconButton(onClick = { showTheme = true }) {
                        Icon(Icons.Default.Palette, contentDescription = "主题颜色")
                    }
                    IconButton(onClick = { vm.toggleDark() }) {
                        Icon(
                            if (state.theme.dark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "切换深色模式"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = c.surface,
                    titleContentColor = c.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = c.surfaceContainerHigh) {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    },
                    label = { Text("记账") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = c.secondaryContainer
                    )
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = {
                        Icon(Icons.Default.BarChart, contentDescription = null)
                    },
                    label = { Text("统计") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = c.secondaryContainer
                    )
                )
            }
        },
        snackbarHost = {
            SnackbarHost(snackHost) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF322F35),
                    contentColor = Color(0xFFF5EFF7),
                    actionColor = Color(0xFFD0BCFF),
                    shape = RoundedCornerShape(4.dp)
                )
            }
        },
        containerColor = c.surface
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (tab) {
                0 -> MainScreen(
                    state = state,
                    onChangeMonth = vm::changeMonth,
                    onGoToday = vm::goToday,
                    onSelectDate = vm::selectDate,
                    onAdd = vm::addExpense,
                    onDelete = vm::deleteExpense
                )
                1 -> StatsScreen(
                    state = state,
                    onChangeMonth = vm::changeMonth,
                    onStepYear = vm::stepYear,
                    onOpenMonth = { idx ->
                        vm.openMonth(idx)
                        tab = 0
                    },
                    onPickDay = { idx ->
                        vm.pickDay(idx)
                        tab = 0
                    }
                )
            }
        }
    }

    if (showTheme) {
        AlertDialog(
            onDismissRequest = { showTheme = false },
            icon = { Icon(Icons.Default.Palette, contentDescription = null, tint = c.primary) },
            title = {
                Text(
                    "主题颜色",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                ThemeSwatches(
                    currentHue = state.theme.hue,
                    dark = state.theme.dark,
                    onSelect = { vm.setHue(it) }
                )
            },
            confirmButton = {
                TextButton(onClick = { showTheme = false }) { Text("完成") }
            },
            containerColor = c.surfaceContainerHigh,
            shape = RoundedCornerShape(28.dp)
        )
    }
}
