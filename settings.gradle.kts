pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.12.3"
        id("org.jetbrains.kotlin.android") version "2.0.21"
        id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
        id("com.google.devtools.ksp") version "2.0.21-1.0.25"
        id("com.google.dagger.hilt.android") version "2.52"
        id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1"
        // Room Gradle plugin (silences the exportSchema warning)
        id("androidx.room") version "2.6.1"
        id("com.google.gms.google-services") version "4.4.2"

    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "checkcheqapp"
include(":app")
