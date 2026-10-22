# MD3 Accounting（简约记账）

Material Design 3 风格的简约记账 Android App，使用 **Kotlin + Jetpack Compose** 实现，功能与网页版一致。

## 快速开始

### 方式 A：GitHub Codespaces（推荐）

1. 打开仓库：https://github.com/WChaoQ/md3-accounting
2. 点击绿色 **Code** → **Codespaces** → **Create codespace on main**
3. 等待环境初始化（首次会安装 Android SDK，约 3–5 分钟）
4. 在终端执行：

```bash
./gradlew :app:assembleDebug
```

成功后 APK 位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

> 建议选 **4-core / 8GB** 机型，否则构建可能较慢或 OOM。

### 方式 B：GitHub Actions（云端自动构建）

1. 打开 **Actions** 标签
2. 选择 **Android CI** → **Run workflow**
3. 构建完成后，在该次 run 的 **Artifacts** 中下载 `app-debug`

推送到 `main` 也会自动触发构建。

### 方式 C：本地 Android Studio

1. Clone 仓库
2. 用 Android Studio Hedgehog+ 打开
3. Sync Gradle 后 Run

## 功能

- 记账页：月总支出、日历、当日列表、添加/删除（可撤销）
- 统计页：按日/按月柱状图
- 8 种主题色 + 深色模式
- DataStore 本地持久化 + 首次示例数据

## 技术栈

Kotlin · Jetpack Compose · Material 3 · DataStore · Kotlinx Serialization

## License

MIT
