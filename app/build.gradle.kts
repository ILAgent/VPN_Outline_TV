import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.Properties
import com.android.build.api.dsl.ApplicationExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "1.9.0"

    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.ilagent.nativeoutline"
    compileSdk = 36

    signingConfigs {
        create("release") {
            // Загружаем свойства из файла, который создаст GitHub Actions
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))

                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    defaultConfig {
        applicationId = "com.ilagent.nativeoutline"
        minSdk = 24
        targetSdk = 36
        versionCode = 300
        versionName = "0.3"

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
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
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
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

fun getCommitHash(): String {

    return try {
        val stdout = ByteArrayOutputStream()
        providers.exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
            standardOutput = stdout
        }
        stdout.toString().trim()
    } catch (e: Exception) {
        println(e)
        "unknown"
    }
}

// Специальная функция для получения времени коммита
fun getCommitTime(): String {
    // Пробуем разные варианты получения времени
    val timeFormats = listOf(
        // Вариант 1: через log (самый надежный)
        runGitCommand("git", "log", "-1", "--format=%cd", "--date=iso"),
        // Вариант 2: через show
        runGitCommand("git", "show", "-s", "--format=%ci", "HEAD"),
        // Вариант 3: через log с другой датой
        runGitCommand("git", "log", "-1", "--format=%cI"),  // ISO 8601
        // Вариант 4: через log с timestamp
        runGitCommand("git", "log", "-1", "--format=%ct")?.let { timestamp ->
            // Конвертируем timestamp в дату
            try {
                val date = Date(timestamp.toLong() * 1000)
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.getDefault()).format(date)
            } catch (e: Exception) {
                println(e)
                "unknown"
            }
        }
    )

    // Берем первый непустой результат
    for (time in timeFormats) {
        if (time != null && time.isNotEmpty()) {
            println("✅ Commit time found: $time")
            return time
        }
    }

    // Если ничего не нашли, возвращаем текущее время
    println("⚠️ No commit time found, using current time")
    return "unknown"
}

fun getGitBranch(): String {
    return try {
        val stdout = ByteArrayOutputStream()
        providers.exec {
            commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
            standardOutput = stdout
            isIgnoreExitValue = true
        }
        stdout.toString().trim().takeIf { it.isNotEmpty() } ?: "unknown"
    } catch (e: Exception) {
        println(e)
        "unknown"
    }
}

// Функция для git команд (работает для хэша и ветки)
fun runGitCommand(vararg command: String): String? {
    return try {
        val output = providers.exec {
            commandLine(command.toList())
            isIgnoreExitValue = true
        }.standardOutput.asText.get()

        output.trim().takeIf { it.isNotEmpty() }
    } catch (e: Exception) {
        println("Git command error: ${e.message}")
        null
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

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.nanohttpd)
    implementation(libs.core)
    implementation(libs.zxing.android.embedded)

}