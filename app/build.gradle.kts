import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.ksp)
}

val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) {
        keystorePropsFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.tcgvault.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tcgvault.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 5
        versionName = "1.0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val storeFilePath = keystoreProps.getProperty("storeFile")
            if (!storeFilePath.isNullOrBlank()) {
                storeFile = rootProject.file(storeFilePath)
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (keystorePropsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
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
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Local persistence (Room SQLite)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Public catalog sync (Scryfall & YGOPRODeck) + image loading
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.androidx.ui.tooling)
}

val ensureEmulator by tasks.registering {
    group = "application"
    description = "Boots an Android emulator if no device is currently connected."

    doLast {
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        val adb = android.adbExecutable.absolutePath
        val emulatorExe = File(
            android.sdkDirectory,
            if (isWindows) "emulator/emulator.exe" else "emulator/emulator"
        )

        fun runOutput(vararg cmd: String): String {
            val proc = ProcessBuilder(*cmd).redirectErrorStream(true).start()
            val out = proc.inputStream.bufferedReader().readText()
            proc.waitFor()
            return out
        }

        val connected = runOutput(adb, "devices")
            .lineSequence()
            .drop(1)
            .any { it.trim().endsWith("\tdevice") }

        if (connected) {
            logger.lifecycle("Android device/emulator already connected.")
            return@doLast
        }

        val requestedAvd = project.findProperty("avd")?.toString()?.takeIf { it.isNotBlank() }
        val avdName = requestedAvd ?: runOutput(emulatorExe.absolutePath, "-list-avds")
            .lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() }
            ?: error("No Android Virtual Devices (AVDs) found. Create one in Android Studio Device Manager.")

        logger.lifecycle("Starting emulator '$avdName'...")
        ProcessBuilder(emulatorExe.absolutePath, "-avd", avdName)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start()

        runOutput(adb, "wait-for-device")
        repeat(90) {
            if (runOutput(adb, "shell", "getprop", "sys.boot_completed").trim() == "1") {
                return@doLast
            }
            Thread.sleep(2000)
        }
        error("Timed out waiting for emulator '$avdName' to finish booting.")
    }
}

tasks.matching { it.name == "installDebug" }.configureEach {
    mustRunAfter(ensureEmulator)
}

tasks.register("runDebug") {
    group = "application"
    description = "Starts an emulator if needed, installs the debug APK, and launches the app."
    dependsOn(ensureEmulator, "installDebug")

    doLast {
        val adb = android.adbExecutable.absolutePath
        val proc = ProcessBuilder(
            adb, "shell", "am", "start", "-n", "com.tcgvault.app/.MainActivity"
        ).inheritIO().start()
        check(proc.waitFor() == 0) { "Failed to launch com.tcgvault.app/.MainActivity" }
    }
}
