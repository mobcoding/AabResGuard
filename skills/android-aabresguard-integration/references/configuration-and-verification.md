# Configuration And Verification

## Remote Plugin Resolution

Use the published AabResGuard implementation, not a Gradle Plugin Marker artifact or a local plugin JAR:

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
                    "com.github.mobcoding.AabResGuard:aabresguard-plugin:v0.1.17"
                )
            }
        }
    }
}
```

Declare the plugin in the app module with the project's existing plugin convention. A version-catalog alias may use the ID `com.bytedance.android.aabResGuard` and version `v0.1.17` when the mapping above is present.

## Minimal Kotlin DSL Template

Use this only when no existing extension block exists:

```kotlin
import com.bytedance.android.plugin.extensions.AabResGuardExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.aab.resguard)
}

configure<AabResGuardExtension> {
    mappingFile = file("mapping.txt").toPath()
    whiteList = setOf(
        "*.R.raw.*",
        "*.R.drawable.icon"
    )
    obfuscatedBundleFileName = "app_build.aab"
    mergeDuplicatedRes = true
    enableFilterFiles = true
    filterList = setOf("META-INF/*")
    enableFilterStrings = false
    unusedStringPath = file("unused.txt").toPath().toString()
    languageWhiteList = setOf("en", "zh")
}
```

`mappingFile` is a previous release input. Store the generated `resources-mapping.txt`, copy it to the next release's `mapping.txt`, and keep that lifecycle explicit in release documentation.

## Output Expectations

For a `release` variant, invoke:

```text
./gradlew aabresguardRelease
```

The plugin runs after the Release Bundle is signed. The original AGP AAB, configured obfuscated AAB, and `resources-mapping.txt` are expected under the app module's Bundle output directory unless the project already defines a custom archive task.

Use the configured obfuscated AAB for distribution and archive `resources-mapping.txt` with the release artifacts.

## Known Compatibility Issues

- `v0.1.15` queried a mapped Bundle Provider too early in some builds. Use the current `v0.1.17` rather than adding task-order patches.
- `v0.1.16` fixed immutable Gradle collection handling for file filters and removed unsafe default ARM64 filtering.
- `v0.1.17` fixes metadata filtering when Bundletool exposes relative paths. A `BUNDLE-METADATA/*` filter rule now matches correctly when explicitly configured.

## Verification Commands

```powershell
Get-ChildItem app\build\outputs\bundle\release -Include *.aab -Recurse
jar tf <obfuscated-aab> | Select-String '^BUNDLE-METADATA/'
```

Only assert that metadata is absent when the project's configured `filterList` includes `BUNDLE-METADATA/*`. Otherwise its presence is expected.
