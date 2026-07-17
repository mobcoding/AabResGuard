**[English](CHANGELOG.md)** | [简体中文](../zh-cn/CHANGELOG.md)

# Change log
## v0.1.15 (2026/07/18)
- Avoid querying the AGP Bundle artifact provider before `sign<Variant>Bundle` completes.
- Keep official Bundle-directory output semantics through the deterministic variant output directory.

## v0.1.14 (2026/07/18)
- Restore the official behavior: the obfuscated AAB and sidecar logs are emitted next to the AGP Bundle output.
- Preserve the original Bundle output while cleaning only stale AabResGuard artifacts.
- Treat a missing configured mapping file as a first full obfuscation, matching the upstream plugin behavior.

## v0.1.13 (2026/07/17)
- Publish the AGP 9 fork to JitPack under `com.github.mobcoding.AabResGuard`.
- Remove the `-agp9` suffix from the release version.
- Make the Gradle task compatible with Configuration Cache by passing extension and signing values as task properties.

## 0.1.6（2020/4/21）
- Compatible wit `AGP-3.5.0`
- Bugfix: `Fix get AGP version failed issue`

## 0.1.5（2020/4/5）
- Compatible with `AGP-4.0.0-alpha09`
- Add `enableObfuscate` for plugin extension.

## 0.1.3（2020/1/8）
- Compatible with `AGP-3.5.2`

## 0.1.2（2020/1/7）
- Compatible with `AGP-4.0.0-alpha07`
-  Fix issue [#13](https://github.com/bytedance/AabResGuard/issues/13)

## 0.1.1（2019/11/26）
- Compatible with `AGP-3.4.1.`
- Fix issue [#4](https://github.com/bytedance/AabResGuard/issues/4)

## 0.1.0（2019/10/16）
- Add support for resources obfuscation.
- Add support for merge duplicated resources.
- Add support for files filtering.
- Add support for string filtering.
- Added support for `gradle plugin` .
- Add support for `command line` .
