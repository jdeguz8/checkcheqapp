import java.util.Properties
import java.io.FileInputStream
import org.jetbrains.dokka.gradle.DokkaTask   // 👈 add this import

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("androidx.room")
    id("com.google.gms.google-services")

    // 👇 Dokka plugin via version catalog (libs.versions.toml -> jetbrainsDokka)
    alias(libs.plugins.jetbrainsDokka)
}

android {
    namespace = "com.jdeguzman.checkcheqapp"
    compileSdk = 35

    // ---- Load secrets from local.properties once ----
    val localProps = Properties().apply {
        val localPropsFile = rootProject.file("local.properties")
        if (localPropsFile.exists()) {
            FileInputStream(localPropsFile).use { fis ->
                load(fis)
            }
        }
    }

    val mapsApiKey: String = localProps.getProperty("MAPS_API_KEY") ?: ""
    val webClientId: String = localProps.getProperty("WEB_CLIENT_ID") ?: ""

    defaultConfig {
        applicationId = "com.jdeguzman.checkcheqapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        // Manifest placeholder (Maps API key)
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = mapsApiKey

        resValue("string", "google_maps_key", mapsApiKey)

        buildConfigField(
            "String",
            "YELP_API_KEY",
            "\"${localProps.getProperty("YELP_API_KEY")}\""
        )
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // (Optional but nice for Room migration tests)
    sourceSets["test"].assets.srcDir("$projectDir/schemas")
    sourceSets["androidTest"].assets.srcDir("$projectDir/schemas")

    // 🔎 You had these duplicated; keeping one set is enough.
    // Leaving the second block out to avoid noise.
}

dependencies {
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.foundation)
    implementation(libs.material3)
    implementation(libs.ui)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.09.02")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // Room (KSP)
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jsoup:jsoup:1.18.1")

    // Hilt (KSP)
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Other
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("com.google.android.material:material:1.12.0")

    val roomVersion = "2.6.1"          // keep consistent across Room deps
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    implementation("androidx.compose.material:material-icons-extended")

    // Google Maps Compose
    implementation("com.google.maps.android:maps-compose:4.4.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")

    implementation("io.coil-kt:coil-compose:2.6.0")

    // Location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.firebase:firebase-auth")

    // Places
    implementation("com.google.android.libraries.places:places:3.5.0")
}

room {
    schemaDirectory("$projectDir/schemas")   // resolves to <module>/schemas
}

// 🔹 Dokka configuration – generates HTML docs from your KDocs
tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets.configureEach {
        // You can tweak per-source-set options here if you like, e.g.:
        // skipDeprecated.set(true)
        // reportUndocumented.set(false)
    }
}
