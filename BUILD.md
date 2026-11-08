# Android 构建说明

当前仓库已经有 Android 工程骨架，但还没有提交 Gradle Wrapper。请在安装 Android Studio / Android SDK 后使用 TRAE 或 Android Studio 打开仓库根目录，让 IDE 生成或同步 Gradle Wrapper。

## 当前目标

- `app`：Android 应用模块
- Kotlin + Jetpack Compose + Room
- 最低 Android 8.0（API 26）
- 本地数据库：`student-local.db`
- 不申请账号、网络或后台遥测权限

## 已实现

- 卡片、习惯·专注、记账、我的数据四个入口
- 三个模块的本地新增和列表展示
- Room 本地数据库
- 无账号的本地优先信息提示

## 尚未实现

- CSV / JSON / `.wbpack` 导入
- 可读导出、完整备份、恢复和撤销
- 真正的间隔复习排期
- 本地提醒
- 习惯计时、统计和记账分类
- Android 真机验证

开发时请先实现“我的数据”导入 / 导出基础能力，再扩展三个模块的完整业务流程。

