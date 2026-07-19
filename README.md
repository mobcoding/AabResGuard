# AabResGuard

<h1 align="center">
  <img src="wiki/images/logo.png" height="220" width="460"/>
  <p align="center" style="font-size: 0.3em">面向 Android App Bundle（AAB）的资源混淆与体积优化工具</p>
</h1>

[![JitPack](https://jitpack.io/v/mobcoding/AabResGuard.svg)](https://jitpack.io/#mobcoding/AabResGuard)
[![License](https://img.shields.io/badge/license-Apache2.0-brightgreen)](LICENSE)
[![Bundletool](https://img.shields.io/badge/Dependency-Bundletool/1.18.3-blue)](https://github.com/google/bundletool)

> 原项目由字节跳动抖音 Android 团队提供。本 fork 维护于 [mobcoding/AabResGuard](https://github.com/mobcoding/AabResGuard)，已适配 AGP 9，并通过 JitPack 发布。

## 当前版本

当前发布版本为 `v0.1.15`，需要 JDK 17，兼容 AGP 9 公共 API。

- 支持 Gradle Configuration Cache。
- 保留 AGP 原始 AAB，不会覆盖 `app-release.aab`。
- 混淆后的 AAB、资源 mapping 与去重日志输出到 AGP Bundle 产物同级目录。
- 配置的历史 mapping 不存在时，按首次全量混淆处理。

## 功能

- **资源混淆：** 重命名资源目录、资源名称与资源文件路径。
- **资源去重：** 合并重复资源文件，减少 AAB 体积。
- **文件过滤：** 过滤 Bundle 中匹配规则的 `META-INF/` 或 `lib/` 文件。
- **资源白名单：** 指定不参与资源混淆的资源规则。
- **增量混淆：** 使用上一版本的 `resources-mapping.txt` 保持资源混淆结果稳定。
- **无用字符串过滤：** 按字符串名称或语言白名单移除资源字符串。
- **命令行工具：** 支持在 CI 或独立流程中直接处理 AAB。

## 快速接入

JitPack 坐标保留 Git Tag 的 `v` 前缀：

```text
com.github.mobcoding.AabResGuard:aabresguard-plugin:v0.1.15
```

### Groovy DSL

根工程 `build.gradle`：

```gradle
buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        classpath "com.github.mobcoding.AabResGuard:aabresguard-plugin:v0.1.15"
    }
}
```

应用模块 `build.gradle`：

```gradle
apply plugin: "com.bytedance.android.aabResGuard"

aabResGuard {
    obfuscatedBundleFileName = "app-resguard.aab"
    mergeDuplicatedRes = true
    enableFilterFiles = true
    filterList = ["META-INF/*", "*/arm64-v8a/*"]
}
```

### Kotlin DSL

JitPack 发布的是插件实现包，而非 Gradle Plugin Marker。请在 `settings.gradle.kts` 中映射插件 ID：

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://jitpack.io")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.bytedance.android.aabResGuard") {
                useModule(
                    "com.github.mobcoding.AabResGuard:aabresguard-plugin:v0.1.15",
                )
            }
        }
    }
}
```

应用模块 `build.gradle.kts`：

```kotlin
import com.bytedance.android.plugin.extensions.AabResGuardExtension

plugins {
    id("com.android.application")
    id("com.bytedance.android.aabResGuard")
}

configure<AabResGuardExtension> {
    obfuscatedBundleFileName = "app-resguard.aab"
    mergeDuplicatedRes = true
    enableFilterFiles = true
    filterList = setOf("META-INF/*", "*/arm64-v8a/*")
}
```

## 常用配置

```kotlin
configure<AabResGuardExtension> {
    // 上一版本归档的资源 mapping，用于增量混淆。
    mappingFile = file("aabresguard-mapping.txt").toPath()

    whiteList = setOf(
        "*.R.raw.*",
        "*.R.drawable.icon",
        "*.R.string.google_app_id",
    )
    obfuscatedBundleFileName = "app-resguard.aab"
    mergeDuplicatedRes = true

    enableFilterFiles = true
    filterList = setOf("META-INF/*", "*/arm64-v8a/*")

    enableFilterStrings = false
    unusedStringPath = file("unused.txt").toPath().toString()
    languageWhiteList = setOf("zh", "en")
}
```

`mappingFile` 是上一次构建的输入文件，不是本次输出路径。请将新生成的 `resources-mapping.txt` 归档到 Bundle 输出目录以外的位置，再在下一次构建中作为 `mappingFile` 使用。若输入路径不存在，插件会执行首次全量混淆。

`obfuscatedBundleFileName` 必须与原始 Bundle 文件名不同，例如不要设置为 `app-release.aab`。

## 构建、签名与输出

仍使用常规 Bundle 任务：

```shell
./gradlew :app:bundleRelease
```

`bundleRelease` 完成后会自动执行 `aabresguardRelease`。应用模块需要在 Android DSL 中配置正式签名；AabResGuard 会使用同一套签名信息重新签名混淆后的 AAB。

使用上述 `app-resguard.aab` 配置时，Release 输出为：

```text
app/build/outputs/bundle/release/
├── app-release.aab
├── app-resguard.aab
├── resources-mapping.txt
└── *-duplicated.txt
```

上传应用商店和 CI 分发时应使用 `app-resguard.aab`，并归档 `resources-mapping.txt` 以支持下一版本的增量混淆。

## 中文文档

- [中文概览](wiki/zh-cn/README.md)
- [资源白名单](wiki/zh-cn/WHITELIST.md)
- [命令行工具](wiki/zh-cn/COMMAND.md)
- [输出文件说明](wiki/zh-cn/OUTPUT.md)
- [体积优化数据](wiki/zh-cn/DATA.md)
- [版本记录](wiki/zh-cn/CHANGELOG.md)
- [贡献指南](wiki/zh-cn/CONTRIBUTOR.md)

## 致谢

- [AndResGuard](https://github.com/shwenzhang/AndResGuard/)
- [BundleTool](https://github.com/google/bundletool)
