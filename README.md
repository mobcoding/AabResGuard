# AabResGuard

<h1 align="center">
  <img src="wiki/images/logo.png" height="220" width="460"/>
  <p align="center" style="font-size: 0.3em">面向 Android App Bundle（AAB）的资源混淆工具</p>
</h1>

[![JitPack](https://jitpack.io/v/mobcoding/AabResGuard.svg)](https://jitpack.io/#mobcoding/AabResGuard)
[![License](https://img.shields.io/badge/license-Apache2.0-brightgreen)](LICENSE)
[![Bundletool](https://img.shields.io/badge/Dependency-Bundletool/1.18.3-blue)](https://github.com/google/bundletool)

**简体中文** | [English documentation](wiki/en/COMMAND.md)

> 原项目由字节跳动抖音 Android 团队提供。本 fork 维护于 [mobcoding/AabResGuard](https://github.com/mobcoding/AabResGuard)，适配 AGP 9，并通过 JitPack 发布。

## 功能

- **资源去重：** 合并重复资源文件，减小 AAB 体积。
- **文件过滤：** 支持过滤 Bundle 中不需要的文件。
- **白名单：** 白名单中的资源不会被混淆。
- **增量混淆：** 使用上一版 `resources-mapping.txt` 保持资源混淆结果稳定。
- **无用字符串过滤：** 可根据无用字符串列表移除资源字符串。

## 快速接入

当前版本为 `v0.1.15`。坐标保留 Git Tag 的 `v` 前缀，不再使用 `-agp9` 后缀。

### Groovy DSL

在根目录 `build.gradle` 中添加：

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

在应用模块中应用插件：

```gradle
apply plugin: "com.bytedance.android.aabResGuard"

aabResGuard {
    mappingFile = file("mapping.txt").toPath() // 用于增量混淆的 mapping 文件
    whiteList = [ // 白名单规则
        "*.R.raw.*",
        "*.R.drawable.icon"
    ]
    obfuscatedBundleFileName = "duplicated-app.aab" // 混淆后的文件名称，必须以 `.aab` 结尾
    mergeDuplicatedRes = true // 是否允许去除重复资源
    enableFilterFiles = true // 是否允许过滤文件
    filterList = [ // 文件过滤规则
        "*/arm64-v8a/*",
        "META-INF/*"
    ]
    enableFilterStrings = false // 过滤文案
    unusedStringPath = file("unused.txt").toPath() // 过滤文案列表路径，默认在 mapping 同目录查找
    languageWhiteList = ["en", "zh"] // 保留 en、en-xx、zh、zh-xx 等语言，其余均删除
}
```

### Kotlin DSL

JitPack 发布的是插件实现包，而不是 Gradle Plugin Marker 包。因此在 `settings.gradle.kts` 中将插件 ID 映射到实际坐标：

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
                    "com.github.mobcoding.AabResGuard:aabresguard-plugin:v0.1.15"
                )
            }
        }
    }
}
```

在应用模块的 `build.gradle.kts` 中：

```kotlin
import com.bytedance.android.plugin.extensions.AabResGuardExtension

plugins {
    id("com.android.application")
    id("com.bytedance.android.aabResGuard")
}

configure<AabResGuardExtension> {
    mappingFile = file("mapping.txt").toPath()
    whiteList = setOf(
        "*.R.raw.*",
        "*.R.drawable.icon"
    )
    obfuscatedBundleFileName = "duplicated-app.aab"
    mergeDuplicatedRes = true
    enableFilterFiles = true
    filterList = setOf(
        "*/arm64-v8a/*",
        "META-INF/*"
    )
    enableFilterStrings = false
    unusedStringPath = file("unused.txt").toPath().toString()
    languageWhiteList = setOf("en", "zh")
}
```

`mappingFile` 是上一次构建的输入文件，不是本次输出路径。请将新生成的 `resources-mapping.txt` 归档后复制为模块目录下的 `mapping.txt`，再在下一次构建中作为输入使用。

## 构建与输出

执行：

```shell
./gradlew bundleRelease
```

插件会在 Bundle 构建结束后执行 `aabresguardRelease`。默认输出位置为：

```text
app/build/outputs/bundle/release/
├── app-release.aab
├── duplicated-app.aab
└── resources-mapping.txt
```

上传应用商店时使用混淆后的 AAB；同时归档 `resources-mapping.txt`，以支持后续版本的增量混淆。

## 文档

- [资源白名单（英文）](wiki/en/WHITELIST.md)
- [命令行工具](wiki/zh-cn/COMMAND.md)
- [输出文件说明](wiki/zh-cn/OUTPUT.md)
- [体积优化数据](wiki/zh-cn/DATA.md)
- [版本记录](wiki/zh-cn/CHANGELOG.md)
- [贡献指南](wiki/zh-cn/CONTRIBUTOR.md)
- [英文命令行文档](wiki/en/COMMAND.md)

## 致谢

- [AndResGuard](https://github.com/shwenzhang/AndResGuard/)
- [BundleTool](https://github.com/google/bundletool)
