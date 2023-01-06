import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    kotlin("multiplatform")
    id("com.github.gmazzo.buildconfig")
    id("com.google.devtools.ksp")
}

kotlin {
    val nonNativeTargets = listOf(
        jvm(),
        js(IR) {
            browser()
            nodejs()
        }
    )
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val hostTarget = when {
        hostOs == "Mac OS X" -> KonanTarget.MACOS_X64
        hostOs == "Linux" -> KonanTarget.LINUX_X64
        isMingwX64 -> KonanTarget.MINGW_X64
        else -> error("Unsupported host OS: $hostOs")
    }

    fun KotlinNativeTarget?.disableWin() = if (isMingwX64) null else this
    fun KotlinNativeTarget?.disableLinux() = if (hostTarget == KonanTarget.LINUX_X64) null else this
    fun KotlinNativeTarget?.disableMac() = if (hostTarget == KonanTarget.MACOS_X64) null else this

    val nativeTargets = listOfNotNull(
        // X64
        mingwX64(),

        linuxX64().disableWin(),
        macosX64().disableWin().disableLinux(),

        // Arm
        linuxArm64().disableWin(),
        macosArm64().disableWin().disableLinux(),
    )
    val allTargets = nonNativeTargets + nativeTargets

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":numkt"))
            }
        }
        val commonTest by getting
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val nativeTest by creating {
            dependsOn(commonTest)
        }

        nativeTargets.forEach {
            getByName("${it.name}Main").dependsOn(nativeMain)
            getByName("${it.name}Test").dependsOn(nativeTest)
        }

        allTargets.forEach {
            getByName("${it.name}Main") {
                kotlin.srcDir(buildDir.absolutePath + "/generated/ksp/${it.name}/${it.name}Main/kotlin")
            }
        }
    }

    nativeTargets.forEach {
        it.apply {
            binaries {
                staticLib {
                    binaryOptions["memoryModel"] = "experimental"
                    freeCompilerArgs += listOf("-Xgc=cms")
                }
            }
        }
    }

    allTargets.forEach {
        dependencies {
            add("ksp${it.name.capitalize()}",  project(":union-processor"))
        }
    }
}
