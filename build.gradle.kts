import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.martmists.commons.isStable
import com.martmists.commons.martmists

plugins {
    kotlin("multiplatform") version "1.7.20" apply false
    id("com.google.devtools.ksp") version "1.7.20-1.0.6" apply false
    id("com.github.gmazzo.buildconfig") version "3.0.3" apply false
    id("com.github.ben-manes.versions") version "0.42.0"
}

allprojects {
    group = "com.martmists.scikt"
    version = "0.0.1"

    repositories {
        mavenCentral()
        martmists()
    }

    tasks {
        withType<DependencyUpdatesTask> {
            rejectVersionIf {
                isStable(currentVersion) && !isStable(candidate.version)
            }
        }
    }
}

subprojects {
    buildDir = rootProject.buildDir.resolve(name)
}
