plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("rs.houtbecke.gradle.recorder.plugin")
}

kotlin {
    androidTarget()
    applyDefaultHierarchyTemplate()
}

android {
    setCompileSdkVersion(34)
    namespace = "splendo.gradle.plugin.example"

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    signingConfigs {
        create("stableDebug") {
            storeFile = project.rootProject.file("keystore/stableDebug.keystore")
            storePassword = "stableDebug"
            keyAlias = "stableDebug"
            keyPassword = "stableDebug"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
        debug {
            signingConfig = signingConfigs.getByName("stableDebug")
        }
    }

    defaultConfig {
        minSdk = 30
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

}

dependencies {
    implementation(libs.compose)
    implementation(libs.composeAnimation)
    implementation(libs.activityCompose)
    implementation(libs.composeMaterial)
    implementation(libs.composeUiTooling)

    androidTestImplementation(libs.composeUiTest)
    androidTestImplementation(libs.test)

    debugImplementation(libs.composeUiTestManifest)
}

recordConfig {
    videoOutput.set(file("out.mp4"))
}

fun runProcess(command: List<String>, printOut: Boolean = true):Pair<Int,String> {
    val builder = ProcessBuilder(command)
    builder.redirectErrorStream(true)
    val process = builder.start()
    val out = StringBuilder()
    process.inputStream!!.bufferedReader().forEachLine {
        if (printOut) println(it)
        out.append(it).append('\n')
    }
    val exitCode = process.waitFor()
    return exitCode to out.toString()
}


task("startSimulator") {
    fun bootXcodeSimulator(device: String) {
        val command = listOf("/usr/bin/xcrun", "simctl", "boot", device, "--arch=x86_64")
        val (exitCode, out) = runProcess(command)

        assert(exitCode == 0 || "current state: Booted" in out) {
            "Failed to boot a simulator $device and it wasn't started before, exit code = $exitCode"
        }
        if (exitCode == 0) {
            println("Simulator started")// if the simulator was booted not by us, we should not shut it down
        }
    }

    doLast { bootXcodeSimulator(properties["test.recorder.simulator.deviceid"].toString()) } // "4B2D8588-040A-4384-9802-EE72CE8C0707"
}

task("shutdownSimulator") {
    fun shutDownSimulator(device: String) {
        val command = listOf("/usr/bin/xcrun", "simctl", "shutdown", device)
        val (exitCode, out) = runProcess(command)
        assert(0 == exitCode)
        println("simulator shutdown")
    }

    doLast { shutDownSimulator(properties["test.recorder.simulator.deviceid"].toString()) } //
}

