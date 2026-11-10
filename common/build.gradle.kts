plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.copy.outs)
}

copyOuts {
    copyApk = false
}

android {
    namespace = "cc.chenhe.weargallery.common"
    compileSdk = 37

    resourcePrefix = "lib_"

    defaultConfig {
        minSdk = 23
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    lint {
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.process)

    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)

    implementation(libs.androidx.exifinterface)

    api(libs.timber)
    api(libs.wearmsger)

    testImplementation(libs.junit)
    testImplementation(libs.strikt.core)
}
