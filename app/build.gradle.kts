import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.jetbrains.compose.compiler)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

val mapsApiKey: String =
    gradleLocalProperties(rootDir, providers).getProperty("MAPS_API_KEY")
        ?: System.getenv("MAPS_API_KEY")
        ?: ""

android {
    namespace = "com.thirtyhelens.ActiveDispatch"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.thirtyhelens.ActiveDispatch"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        vectorDrawables { useSupportLibrary = true }
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        }

//    buildTypes {
//        release {
//            isMinifyEnabled = true
//            isShrinkResources = true
//
//        }
//    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlin { jvmToolchain(17) }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    buildFeatures { compose = true }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

dependencies {
    implementation("com.google.android.material:material:1.12.0")

    // Android 12+ splash API (and compat attrs like windowSplashScreen*)
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(platform("androidx.compose:compose-bom:2024.08.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.foundation.layout)
    debugImplementation(libs.androidx.ui.tooling)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    implementation(libs.ktor.core)
    implementation(libs.ktor.okhttp)
    implementation(libs.ktor.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.kotlinx.serialization.json)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3:1.3.0-beta04")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.maps.android:maps-compose:2.11.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation(platform("com.google.firebase:firebase-bom:34.2.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics-ndk")
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
