import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    kotlin("multiplatform")
    id("com.github.gmazzo.buildconfig")
}

kotlin {
    jvm()
    js(IR) {
        browser()
        nodejs()
    }
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

    val targets = listOfNotNull(
        // X64
        mingwX64(),

        linuxX64().disableWin(),
        macosX64().disableWin().disableLinux(),

        // Arm
        linuxArm64().disableWin(),
        macosArm64().disableWin().disableLinux(),
    )

    sourceSets {
        val commonMain by getting
        val commonTest by getting
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val nativeTest by creating {
            dependsOn(commonTest)
        }
        targets.forEach {
            getByName("${it.name}Main").dependsOn(nativeMain)
            getByName("${it.name}Test").dependsOn(nativeTest)
        }
    }

    targets.forEach {
        it.apply {
            binaries {
                staticLib {
                    binaryOptions["memoryModel"] = "experimental"
                    freeCompilerArgs += listOf("-Xgc=cms")
                }
            }
        }
    }
}
