# 知行

当前由项目负责人直接统筹产品、设计与 Android 实现，外部协作者的文档仅作为参考输入。

知行是一款 Android 本地优先 App，首版包含三个模块：背单词 / 学习卡片、习惯·专注、轻量记账。

## 当前实现

- Kotlin + Jetpack Compose + Room
- 无账号，本地 SQLite 数据库
- 三个模块的最小记录闭环
- “我的数据”入口已预留导入、导出、备份、恢复和隐私说明
- 内置词库内容只使用原创示例；考试词库需要单独核验授权

## 开发状态

当前是 Android 工程骨架和最小闭环，导入、导出、备份、恢复、复习排期和高保真设计仍需继续实现。请阅读 `项目总览与决策清单.md` 与 `词库内容方案-首版任务.md`。

## GitHub

https://github.com/swenhao10-netizen/local-first-student-app

