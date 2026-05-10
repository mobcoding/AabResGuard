package com.bytedance.android.plugin.tasks

import com.bytedance.android.aabresguard.commands.ObfuscateBundleCommand
import com.bytedance.android.plugin.extensions.AabResGuardExtension
import com.bytedance.android.plugin.model.SigningConfig
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path

abstract class AabResGuardTask : DefaultTask() {

    @get:Input
    abstract val variantName: Property<String>

    @get:InputFile
    abstract val bundleFile: RegularFileProperty

    @get:OutputFile
    abstract val obfuscatedBundleFile: RegularFileProperty

    @get:Internal
    lateinit var signingConfig: SigningConfig

    @get:Internal
    val aabResGuard: AabResGuardExtension
        get() = project.extensions.getByType(AabResGuardExtension::class.java)

    init {
        description = "Assemble resource proguard for bundle file"
        group = "bundle"
        outputs.upToDateWhen { false }
    }

    @Internal
    fun getObfuscatedBundlePath(): Path = obfuscatedBundleFile.get().asFile.toPath()

    @TaskAction
    fun executeTask() {
        println(aabResGuard.toString())
        printSignConfiguration()
        prepareUnusedFile()

        val command = ObfuscateBundleCommand.builder()
            .setEnableObfuscate(aabResGuard.enableObfuscate)
            .setBundlePath(bundleFile.get().asFile.toPath())
            .setOutputPath(obfuscatedBundleFile.get().asFile.toPath())
            .setMergeDuplicatedResources(aabResGuard.mergeDuplicatedRes)
            .setWhiteList(aabResGuard.whiteList)
            .setFilterFile(aabResGuard.enableFilterFiles)
            .setFileFilterRules(aabResGuard.filterList)
            .setRemoveStr(aabResGuard.enableFilterStrings)
            .setUnusedStrPath(aabResGuard.unusedStringPath)
            .setLanguageWhiteList(aabResGuard.languageWhiteList)

        aabResGuard.mappingFile?.let { command.setMappingPath(it) }

        if (signingConfig.storeFile != null && signingConfig.storeFile!!.exists()) {
            command.setStoreFile(signingConfig.storeFile!!.toPath())
                .setKeyAlias(signingConfig.keyAlias)
                .setKeyPassword(signingConfig.keyPassword)
                .setStorePassword(signingConfig.storePassword)
        }
        command.build().execute()
    }

    private fun prepareUnusedFile() {
        val buildType = variantName.get().replaceFirstChar { it.uppercase() }
            .replace("Release", "")
        val flavorName = buildType.replaceFirstChar { it.lowercase() }
        val resourcePath = "${project.layout.buildDirectory.get().asFile.absolutePath}/outputs/mapping/$flavorName/release/unused.txt"
        val usedFile = project.file(resourcePath)
        if (usedFile.exists()) {
            println("find unused.txt : ${usedFile.absolutePath}")
            if (aabResGuard.enableFilterStrings && aabResGuard.unusedStringPath.isNullOrBlank()) {
                aabResGuard.unusedStringPath = usedFile.absolutePath
                println("replace unused.txt!")
            }
        } else {
            println(
                "not exists unused.txt : ${usedFile.absolutePath}\n" +
                    "use default path : ${aabResGuard.unusedStringPath}"
            )
        }
    }

    private fun printSignConfiguration() {
        println("-------------- sign configuration --------------")
        println("\tstoreFile : ${signingConfig.storeFile}")
        println("\tkeyPassword : ${encrypt(signingConfig.keyPassword)}")
        println("\talias : ${encrypt(signingConfig.keyAlias)}")
        println("\tstorePassword : ${encrypt(signingConfig.storePassword)}")
        println("-------------- sign configuration --------------")
    }

    private fun encrypt(value: String?): String {
        if (value == null) return "/"
        if (value.length > 2) {
            return "${value.substring(0, value.length / 2)}****"
        }
        return "****"
    }
}
