plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    jvm()

    targets.all {
        compilations.all {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.kotlinx.serialization.json)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0-RC.2")
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native)
        }

        // e31-s1: JVM target for unit testing without Android instrumented setup
        val jvmMain by getting {
            dependencies {
                // SQLDelight JDBC driver — runs real SQLite queries in-memory on JVM
                implementation("app.cash.sqldelight:sqlite-driver:2.0.2")
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                // JUnit5 for modern test lifecycle
                implementation("org.junit.jupiter:junit-jupiter:5.11.4")
                implementation("org.junit.platform:junit-platform-launcher:1.11.4")
                // MockK for mocking ViewModels/repos in isolation
                implementation("io.mockk:mockk:1.13.13")
                // Turbine — test Kotlin Flows deterministically (SyncStatus, StateFlows)
                implementation("app.cash.turbine:turbine:1.2.0")
                // Coroutines test utils (TestDispatcher, runTest)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
            }
        }
    }
}

android {
    namespace = "com.storebook.inventoryapp.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

sqldelight {
    databases {
        create("StoreBookDatabase") {
            packageName.set("com.storebook.inventoryapp.shared.data.local")
        }
    }
}
