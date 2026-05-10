package com.bytedance.android.plugin.internal

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.SigningConfig as DslSigningConfig
import com.android.build.api.variant.ApplicationVariant
import com.bytedance.android.plugin.model.SigningConfig

internal fun resolveSigningConfig(
    android: ApplicationExtension,
    variant: ApplicationVariant,
): SigningConfig {
    val buildTypeConfig = variant.buildType
        ?.let { android.buildTypes.findByName(it)?.signingConfig }
    if (buildTypeConfig != null) {
        return buildTypeConfig.toModel()
    }

    val flavorConfig = variant.productFlavors
        .asReversed()
        .mapNotNull { (_, flavorName) -> android.productFlavors.findByName(flavorName)?.signingConfig }
        .firstOrNull()
    if (flavorConfig != null) {
        return flavorConfig.toModel()
    }

    return android.defaultConfig.signingConfig?.toModel() ?: SigningConfig(null, null, null, null)
}

private fun DslSigningConfig.toModel(): SigningConfig = SigningConfig(
    storeFile = storeFile,
    storePassword = storePassword,
    keyAlias = keyAlias,
    keyPassword = keyPassword,
)
