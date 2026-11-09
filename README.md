# 知行

当前由项目负责人直接统筹产品、设计与 Android 实现，外部协作者的文档仅作为参考输入。

知行是一款 Android 本地优先 App，首版包含三个模块：背单词 / 学习卡片、习惯·专注、轻量记账。

## 当前实现

- Kotlin + Jetpack Compose + Room
- 无账号，本地 SQLite 数据库
- 三个模块的最小记录闭环
- Material 3 视觉系统：青绿色主色、卡片分组、底部导航和轻量动效
- “我的数据”支持 JSON 导入、导出和备份提示
- 写入操作放在后台线程，避免点击保存时卡顿
- 自定义“知行”书本 + 向上箭头 App 图标
- 内置词库内容只使用原创示例；考试词库需要单独核验授权

## 开发状态

当前是 Android 内部测试版。复习排期、考试词库授权、完整冲突处理、提醒和正式签名发布仍需继续实现。请阅读 `项目总览与决策清单.md` 与 `词库内容方案-首版任务.md`。

## UI 参考

视觉和交互采用 Jetpack Compose Material 3 的成熟模式，参考了 Android 官方的 [Now in Android](https://github.com/android/nowinandroid) 与 [Compose Samples](https://github.com/android/compose-samples)，并按“知行”的本地优先定位重新组合。

## GitHub

https://github.com/swenhao10-netizen/local-first-student-app

