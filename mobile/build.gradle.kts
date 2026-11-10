import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.copy.outs)
}


android {
    namespace = "cc.chenhe.weargallery"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.xxtvrxx233.weargallery"
        minSdk = 26
        targetSdk = 37
        versionCode = 220603020 // header(22)+xx.xx.xx+device(0-phone; 1-wear)
        versionName = "v6.3.2"

        vectorDrawables.useSupportLibrary = true

    }

    signingConfigs {
        Signing(rootDir).readConfig()?.also { config ->
            create("release") {
                storeFile = config.storeFile
                storePassword = config.storePassword
                keyAlias = config.keyAlias
                keyPassword = config.keyPassword
            }
        }
    }

    buildTypes {
        debug {
            // Same key as release so debug builds can replace a signed install.
            signingConfigs.findByName("release")?.also { signingConfig = it }
        }
        release {
            signingConfigs.findByName("release")?.also { signingConfig = it }
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }

    lint {
        disable += "MissingTranslation"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    implementation(project(":common"))

    // compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.window.size)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.material.kolor)

    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.localbroadcastmanager)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.google.material)
    implementation(libs.compressor)
    implementation(libs.google.play.services.wearable)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)


    // pictures
    implementation(libs.subsampling.scale.image.view)
    implementation(libs.coil)
    implementation(libs.coil.gif)
    implementation(libs.coil.compose)

    testImplementation(libs.junit)
    testImplementation(libs.strikt.core)
}
