# 资源白名单

`whiteList` 用于声明不参与资源混淆的资源规则。规则匹配资源的完整 R 类路径，支持通配符。

```kotlin
configure<AabResGuardExtension> {
    whiteList = setOf(
        "*.R.raw.*",
        "*.R.drawable.icon",
        "*.R.string.google_app_id",
    )
}
```

## Google 服务常用规则

以下资源通常由 Google 服务、Firebase 或 Crashlytics 在运行时读取，建议按实际依赖加入白名单：

```text
*.R.string.default_web_client_id
*.R.string.firebase_database_url
*.R.string.gcm_defaultSenderId
*.R.string.google_api_key
*.R.string.google_app_id
*.R.string.google_crash_reporting_api_key
*.R.string.google_storage_bucket
*.R.string.project_id
*.R.string.com.crashlytics.android.build_id
```

仅保留应用确实使用的规则。接入新的 SDK 后，若其通过资源名称、反射或配置约定访问资源，也应将对应资源加入白名单。
