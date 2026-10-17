import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.homearcade.tv"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.homearcade.tv"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val storeFilePath = keystoreProperties.getProperty("storeFile") ?: System.getenv("KEYSTORE_FILE")
            val storePasswordVal = keystoreProperties.getProperty("storePassword") ?: System.getenv("KEYSTORE_PASSWORD")
            val keyAliasVal = keystoreProperties.getProperty("keyAlias") ?: System.getenv("KEY_ALIAS")
            val keyPasswordVal = keystoreProperties.getProperty("keyPassword") ?: System.getenv("KEY_PASSWORD")

            if (storeFilePath != null && storePasswordVal != null && keyAliasVal != null && keyPasswordVal != null) {
                val resolvedFile = rootProject.file(storeFilePath)
                if (resolvedFile.exists()) {
                    storeFile = resolvedFile
                    storePassword = storePasswordVal
                    keyAlias = keyAliasVal
                    keyPassword = keyPasswordVal
                }
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            val releaseSigning = signingConfigs.getByName("release")
            signingConfig = if (releaseSigning.storeFile != null) {
                releaseSigning
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/INDEX.LIST",
                "/META-INF/io.netty.versions.properties"
            )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // Compose BOM & Core
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Jetpack Compose for TV
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.tv.foundation)

    // Navigation & Lifecycle & DataStore
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Image Loading (Coil)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Ktor Client Network Stack & Kotlinx Serialization
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
}
