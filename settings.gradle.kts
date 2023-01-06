buildscript {
    repositories {
        mavenCentral()
        maven("https://maven.martmists.com/releases")
    }
    dependencies {
        classpath("com.martmists.commons:commons-gradle:1.0.4")
    }
}

include(":union-annotations", ":union-processor", ":numkt", ":scikt")
