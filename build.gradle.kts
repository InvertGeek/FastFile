plugins {
    id("com.android.application") version "8.10.1" apply false
    id("org.jetbrains.kotlin.android") version "2.2.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0" apply false
}

buildscript {
    repositories {
        flatDir {
            dirs("libs")
        }
        google()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.3")
    }
}
