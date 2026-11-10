import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.copy.outs)
    alias(libs.plugins.aboutlibraries)
}


android {
    namespace = "cc.chenhe.weargallery"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.xxtvrxx233.weargallery"
        minSdk = 26
        targetSdk = 37
        versionCode = 220603021 // header(22)+xx.xx.xx+device(0-phone; 1-wear)
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
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    lint {
        disable += "MissingTranslation"
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(project(":common"))

    // Compose for Wear OS
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.material.kolor)
    implementation(libs.aboutlibraries.core)

    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.palette)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.google.play.services.wearable)
    implementation(libs.google.support.wearable)
    compileOnly(libs.google.wearable.compile.only)
    implementation(libs.ichenhe.watch.face.helper)
    implementation(libs.nanohttpd)
    implementation(libs.google.zxing.core)

    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.localbroadcastmanager)

    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.compose)

    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)


    implementation(libs.sketch)
    implementation(libs.sketch.gif)
    implementation(libs.wearvision)

    implementation(libs.coil)
    implementation(libs.coil.gif)
    implementation(libs.coil.compose)

    testImplementation(libs.junit)
    testImplementation(libs.strikt.core)
}
