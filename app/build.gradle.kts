plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.healthyscan.app"

    // Google Play requires new apps/updates to target API 36 (Android 16)
    // starting August 31, 2026. See:
    // https://support.google.com/googleplay/android-developer/answer/11926878
    compileSdk = 36

    defaultConfig {
        applicationId = "com.healthyscan.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // Keep the OFF (OpenFoodFacts) API base URL configurable per build type
        buildConfigField("String", "OFF_BASE_URL", "\"https://world.openfoodfacts.org/\"")
    }

    signingConfigs {
        // Fill this in with your own upload keystore before building a release AAB.
        // Do NOT commit real keystore passwords to git — use a local.properties
        // or environment variables instead (see README).
        create("release") {
            val keystorePath = System.getenv("HEALTHYSCAN_KEYSTORE_PATH")
            if (keystorePath != null) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("HEALTHYSCAN_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("HEALTHYSCAN_KEY_ALIAS")
                keyPassword = System.getenv("HEALTHYSCAN_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (System.getenv("HEALTHYSCAN_KEYSTORE_PATH") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core / Compose
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation(platform("androidx.compose:compose-bom:2024.09.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.1")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Classic Material Components theme (needed for the XML splash-screen theme,
    // Theme.Material3.DayNight.NoActionBar lives here, not in Compose Material3)
    implementation("com.google.android.material:material:1.12.0")

    // Per-app language (EL/EN switch), works back to minSdk via AppCompat backport
    implementation("androidx.appcompat:appcompat:1.7.0")

    // CameraX (barcode scan preview)
    val cameraxVersion = "1.3.4"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // ML Kit barcode scanning (on-device)
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // Networking (Open Food Facts product lookup)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Local storage (history / favorites / basket)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Preferences (theme choice, language choice, onboarding answers)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Image loading (product photos)
    implementation("io.coil-kt:coil-compose:2.7.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.02"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
