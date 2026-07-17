[English](../en/CHANGELOG.md) | **[简体中文](CHANGELOG.md)**

# 版本日志
## v0.1.15（2026/07/18）
- 避免在 `sign<Variant>Bundle` 完成前查询 AGP Bundle 产物 Provider。
- 使用确定性的 variant Bundle 输出目录，继续保持官网同级输出语义。

## v0.1.14（2026/07/18）
- 恢复官方输出行为：混淆后的 AAB 与日志文件输出到 AGP Bundle 产物同级目录。
- 保留原始 Bundle，只清理旧的 AabResGuard 产物。
- 配置的 mapping 文件不存在时按首次全量混淆处理，与上游插件行为一致。

## v0.1.13（2026/07/17）
- 以 `com.github.mobcoding.AabResGuard` 坐标发布 AGP 9 版本到 JitPack。
- 版本号移除 `-agp9` 后缀。
- 将扩展配置和签名信息改为 Gradle Task Property，兼容 Configuration Cache。

## 0.1.6（2020/4/21）
- 适配 `AGP-3.5.0`
- 修复获取 `AGP` 版本号失败的问题

## 0.1.5（2020/4/5）
- 适配 `AGP-4.0.0-alpha09`
- 给插件添加 `enableObfuscate` 参数

## 0.1.3（2020/1/8）
- 适配 `AGP-3.5.2`

## 0.1.2（2020/1/7）
- 适配 `AGP-4.0.0-alpha07`
-  Fix issue [#13](https://github.com/bytedance/AabResGuard/issues/13)

## 0.1.1（2019/11/26）
- 适配 `AGP-3.4.1`
- Fix issue [#4](https://github.com/bytedance/AabResGuard/issues/4)

## 0.1.0（2019/10/16）
- 添加资源混淆功能
- 添加资源去重功能
- 添加文件过滤功能
- 添加字符串过滤功能
- 添加 `gradle plugin` 的支持
- 添加命令行支持
