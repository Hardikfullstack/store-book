plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.storebook.inventoryapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.storebook.inventoryapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Strip out unused languages from dependencies (saves 1-3MB)
        resourceConfigurations.addAll(listOf("en", "it", "hi", "es", "fr", "de", "pt", "ru", "gu"))
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            firebaseCrashlytics {
                mappingFileUploadEnabled = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // 1. Platform & Core
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)

    // 2. Compose UI (Material 3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.runtime)
    implementation("androidx.compose.material:material-icons-core")

    // 3. Lifecycle & Navigation
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.navigation.compose)

    // 4. Firebase (Using BOM for version alignment)
    implementation(platform(libs.firebase.bom))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-firestore")

    // 5. Features & Utilities
    implementation(libs.coil.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.google.accompanist.permissions)
    implementation(libs.google.play.services.ads)
    implementation(libs.google.play.services.mlkit.document-scanner)
    implementation(libs.google.play.services.code.scanner)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.pdfbox.android)
    implementation(libs.android.pdf.viewer)
    implementation(libs.reorderable)
    implementation(libs.google.play.review.ktx)
    implementation(libs.google.play.app.update-ktx)
    implementation(libs.android.billing.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.lottie.compose)
    implementation(libs.poi)
    implementation(libs.poi.ooxml)

    // 6. Support Libraries
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.camera.camera2.pipe)

    // 7. Debug / Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

ktlint {
    version.set("1.5.0")
    android.set(true)
    ignoreFailures.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
    }
    filter {
        exclude("**/generated/**")
        include("**/java/**")
        include("**/kotlin/**")
    }
}
