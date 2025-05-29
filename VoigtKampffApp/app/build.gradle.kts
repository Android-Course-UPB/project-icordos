plugins {
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"//"1.9.23"
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.0.21-1.0.25"
            //"1.9.23-1.0.19" // Use the KSP version compatible with your Kotlin version
}

android {
    namespace = "com.example.voigkampff"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.voigkampff"
        minSdk = 24
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // Check for the latest version
    // Kotlinx Serialization for JSON parsing
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // Or the latest version
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3") // Check for the latest version
    // Retrofit adapter for Kotlinx Serialization
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0") // Check for the latest version
    // ViewModel for lifecycle-aware data handling
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    // Coroutines for asynchronous operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // Check for latest
    // ... other dependencies
    implementation("androidx.compose.runtime:runtime-livedata:1.6.7")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Room
    val roomVersion = "2.6.1" // Use the latest stable version
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion") // For Java projects
    ksp("androidx.room:room-compiler:$roomVersion") // For Kotlin projects (use KSP)
    implementation("androidx.room:room-ktx:$roomVersion") // Kotlin Extensions and Coroutines support for Room
    implementation("androidx.compose.material3:material3:1.3.0-alpha03") // Use the latest stable M3 version
    // You'll also need navigation-compose if you haven't explicitly added it for NavHost
    implementation("androidx.navigation:navigation-compose:2.7.7") // Use the latest stable version
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.datastore:datastore-preferences:1.1.1") // Use the latest stable version

    // For ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3") // Use the latest stable version
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3") // For viewModelScope
}