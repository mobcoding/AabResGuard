---
name: android-aabresguard-integration
description: Integrate, migrate, validate, or troubleshoot mobcoding AabResGuard in an Android application. Use when a project needs AAB resource obfuscation, migration from a local AabResGuard JAR, preservation of existing AabResGuardExtension settings, mapping-file handling, or verification of the obfuscated AAB output.
---

# Android AabResGuard Integration

Preserve the consuming project's existing behavior. Inspect its configuration before adding, upgrading, or migrating AabResGuard.

Read [references/configuration-and-verification.md](references/configuration-and-verification.md) before editing Gradle files.

## Workflow

1. Audit the existing project first.
   - Read `settings.gradle*`, version catalogs, root and app build scripts, existing `AabResGuardExtension` blocks, `mapping.txt`, `unused.txt`, custom archive tasks, and local plugin JAR references.
   - Record existing white lists, language white lists, file/string filters, output names, mapping paths, signing, and downstream task dependencies.

2. Preserve existing intent.
   - Retain existing extension values and custom output/archiving tasks unless they are broken or the user requests a change.
   - Keep `obfuscatedBundleFileName` at its default value, `app_build.aab`. Do not rename the obfuscated AAB or introduce an alternative output name.
   - Use the minimal template only when no AabResGuard configuration exists.
   - Do not copy another project's whitelist, language list, filter rules, signing configuration, or output naming convention.

3. Configure the remote plugin.
   - Use `v0.1.17` and map `com.bytedance.android.aabResGuard` through `pluginManagement.resolutionStrategy` to the JitPack module.
   - Apply the plugin only to the application module that creates the target AAB.
   - Replace an obsolete local `plugin-*.jar` only after the remote plugin resolves and the existing extension configuration is retained.

4. Handle mapping and filters correctly.
   - Treat module `mapping.txt` as input from a previous release. Treat generated `resources-mapping.txt` as output to archive for the next release.
   - Never enable an ARM64 filter by default; it can remove device support.
   - Use `BUNDLE-METADATA/*` only when the project explicitly wants it filtered. Version `v0.1.17` normalizes Bundletool relative metadata paths before matching this rule.

5. Build and verify.
   - Run the requested variant's `aabresguard<Variant>` task or its existing wrapper task.
   - Verify the obfuscated AAB name and location, `resources-mapping.txt`, existing archive/copy tasks, and any enabled filter results.
   - Keep the original AGP bundle unless the project's existing behavior explicitly replaces it.

## Guardrails

- Do not query AGP's final Bundle Provider before `sign<Variant>Bundle` completes. Use the plugin's task/provider flow rather than eager task-time reads.
- Do not add a ZIP rewrite, re-signing task, or `BUNDLE-METADATA` workaround when `v0.1.17` is in use and the plugin configuration is correct.
- Keep `obfuscatedBundleFileName = "app_build.aab"`; it is the fixed default output name for this integration.
- Preserve unrelated StringFog, Kotlin, KSP/Room, resource, signing, and deployment configuration.
- Treat AGP compatibility as a build-time check. If the project differs materially from the documented AGP 9 target, configure first and stop on API incompatibility rather than guessing a workaround.
