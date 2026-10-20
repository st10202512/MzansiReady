plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services") // Required for Firebase
}

android {
    namespace = "com.mzansiready.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mzansiready.app"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    // --- CORE ANDROID UI ---
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // --- LIFECYCLE & ACTIVITY ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")
    implementation("androidx.activity:activity-ktx:1.9.1")

    // --- NETWORKING (REST API) ---
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // --- LOCAL STORAGE & SECURITY ---
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // --- BACKGROUND WORK (Offline Sync) ---
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // =====================================================================
    // --- PART 3 ADDITIONS: FIREBASE & GOOGLE AUTH ---
    // =====================================================================

    // Firebase Authentication (For SSO)
    implementation("com.google.firebase:firebase-auth:23.1.0")

    // Firebase Cloud Messaging (For Push Notifications)
    implementation("com.google.firebase:firebase-messaging:24.0.0")

    // Firebase Firestore (NoSQL Database)
    implementation("com.google.firebase:firebase-firestore:25.0.0")

    // Firebase Storage (Blob Storage)
    implementation("com.google.firebase:firebase-storage:21.0.0")

    // Google Play Services Auth (Required for the SignInButton UI component)
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Credential Manager (For the modern Google Sign-In flow)
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // --- SPLASH SCREEN ---
    implementation("androidx.core:core-splashscreen:1.0.1")

    // =====================================================================
    // --- TESTING ---
    // =====================================================================
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}