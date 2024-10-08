@file:Suppress("UnstableApiUsage")

import com.project.starter.easylauncher.filter.ColorRibbonFilter

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("com.starter.easylauncher") version "6.2.0"
}

android {
    namespace = "com.splicr.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.splicr.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 17
        versionName = "6.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        signingConfig = signingConfigs.getByName("debug")
    }

    signingConfigs {
        create("staging") {
            keyAlias = System.getenv("KEY_ALIAS") ?: project.properties["KEY_ALIAS"].toString()
            keyPassword =
                System.getenv("KEY_PASSWORD") ?: project.properties["KEY_PASSWORD"].toString()
            storeFile = file(
                System.getenv("KEYSTORE_PATH") ?: project.properties["KEYSTORE_PATH"].toString()
            )
            storePassword = System.getenv("KEYSTORE_PASSWORD")
                ?: project.properties["KEYSTORE_PASSWORD"].toString()
        }

        create("release") {
            keyAlias = System.getenv("KEY_ALIAS") ?: project.properties["KEY_ALIAS"].toString()
            keyPassword =
                System.getenv("KEY_PASSWORD") ?: project.properties["KEY_PASSWORD"].toString()
            storeFile = file(
                System.getenv("KEYSTORE_PATH") ?: project.properties["KEYSTORE_PATH"].toString()
            )
            storePassword = System.getenv("KEYSTORE_PASSWORD")
                ?: project.properties["KEYSTORE_PASSWORD"].toString()
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }
        create("staging") {
            initWith(getByName("debug"))
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            isDebuggable = false
            signingConfig = signingConfigs.getByName("staging")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }
    kotlinOptions {
        jvmTarget = "19"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    androidResources {
        generateLocaleConfig = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
            jniLibs.keepDebugSymbols += setOf(
                "**/libavcodec.so",
                "**/libavcodec_neon.so",
                "**/libavdevice.so",
                "**/libavdevice_neon.so",
                "**/libavfilter.so",
                "**/libavfilter_neon.so",
                "**/libavformat.so",
                "**/libavformat_neon.so",
                "**/libavutil.so",
                "**/libavutil_neon.so",
                "**/libc++_shared.so",
                "**/libffmpegkit.so",
                "**/libffmpegkit_abidetect.so",
                "**/libffmpegkit_armv7a_neon.so",
                "**/libswresample.so",
                "**/libswresample_neon.so",
                "**/libswscale.so",
                "**/libswscale_neon.so",
                "**/libandroidx.graphics.path.so"
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.googleid)
    implementation(libs.review.ktx)
    implementation(libs.firebase.appcheck.debug)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.threetenabp)
    implementation(libs.accompanist.placeholder.material)
    implementation(libs.google.firebase.auth)
    implementation(libs.coil.compose)
    implementation(libs.ui)
    implementation(libs.billing)
    implementation(libs.accompanist.permissions)
    implementation(libs.ffmpeg.kit.full)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.transcoder)
    implementation(libs.firebase.vertexai)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.review)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.ktor.client.core) // Core Ktor Client
    implementation(libs.ktor.client.android)
    testImplementation(libs.ktor.client.mock)
    androidTestImplementation(libs.ktor.client.mock)
    implementation(libs.androidx.activity.ktx)

}

easylauncher {
    buildTypes {
        register("staging") {
            filters(
                customRibbon(
                    label = "BETA",
                    labelColor = "#000000",
                    ribbonColor = "#FFFFFF",
                    gravity = ColorRibbonFilter.Gravity.BOTTOM,
                    fontName = "ComicSansMs",
                    textSizeRatio = 0.1f
                )
            )
        }
        register("debug") {
            filters(
                customRibbon(
                    label = "DEBUG",
                    labelColor = "#000000",
                    ribbonColor = "#FFFFFF",
                    gravity = ColorRibbonFilter.Gravity.BOTTOM,
                    fontName = "ComicSansMs",
                    textSizeRatio = 0.1f
                )
            )
        }
    }
}