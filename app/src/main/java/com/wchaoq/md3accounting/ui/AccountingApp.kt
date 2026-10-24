package com.wchaoq.md3accounting.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import java.io.File
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    var showDataMenu by rememberSaveable { mutableStateOf(false) }
    var showAbout by rememberSaveable { mutableStateOf(false) }
    var showDeleteAllDialog by rememberSaveable { mutableStateOf(false) }
    val snackHost = remember { SnackbarHostState() }
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            }.onSuccess { csv ->
                if (csv != null) vm.importCsv(csv)
            }.onFailure {
                android.widget.Toast.makeText(context, "无法读取 CSV 文件", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            vm.exportCsv { csv ->
                scope.launch(Dispatchers.IO) {
                    val result = runCatching {
                        val output = context.contentResolver.openOutputStream(uri) ?: error("无法创建文件")
                        output.bufferedWriter(Charsets.UTF_8).use { it.write(csv) }
                    }
                    if (result.isFailure) withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(context, "导出失败，请重试", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Remove CSVs left by older versions; new exports go straight to the chosen document.
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) { File(context.cacheDir, "exports").deleteRecursively() }
    }

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
                    Row(
                        modifier = Modifier.clickable { showAbout = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painterResource(if (tab == 0) R.drawable.ic_calendar else R.drawable.ic_stats),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(if (tab == 0) "记录" else "统计")
                    }
                },
                actions = {
                    IconButton(onClick = { showTheme = true }) {
                        Icon(painterResource(R.drawable.ic_palette), contentDescription = "主题颜色")
                    }
                    IconButton(onClick = { vm.toggleDark(systemDark) }) {
                        Icon(
                            painterResource(if (dark) R.drawable.ic_light_mode else R.drawable.ic_dark_mode),
                            contentDescription = if (dark) "切换浅色模式" else "切换深色模式"
                        )
                    }
                    Box {
                        IconButton(onClick = { showDataMenu = true }) {
                            Icon(painterResource(R.drawable.ic_data), contentDescription = "数据管理")
                        }
                        DropdownMenu(
                            expanded = showDataMenu,
                            onDismissRequest = { showDataMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("导入 CSV") },
                                leadingIcon = { Icon(painterResource(R.drawable.ic_import), contentDescription = null) },
                                onClick = { showDataMenu = false; importLauncher.launch("text/*") }
                            )
                            DropdownMenuItem(
                                text = { Text("导出 CSV") },
                                leadingIcon = { Icon(painterResource(R.drawable.ic_export), contentDescription = null) },
                                onClick = { showDataMenu = false; exportLauncher.launch("支出记录.csv") }
                            )
                            DropdownMenuItem(
                                text = { Text("删除所有数据", color = scheme.error) },
                                leadingIcon = { Icon(painterResource(R.drawable.ic_delete), contentDescription = null, tint = scheme.error) },
                                onClick = { showDataMenu = false; showDeleteAllDialog = true }
                            )
                        }
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
                    label = { Text("记录") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(painterResource(R.drawable.ic_stats), contentDescription = null) },
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
                    onSelectDate = vm::selectDate,
                    onSave = vm::saveExpense,
                    onDelete = vm::deleteExpense
                )
                1 -> StatsScreen(
                    state = vm.statsState.collectAsStateWithLifecycle().value,
                    onChangeMonth = vm::changeStatsMonth,
                    onStepYear = vm::stepYear,
                    onOpenMonth = vm::openStatsMonth,
                    onPickDay = vm::pickStatsDay
                )
            }
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_ai),
                    contentDescription = null,
                    tint = scheme.primary
                )
            },
            title = { Text("支出记录") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    Text("由 GPT、Grok、Gemini、Claude 制作", color = scheme.onSurface)
                    Text("制作日期：2026-10-24", color = scheme.onSurfaceVariant)
                    Text("版本：1.0.5（6）", color = scheme.onSurfaceVariant)
                    Text(
                        "wchaoq",
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        color = scheme.onSurfaceVariant.copy(alpha = 0.55f),
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("知道了") }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            icon = { Icon(painterResource(R.drawable.ic_delete), contentDescription = null, tint = scheme.error) },
            title = { Text("删除所有数据？") },
            text = { Text("此操作会永久删除全部支出记录，且无法撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteAllDialog = false
                    vm.deleteAllExpenses()
                }) { Text("删除全部", color = scheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteAllDialog = false }) { Text("取消") } },
            shape = RoundedCornerShape(28.dp)
        )
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
