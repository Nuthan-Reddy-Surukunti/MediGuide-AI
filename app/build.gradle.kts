plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.firstaidapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.firstaidapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Add API key to default config
        buildConfigField("String", "GEMINI_API_KEY", "\"AIzaSyA-6SYM1ymRRmRpx5dQg-iTiUbNlT2rapI\"")
    }

    buildTypes {
        debug {
            // Debug configuration - API key inherited from defaultConfig
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Release configuration - API key inherited from defaultConfig
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
        dataBinding = true
        buildConfig = true
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Lifecycle components
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation Component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    ksp(libs.androidx.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Gson for JSON parsing
    implementation(libs.gson)

    // RecyclerView
    implementation(libs.androidx.recyclerview)

    // CardView
    implementation(libs.androidx.cardview)

    // ViewPager2
    implementation(libs.androidx.viewpager2)

    // FlexboxLayout
    implementation(libs.flexbox)

    // WorkManager for background initialization
    implementation(libs.androidx.work.runtime.ktx)

    // Lottie Animations for engaging first aid procedure demonstrations
    implementation(libs.lottie)

    // Google Play Services for Location
    implementation(libs.play.services.location)

    // Google AI SDK for Gemini
    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")

    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    // Firebase Auth
    implementation("com.google.firebase:firebase-auth-ktx")
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.room.testing)
}