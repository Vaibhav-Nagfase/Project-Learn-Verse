plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.learnverse"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.learnverse"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    // Your existing non-compose dependencies are fine
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    val okhttpVersion = "4.12.0" // Or check for the absolute latest stable version
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion") // Add core okhttp
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion") // Match version
    implementation("com.squareup.okhttp3:okhttp-sse:$okhttpVersion") // Match version

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation(libs.play.services.location)

    implementation("com.auth0.android:jwtdecode:2.0.2")
    implementation("io.coil-kt:coil-compose:2.6.0")

    implementation("io.noties.markwon:core:4.6.2")
    implementation("io.noties.markwon:ext-strikethrough:4.6.2")
    implementation("io.noties.markwon:linkify:4.6.2")

    // Media3 ExoPlayer (Modern video player)
    implementation("androidx.media3:media3-exoplayer:1.8.0")
    implementation("androidx.media3:media3-ui:1.8.0")
    implementation("androidx.media3:media3-common:1.8.0")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Accompanist for tabs
    implementation("com.google.accompanist:accompanist-pager:0.36.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.36.0")


    // --- DEPENDENCY CLEANUP ---

    // Keep the navigation dependency (its version is managed separately)
    implementation("androidx.navigation:navigation-compose:2.7.5")


    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.runtime:runtime-livedata")

    // The rest of your dependencies are managed by your version catalog (libs) and are correct.
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}