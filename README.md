# MD3 Accounting（简约记账）

Material Design 3 风格的简约记账 Android App，使用 **Kotlin + Jetpack Compose** 实现，功能与网页版一致。

## 功能

- **记账页**
  - 当月总支出卡片
  - 日历视图：每日支出预览、选中高亮、今天描边
  - 当日支出列表（首字圆形头像）
  - 添加支出表单（事项 + 金额）
  - 删除记录 + Snackbar 撤销
- **统计页**
  - 按日柱状图（可点击跳转日期）
  - 按月柱状图（可点击跳转月份）
- **主题**
  - 8 种主题色（紫 / 蓝 / 青 / 绿 / 金 / 橙 / 红 / 粉）
  - 深色模式切换
- **数据**
  - DataStore + JSON 本地持久化
  - 首次启动自动生成去年至今的示例数据（固定随机种子，与网页版逻辑一致）

## 技术栈

| 库 | 用途 |
|---|---|
| Jetpack Compose + Material 3 | UI |
| Navigation（底部栏手写） | 页面切换 |
| DataStore Preferences | 持久化 |
| Kotlinx Serialization | JSON |
| ViewModel + StateFlow | 状态管理 |

## 环境要求

- Android Studio Hedgehog (2023.1.1) 或更新版本
- JDK 17
- minSdk 26 / targetSdk 35

## 运行

```bash
git clone https://github.com/WChaoQ/md3-accounting.git
cd md3-accounting
# 用 Android Studio 打开，Sync Gradle 后 Run
```

## License

MIT
