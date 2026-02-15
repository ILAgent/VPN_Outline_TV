import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)

    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.ilagent.nativeoutline"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ilagent.nativeoutline"
        minSdk = 24
        targetSdk = 34
        versionCode = 102000
        versionName = "1.2"

        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "COMMIT_HASH", "\"${getCommitHash()}\"")
        buildConfigField("String", "COMMIT_TIME", "\"${getCommitTime()}\"")
        buildConfigField(
            "String",
            "BUILD_TIME",
            "\"${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}\""
        )
        buildConfigField("String", "BRANCH", "\"${getGitBranch()}\"")
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

fun getCommitHash(): String {

    return try {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
            standardOutput = stdout
        }
        stdout.toString().trim()
    } catch (e: Exception) {
        "unknown"
    }
}

fun getCommitTime(): String {
    val stdout = ByteArrayOutputStream()
    //return try {
    exec {
        commandLine("git", "log", "-1", "--format=%ci")
        isIgnoreExitValue = true
    }
    return stdout.toString().trim()
//    } catch (e: Exception) {
//        println(e)
//        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
//    }
}

fun getGitBranch(): String {
    return try {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
            standardOutput = stdout
            isIgnoreExitValue = true
        }
        stdout.toString().trim().takeIf { it.isNotEmpty() } ?: "unknown"
    } catch (e: Exception) {
        "unknown"
    }
}

dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.play.services.fido)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.coil.kt)
    implementation(libs.coil.kt.coil.compose)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
    implementation(libs.firebase.config)

    implementation(libs.gson)

    implementation(libs.nanohttpd)
    implementation(libs.core)
    implementation(libs.zxing.android.embedded)
}