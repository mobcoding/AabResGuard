package com.bytedance.android.plugin

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.bytedance.android.plugin.extensions.AabResGuardExtension
import com.bytedance.android.plugin.internal.resolveSigningConfig
import com.bytedance.android.plugin.tasks.AabResGuardTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class AabResGuardPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        checkApplicationPlugin(project)
        project.extensions.create("aabResGuard", AabResGuardExtension::class.java)

        val android = project.extensions.getByType(ApplicationExtension::class.java)
        val androidComponents = project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)

        androidComponents.onVariants { variant ->
            val variantName = variant.name.replaceFirstChar { it.uppercase() }
            val taskName = "aabresguard$variantName"
            val bundleTaskName = "bundle$variantName"

            val extension = project.extensions.getByType(AabResGuardExtension::class.java)
            val bundleFile = variant.artifacts.get(SingleArtifact.BUNDLE)
            val bundleOutputDirectory = "outputs/bundle/${variant.name}"
            val taskProvider = project.tasks.register(taskName, AabResGuardTask::class.java) { task ->
                task.variantName.set(variant.name)
                task.bundleFile.set(bundleFile)
                task.obfuscatedBundleFile.set(
                    project.layout.buildDirectory.file(
                        "$bundleOutputDirectory/${extension.obfuscatedBundleFileName}"
                    )
                )
                task.resourceMappingFile.set(
                    project.layout.buildDirectory.file(
                        "$bundleOutputDirectory/resources-mapping.txt"
                    )
                )
                task.enableObfuscate.set(extension.enableObfuscate)
                task.mappingFilePath.set(extension.mappingFile?.toString().orEmpty())
                task.whiteList.set(extension.whiteList.orEmpty())
                task.mergeDuplicatedRes.set(extension.mergeDuplicatedRes)
                task.enableFilterFiles.set(extension.enableFilterFiles)
                task.filterList.set(extension.filterList.orEmpty())
                task.enableFilterStrings.set(extension.enableFilterStrings)
                task.unusedStringPath.set(extension.unusedStringPath.orEmpty())
                task.languageWhiteList.set(extension.languageWhiteList.orEmpty())

                val flavorName = variant.name
                    .replaceFirstChar { it.uppercase() }
                    .replace("Release", "")
                    .replaceFirstChar { it.lowercase() }
                task.defaultUnusedStringFile.set(
                    project.layout.buildDirectory.file(
                        "outputs/mapping/$flavorName/release/unused.txt"
                    )
                )

                val signingConfig = resolveSigningConfig(android, variant)
                task.signingStoreFile.fileValue(signingConfig.storeFile)
                task.signingStorePassword.set(signingConfig.storePassword.orEmpty())
                task.signingKeyAlias.set(signingConfig.keyAlias.orEmpty())
                task.signingKeyPassword.set(signingConfig.keyPassword.orEmpty())
            }

            project.tasks.findByName(bundleTaskName)?.finalizedBy(taskProvider)
            project.tasks.whenTaskAdded { task ->
                if (task.name == bundleTaskName) {
                    task.finalizedBy(taskProvider)
                }
            }
        }
    }

    private fun checkApplicationPlugin(project: Project) {
        if (!project.plugins.hasPlugin("com.android.application")) {
            throw GradleException("Android Application plugin required")
        }
    }
}
