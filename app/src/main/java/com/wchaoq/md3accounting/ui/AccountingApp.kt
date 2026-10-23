package com.wchaoq.md3accounting.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wchaoq.md3accounting.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wchaoq.md3accounting.AccountingViewModel
import com.wchaoq.md3accounting.ui.components.ThemeSwatches
import com.wchaoq.md3accounting.ui.home.HomeScreen
import com.wchaoq.md3accounting.ui.stats.StatsScreen
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountingApp(
    vm: AccountingViewModel,
    dark: Boolean,
    systemDark: Boolean
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showTheme by rememberSaveable { mutableStateOf(false) }
    val snackHost = remember { SnackbarHostState() }
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    val theme by vm.themeState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.snackEvents.collectLatest { event ->
            snackHost.currentSnackbarData?.dismiss()
            snackHost.showSnackbar(message = event.message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painterResource(R.drawable.ic_wallet),
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(if (tab == 0) "记账" else "统计")
                    }
                },
                actions = {
                    IconButton(onClick = { showTheme = true }) {
                        Icon(painterResource(R.drawable.ic_palette), contentDescription = "主题颜色")
                    }
                    IconButton(onClick = { vm.toggleDark(systemDark) }) {
                        Icon(
                            painterResource(if (dark) R.drawable.ic_light_mode else R.drawable.ic_dark_mode),
                            contentDescription = "切换深色模式"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(painterResource(R.drawable.ic_calendar), contentDescription = null) },
                    label = { Text("记账") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(painterResource(R.drawable.ic_bar_chart), contentDescription = null) },
                    label = { Text("统计") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackHost) }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (tab) {
                0 -> HomeScreen(
                    state = vm.homeState.collectAsStateWithLifecycle().value,
                    onChangeMonth = vm::changeMonth,
                    onGoToday = vm::goToday,
                    onSelectDate = vm::selectDate,
                    onSave = vm::saveExpense,
                    onDelete = vm::deleteExpense
                )
                1 -> StatsScreen(
                    state = vm.statsState.collectAsStateWithLifecycle().value,
                    onChangeMonth = vm::changeStatsMonth,
                    onStepYear = vm::stepYear,
                    onOpenMonth = vm::openStatsMonth,
                    onPickDay = vm::pickStatsDay,
                    onExportCsv = {
                        vm.exportCsv { csv ->
                            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
                            val date = java.time.LocalDate.now().toString()
                            val csvFile = File(exportDir, "记账记录_$date.csv")
                            csvFile.writeText(csv, Charsets.UTF_8)
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                csvFile
                            )
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_SUBJECT, "记账记录_$date.csv")
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "导出记账记录"))
                        }
                    }
                )
            }
        }
    }

    if (showTheme) {
        AlertDialog(
            onDismissRequest = { showTheme = false },
            icon = { Icon(painterResource(R.drawable.ic_palette), contentDescription = null, tint = scheme.primary) },
            title = {
                Text(
                    "主题颜色",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                ThemeSwatches(
                    currentHue = theme.prefs.hue,
                    dark = dark,
                    dynamicColor = theme.prefs.dynamicColor,
                    onToggleDynamic = vm::setDynamicColor,
                    onSelect = { vm.setHue(it) }
                )
            },
            confirmButton = {
                TextButton(onClick = { showTheme = false }) { Text("完成") }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}
