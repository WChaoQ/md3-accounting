# MD3 Accounting（简约记账）

Material Design 3 风格的简约记账 Android App（Kotlin + Jetpack Compose）。

## 功能

- **记账页**：月总览、日历、当日列表、添加/删除
- **统计页**：日/月 Canvas 柱状图（按触摸 x 选中）
- **主题**：8 种色相 + 深色模式（首次跟随系统）
- **数据**：Room（支出，金额单位：分）+ DataStore（主题）

## 技术栈

| 库 | 用途 |
|---|---|
| Compose + Material 3 | UI |
| Room | 支出持久化与 SQL 聚合 |
| DataStore Preferences | 主题 |
| ViewModel + StateFlow | 状态 |
| SplashScreen | 启动至主题就绪 |
| Version Catalog | 依赖版本集中管理 |

图标使用本地 `vector drawable`，不依赖 `material-icons-extended`。

## 环境

- JDK 17 / minSdk 26 / targetSdk 35

## 下载与安装

- 每次 `gpt` 分支构建成功后，GitHub Releases 会发布一个可安装的测试版 APK；构建会安装 Android SDK command-line tools，并包含单元测试、Debug/Release APK 组装、APK 签名校验和包名/版本校验。
- 下载文件名为 `app-release.apk`，下载后在 Android 系统中允许浏览器/文件管理器安装未知来源应用，再打开 APK 安装。
- 当前 CI 版本使用 Android debug 签名以便侧载安装；这不是 Google Play 正式发布签名。不同 CI 构建使用的签名可能不同，更新时若提示签名不一致，需要先卸载旧版（先备份重要数据）。
- 版本号：`1.0.1`（versionCode `2`）。

## 运行

```bash
git clone https://github.com/WChaoQ/md3-accounting.git
cd md3-accounting
./gradlew :app:assembleDebug
```

## 项目结构

```
com.wchaoq.md3accounting/
├── MainActivity.kt, AccountingViewModel.kt
├── data/
│   ├── local/          # Room
│   ├── ThemeStore.kt
│   ├── ExpenseRepository.kt
│   └── Models.kt
├── ui/
│   ├── AccountingApp.kt
│   ├── home/  stats/  components/  theme/
└── util/               # Money, DateInt
```

## License

MIT
