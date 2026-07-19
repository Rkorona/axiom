# 西幻跑团文字冒险游戏 — 风暴斗篷酒馆

## 项目概述

这是一个 Android（Kotlin + Jetpack Compose）应用，实现了西幻 TTRPG 文字冒险游戏的主界面（安全区：风暴斗篷酒馆）。采用 **Material 3 Expressive** 设计语言，具有沉浸式的暗黑黄金西幻美学风格。

## 技术栈

- **语言**：Kotlin
- **UI 框架**：Jetpack Compose + Material 3
- **最低 SDK**：30（Android 11）
- **目标 SDK**：37
- **包名**：`com.example.myapplication`

## 项目结构

```
app/src/main/java/com/example/myapplication/
├── MainActivity.kt                        # 程序入口
├── data/
│   └── GameModels.kt                      # 数据模型（角色、时间、任务）
├── ui/
│   ├── theme/
│   │   ├── Color.kt                       # 西幻暗黑黄金主题色
│   │   ├── Theme.kt                       # MaterialTheme 配置
│   │   └── Type.kt                        # 字体排版
│   ├── components/
│   │   ├── RunicShapes.kt                 # 自定义八角形/切角形状
│   │   ├── AmbientGlow.kt                 # 壁炉篝火动态光晕背景
│   │   ├── CharacterHeader.kt             # 角色状态条（HP/MP/金币）
│   │   ├── NarrativeSection.kt            # 随时间变化的氛围叙事文本
│   │   ├── InteractiveNodes.kt            # 酒馆/铁匠铺/神殿折叠交互卡片
│   │   ├── BountyBoard.kt                 # 悬赏布告栏任务系统
│   │   └── EmbarkButton.kt                # 八角形脉冲光晕出征按钮
│   └── screen/
│       └── TownHubScreen.kt               # 主页面整合
```

## 构建方式

使用 Android Studio 打开此项目并构建，或使用 Gradle：

```bash
./gradlew assembleDebug
```

需要 Android SDK（通过 Android Studio 安装）。

## 功能特性

- **时间系统**：清晨 / 正午 / 黄昏 / 深夜，长休可推进时间
- **角色面板**：等级徽章、HP/MP 状态槽、金币显示
- **酒馆交互**：长休回满（15🪙）、购买药水（20🪙）、驱散诅咒（30🪙）
- **布告栏**：可揭下的悬赏任务，带难度标记
- **出征按钮**：脉冲光晕八角形大按钮

## User Preferences

- 项目语言：中文
- 设计风格：西幻 / Material 3 Expressive / 暗黑黄金主题
