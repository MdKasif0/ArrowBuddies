import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val applicationPackage = "com.kasif.arrowbuddies"

android {
    namespace = applicationPackage
    compileSdk = 35

    defaultConfig {
        applicationId = applicationPackage
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // English-only game: drop other languages bundled by AndroidX libraries too
        resourceConfigurations += listOf("en")
        vectorDrawables {
            useSupportLibrary = true
        }

        // AdMob Official Test Identifiers
        val testAdmobAppId = "ca-app-pub-3940256099942544~3347511713"
        val testRewardedAdUnitId = "ca-app-pub-3940256099942544/5224354917"

        manifestPlaceholders["admobAppId"] = testAdmobAppId
        buildConfigField("boolean", "ADS_ENABLED", "true")
        buildConfigField("String", "ADMOB_APP_ID", "\"$testAdmobAppId\"")
        buildConfigField("String", "ADMOB_REWARDED_FAIL_ID", "\"$testRewardedAdUnitId\"")
        buildConfigField("String", "ADMOB_REWARDED_WIN_ID", "\"$testRewardedAdUnitId\"")
        buildConfigField("String", "ADMOB_REWARDED_HINT_ID", "\"$testRewardedAdUnitId\"")
    }

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    val adsPropertiesFile = rootProject.file("ads.properties")
    val adsProperties = Properties()
    val isReleaseTask = gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }
    if (adsPropertiesFile.exists()) {
        adsPropertiesFile.inputStream().use { adsProperties.load(it) }
    } else if (isReleaseTask) {
        throw GradleException("Release build failed: 'ads.properties' is missing! Please provide ads.properties at root with AdMob production IDs.")
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                val storeFilePath = keystoreProperties.getProperty("storeFile")
                storeFile = rootProject.file(storeFilePath)
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            val releaseAppId = adsProperties.getProperty("admob.app.id") ?: "ca-app-pub-3940256099942544~3347511713"
            val releaseFailAdId = adsProperties.getProperty("admob.rewarded.fail.id") ?: "ca-app-pub-3940256099942544/5224354917"
            val releaseWinAdId = adsProperties.getProperty("admob.rewarded.win.id") ?: "ca-app-pub-3940256099942544/5224354917"
            val releaseHintAdId = adsProperties.getProperty("admob.rewarded.hint.id") ?: "ca-app-pub-3940256099942544/5224354917"
            val releaseAdsEnabled = adsProperties.getProperty("admob.enabled", "true").toBoolean()

            manifestPlaceholders["admobAppId"] = releaseAppId
            buildConfigField("boolean", "ADS_ENABLED", releaseAdsEnabled.toString())
            buildConfigField("String", "ADMOB_APP_ID", "\"$releaseAppId\"")
            buildConfigField("String", "ADMOB_REWARDED_FAIL_ID", "\"$releaseFailAdId\"")
            buildConfigField("String", "ADMOB_REWARDED_WIN_ID", "\"$releaseWinAdId\"")
            buildConfigField("String", "ADMOB_REWARDED_HINT_ID", "\"$releaseHintAdId\"")
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ""
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        // Robolectric tests read the level packs from assets
        unitTests.isIncludeAndroidResources = true
    }

    sourceSets {
        // Exported Room schemas, used by migration tests
        getByName("test").assets.srcDir("$projectDir/schemas")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

ksp {
    // Export Room schemas so every version is reviewed and migrations can be tested
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(project(":engine"))

    // AndroidX Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Dagger Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Google Mobile Ads & UMP
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)

    // Kotlinx
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.robolectric)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}

