plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.roomify.app"
    compileSdk {
        version = release(37)
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    defaultConfig {

        applicationId = "com.roomify.app"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    buildTypes {

        getByName("debug") {
            buildConfigField(
                "String",
                "BASE_URL",
                "\"http://10.0.2.2:5179/\""
            )
        }

        getByName("release") {
            buildConfigField(
                "String",
                "BASE_URL",
                "\"https://example.invalid/\""
            )

            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    implementation(
        "org.maplibre.gl:android-sdk-opengl:13.7.1"
    )

    // ============================================================
    // ANDROIDX / UI
    // ============================================================

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)


    // ============================================================
    // NETWORK - RETROFIT
    // ============================================================

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)


    // ============================================================
    // NETWORK - OKHTTP
    // ============================================================

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)


    // ============================================================
    // JSON
    // ============================================================

    implementation(libs.gson)


    // ============================================================
    // COROUTINES
    // ============================================================

    implementation(libs.coroutines.android)


    // ============================================================
    // LIFECYCLE / VIEWMODEL
    // ============================================================

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)


    // ============================================================
    // TOKEN / SESSION STORAGE
    // ============================================================

    implementation(libs.androidx.datastore.preferences)


    // ============================================================
    // TEST
    // ============================================================

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.okhttp.mockwebserver)

    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    coreLibraryDesugaring(
        "com.android.tools:desugar_jdk_libs:2.1.5"
    )
}