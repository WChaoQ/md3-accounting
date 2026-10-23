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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.rememberDatePickerState
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
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
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
    var showExportOptions by rememberSaveable { mutableStateOf(false) }
    var showDataMenu by rememberSaveable { mutableStateOf(false) }
    var showAbout by rememberSaveable { mutableStateOf(false) }
    var dateDialog by rememberSaveable { mutableIntStateOf(0) }
    var exportStart by remember { mutableStateOf<LocalDate?>(null) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = LocalDate.now().atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli())
    val snackHost = remember { SnackbarHostState() }
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current

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

    fun exportCsv(start: LocalDate? = null, end: LocalDate? = null) {
        vm.exportCsv(start, end) { csv ->
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val suffix = if (start == null || end == null) "全部" else "${start}_${end}"
            val csvFile = File(exportDir, "记账记录_$suffix.csv")
            csvFile.writeText(csv, Charsets.UTF_8)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", csvFile)
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, csvFile.name)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(sendIntent, "导出记账记录"))
        }
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
                            painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(if (tab == 0) "支出" else "统计")
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
                                onClick = { showDataMenu = false; showExportOptions = true }
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
                    label = { Text("支出") }
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
                    painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    tint = scheme.onSurface
                )
            },
            title = { Text("支出") },
            text = {
                Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    Text("简洁、轻量的个人支出记录与统计工具。", color = scheme.onSurface)
                    Text("开发者：wchaoq", color = scheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painterResource(R.drawable.ic_ai),
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.size(8.dp))
                        Text("AI 协助：ChatGPT", color = scheme.onSurfaceVariant)
                    }
                    Text("界面设计：Material 3", color = scheme.onSurfaceVariant)
                    Text("版本 1.0.1", color = scheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("知道了") }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showExportOptions) {
        AlertDialog(
            onDismissRequest = { showExportOptions = false },
            title = { Text("导出 CSV") },
            text = { Text("选择导出全部记录，或指定日期范围。") },
            confirmButton = {
                TextButton(onClick = {
                    showExportOptions = false
                    exportStart = null
                    exportCsv()
                }) { Text("全部导出") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportOptions = false
                    datePickerState.selectedDateMillis = LocalDate.now().atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
                    dateDialog = 1
                }) { Text("选择日期范围") }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (dateDialog != 0) {
        DatePickerDialog(
            onDismissRequest = { dateDialog = 0 },
            confirmButton = {
                TextButton(onClick = {
                    val picked = datePickerState.selectedDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate()
                    }
                    if (picked != null && dateDialog == 1) {
                        exportStart = picked
                        datePickerState.selectedDateMillis = picked.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
                        dateDialog = 2
                    } else if (picked != null) {
                        val startDate = exportStart
                        dateDialog = 0
                        if (startDate != null && !picked.isBefore(startDate)) exportCsv(startDate, picked)
                        else android.widget.Toast.makeText(context, "结束日期不能早于开始日期", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }) { Text(if (dateDialog == 1) "下一步" else "导出") }
            },
            dismissButton = { TextButton(onClick = { dateDialog = 0 }) { Text("取消") } }
        ) {
            DatePicker(state = datePickerState, title = { Text(if (dateDialog == 1) "选择开始日期" else "选择结束日期") })
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
