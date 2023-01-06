import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("com.github.gmazzo.buildconfig")
    `maven-publish`
}

dependencies {
    kapt("com.google.auto.service:auto-service:1.0.1")
    implementation("com.google.auto.service:auto-service-annotations:1.0.1")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.7.20-1.0.6")

    implementation(project(":union-annotations"))
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs += listOf(
                "-opt-in=com.google.devtools.ksp.KspExperimental",
            )
        }
    }
}
