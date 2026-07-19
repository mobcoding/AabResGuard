# AabResGuard 中文文档

AabResGuard 用于在 Android App Bundle（AAB）构建完成后执行资源混淆、资源去重和文件过滤。

当前版本为 `v0.1.15`，适配 AGP 9，要求 JDK 17。常规 `bundle<Variant>` 任务完成后会自动执行对应的 `aabresguard<Variant>` 任务，原始 AAB 会被保留。

## 文档索引

- [项目概览与 Gradle 接入](../../README.md)
- [资源白名单](WHITELIST.md)
- [命令行工具](COMMAND.md)
- [输出文件说明](OUTPUT.md)
- [体积优化数据](DATA.md)
- [版本记录](CHANGELOG.md)
- [贡献指南](CONTRIBUTOR.md)

## 构建产物

混淆后的 AAB、`resources-mapping.txt` 和去重日志会输出到 `build/outputs/bundle/<variant>/`，与 AGP Bundle 产物同级。请使用混淆后的 AAB 进行分发，并将 `resources-mapping.txt` 归档到输出目录以外的位置，以便下一版本进行增量混淆。
