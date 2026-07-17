package com.bytedance.android.plugin.tasks

import com.bytedance.android.aabresguard.commands.ObfuscateBundleCommand
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path

abstract class AabResGuardTask : DefaultTask() {

    @get:Input
    abstract val variantName: Property<String>

    @get:InputFile
    abstract val bundleFile: RegularFileProperty

    @get:OutputFile
    abstract val obfuscatedBundleFile: RegularFileProperty

    @get:Input
    abstract val enableObfuscate: Property<Boolean>

    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val mappingFile: RegularFileProperty

    @get:Input
    abstract val whiteList: SetProperty<String>

    @get:Input
    abstract val mergeDuplicatedRes: Property<Boolean>

    @get:Input
    abstract val enableFilterFiles: Property<Boolean>

    @get:Input
    abstract val filterList: SetProperty<String>

    @get:Input
    abstract val enableFilterStrings: Property<Boolean>

    @get:Input
    abstract val unusedStringPath: Property<String>

    @get:Input
    abstract val languageWhiteList: SetProperty<String>

    @get:Internal
    abstract val defaultUnusedStringFile: RegularFileProperty

    @get:Internal
    abstract val signingStoreFile: RegularFileProperty

    @get:Internal
    abstract val signingStorePassword: Property<String>

    @get:Internal
    abstract val signingKeyAlias: Property<String>

    @get:Internal
    abstract val signingKeyPassword: Property<String>

    init {
        description = "Assemble resource proguard for bundle file"
        group = "bundle"
        outputs.upToDateWhen { false }
    }

    @Internal
    fun getObfuscatedBundlePath(): Path = obfuscatedBundleFile.get().asFile.toPath()

    @TaskAction
    fun executeTask() {
        printConfiguration()
        printSignConfiguration()
        prepareOutputDir()
        val unusedStringPath = resolveUnusedStringPath()

        val command = ObfuscateBundleCommand.builder()
            .setEnableObfuscate(enableObfuscate.get())
            .setBundlePath(bundleFile.get().asFile.toPath())
            .setOutputPath(obfuscatedBundleFile.get().asFile.toPath())
            .setMergeDuplicatedResources(mergeDuplicatedRes.get())
            .setWhiteList(whiteList.get())
            .setFilterFile(enableFilterFiles.get())
            .setFileFilterRules(filterList.get())
            .setRemoveStr(enableFilterStrings.get())
            .setUnusedStrPath(unusedStringPath)
            .setLanguageWhiteList(languageWhiteList.get())

        mappingFile.orNull?.asFile?.toPath()?.let { command.setMappingPath(it) }

        val storeFile = signingStoreFile.orNull?.asFile
        if (storeFile != null && storeFile.exists()) {
            command.setStoreFile(storeFile.toPath())
                .setKeyAlias(signingKeyAlias.get())
                .setKeyPassword(signingKeyPassword.get())
                .setStorePassword(signingStorePassword.get())
        }
        command.build().execute()
    }

    private fun prepareOutputDir() {
        val outputDir = obfuscatedBundleFile.get().asFile.parentFile
        if (outputDir.exists()) {
            outputDir.deleteRecursively()
        }
        outputDir.mkdirs()
    }

    private fun resolveUnusedStringPath(): String {
        val configuredPath = unusedStringPath.get()
        val usedFile = defaultUnusedStringFile.get().asFile
        if (usedFile.exists()) {
            println("find unused.txt : ${usedFile.absolutePath}")
            if (enableFilterStrings.get() && configuredPath.isBlank()) {
                println("replace unused.txt!")
                return usedFile.absolutePath
            }
        } else {
            println(
                "not exists unused.txt : ${usedFile.absolutePath}\n" +
                    "use default path : $configuredPath"
            )
        }
        return configuredPath
    }

    private fun printConfiguration() {
        println(
            "AabResGuardExtension\n" +
                "\tenableObfuscate=${enableObfuscate.get()}\n" +
                "\tmappingFile=${mappingFile.orNull?.asFile}\n" +
                "\twhiteList=${whiteList.get()}\n" +
                "\tobfuscatedBundleFileName=${obfuscatedBundleFile.get().asFile.name}\n" +
                "\tmergeDuplicatedRes=${mergeDuplicatedRes.get()}\n" +
                "\tenableFilterFiles=${enableFilterFiles.get()}\n" +
                "\tfilterList=${filterList.get()}\n" +
                "\tenableFilterStrings=${enableFilterStrings.get()}\n" +
                "\tunusedStringPath=${unusedStringPath.get()}\n" +
                "\tlanguageWhiteList=${languageWhiteList.get()}"
        )
    }

    private fun printSignConfiguration() {
        println("-------------- sign configuration --------------")
        println("\tstoreFile : ${signingStoreFile.orNull?.asFile}")
        println("\tkeyPassword : ${encrypt(signingKeyPassword.get())}")
        println("\talias : ${encrypt(signingKeyAlias.get())}")
        println("\tstorePassword : ${encrypt(signingStorePassword.get())}")
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
