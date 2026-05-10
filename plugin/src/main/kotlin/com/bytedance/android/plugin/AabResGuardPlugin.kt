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
            val taskProvider = project.tasks.register(taskName, AabResGuardTask::class.java) { task ->
                task.variantName.set(variant.name)
                task.bundleFile.set(bundleFile)
                task.obfuscatedBundleFile.set(
                    project.layout.buildDirectory.file(
                        "outputs/aabresguard/${variant.name}/${extension.obfuscatedBundleFileName}"
                    )
                )
                task.signingConfig = resolveSigningConfig(android, variant)
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
