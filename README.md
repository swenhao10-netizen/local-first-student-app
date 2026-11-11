# 知行

**记下今天的每一件小事。**

知行是一款本地优先的 Android 学生 App：把随手记、背单词、记账放进同一条个人时间轴。免费、不用注册、离线可用，所有数据只存在你自己的手机里。

👉 [下载最新版 APK](https://github.com/swenhao10-netizen/zhixing/releases/latest)（Android 8.0 及以上，覆盖安装不丢数据）

## 功能

**记录（首页时间轴）**
- 想到就发一条：文字（最多 500 字）、照片和视频（相册选、拍照或录像），点开全屏查看或播放
- 照片和视频另存在知行里，相册删了也不影响；可以单个、整条或全部存回相册
- 打开「记一笔账」，同一条记录里填分类和金额
- 背单词会自动在时间轴记一条

**单词**
- 内置大学英语四六级词汇 5805 个（ECDICT），一词一卡
- 先想再看：点「认识 / 不认识」后才显示释义，可继续上次进度或从头开始
- 「不认识」的词自动收进「不记得的单词」单独复习
- 「我的卡片」：自己加的词，可删除

**总览**
- 本月结余、收入、支出
- 学习天数、记录条数、背过的单词数
- 按分类的支出统计，月历标出有记录的日子

**我的**
- 头像、昵称（只用本机图片）
- 主题颜色、液态玻璃主题、自定义背景
- 背单词 / 记账每日提醒
- 导出本地备份、从备份恢复
- 导出给 AI：把记录整理成带说明的文字，分享或粘贴给任意 AI，让它快速了解你

## 隐私

- 记录、单词、账目都只保存在这台手机，不上传
- 不需要账号，没有广告、订阅、内购、排行榜，不做默认数据收集
- 卸载 App 会删除本机数据，换机或卸载前请先导出备份

## 技术栈

Kotlin 2.2 · Jetpack Compose（Material 3）· Room · WorkManager · minSdk 26 / compileSdk 36

液态玻璃效果使用 [Kyant0/AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass)（backdrop 1.0.0，Apache 2.0），底栏与按钮组件移植自其示例，见 `app/src/main/java/com/swenhao10/localfirst/ui/liquid/`。模糊需 Android 12+，折射需 Android 13+，低版本自动退化为半透明。

## 构建

```bash
./gradlew assembleDebug
```

需要 JDK 21。推送任意分支，GitHub Actions 会自动打包并发布预览版；推送 `v*` 标签会发布正式版（更新说明放在 `release-notes/<标签>.md`）。

APK 使用仓库内固定的 debug 签名（`keystore/zhixing-debug.jks`），保证每次打包都能覆盖安装。

## 目录

```
app/                     Android 应用
  src/main/assets/packs/ 内置词包（SQLite）
packs/cet46/             词包来源与许可证说明
release-notes/           各版本更新说明
keystore/                debug 签名
```

## 致谢

- 词库：[ECDICT](https://github.com/skywind3000/ECDICT)（MIT）
- 液态玻璃：[AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass)（Apache 2.0）
