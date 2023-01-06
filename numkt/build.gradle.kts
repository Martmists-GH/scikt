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
                api(project(":union-annotations"))
                api("com.martmists.commons:commons-mpp-extensions:1.0.4")
                api("com.martmists.commons:commons-mpp-functions:1.0.4")
            }

            kotlin.srcDir(buildDir.absolutePath + "/generated/ksp/metadata/commonMain/kotlin")
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
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
}

dependencies {
    add("kspCommonMainMetadata",  project(":union-processor"))
}

afterEvaluate {
    tasks {
        withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>> {
            if (name != "kspCommonMainKotlinMetadata") {
                dependsOn("kspCommonMainKotlinMetadata")
            }
        }
    }
}
