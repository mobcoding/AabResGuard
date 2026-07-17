package com.bytedance.android.plugin.tasks

import com.bytedance.android.aabresguard.commands.ObfuscateBundleCommand
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Path

abstract class AabResGuardTask : DefaultTask() {

    @get:Input
    abstract val variantName: Property<String>

    @get:InputFile
    abstract val bundleFile: RegularFileProperty

    @get:OutputFile
    abstract val obfuscatedBundleFile: RegularFileProperty

    @get:OutputFile
    abstract val resourceMappingFile: RegularFileProperty

    @get:Input
    abstract val enableObfuscate: Property<Boolean>

    @get:Input
    abstract val mappingFilePath: Property<String>

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
        val mappingFile = resolveMappingFile()
        prepareOutputFiles(mappingFile)
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

        mappingFile?.let { command.setMappingPath(it) }

        val storeFile = signingStoreFile.orNull?.asFile
        if (storeFile != null && storeFile.exists()) {
            command.setStoreFile(storeFile.toPath())
                .setKeyAlias(signingKeyAlias.get())
                .setKeyPassword(signingKeyPassword.get())
                .setStorePassword(signingStorePassword.get())
        }
        command.build().execute()
    }

    private fun prepareOutputFiles(mappingInput: Path?) {
        val bundle = bundleFile.get().asFile.absoluteFile
        val obfuscatedBundle = obfuscatedBundleFile.get().asFile.absoluteFile
        if (bundle == obfuscatedBundle) {
            throw GradleException("obfuscatedBundleFileName must differ from the input bundle file name")
        }

        val outputMapping = resourceMappingFile.get().asFile.absoluteFile
        if (mappingInput?.toFile()?.absoluteFile == outputMapping) {
            throw GradleException(
                "mappingFile must be archived outside the Bundle output directory before incremental obfuscation"
            )
        }

        val outputDir = obfuscatedBundle.parentFile
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw GradleException("Unable to create AabResGuard output directory: $outputDir")
        }

        deleteOutputFile(obfuscatedBundle)
        deleteOutputFile(outputMapping)
        outputDir.listFiles { _, name -> name.endsWith("-duplicated.txt") }
            ?.forEach(::deleteOutputFile)
    }

    private fun deleteOutputFile(file: File) {
        if (file.exists() && !file.delete()) {
            throw GradleException("Unable to delete stale AabResGuard output: $file")
        }
    }

    private fun resolveMappingFile(): Path? {
        val configuredPath = mappingFilePath.get()
        if (configuredPath.isBlank()) {
            return null
        }

        val file = File(configuredPath)
        if (file.isFile) {
            return file.toPath()
        }

        println("not exists mapping file : ${file.absolutePath}")
        return null
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
                "\tmappingFile=${mappingFilePath.get().ifBlank { null }}\n" +
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
