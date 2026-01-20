import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
}

val room_version = "2.7.0-alpha11"
val ktor_version = "2.3.11"

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    val xcf = XCFramework()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { 
        it.binaries.framework { 
            baseName = "shared"
            xcf.add(this)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:$ktor_version")
                implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
                implementation("io.ktor:ktor-client-logging:$ktor_version")

                implementation("androidx.room:room-runtime:$room_version")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:$ktor_version")
                implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
                implementation("com.squareup.okhttp3:okhttp")
                implementation("androidx.sqlite:sqlite-bundled:2.5.0-alpha01")

                implementation("androidx.room:room-ktx:$room_version")
                implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2")
                implementation("androidx.media3:media3-exoplayer:1.4.1")
                implementation("androidx.media3:media3-ui:1.4.1")
                implementation("androidx.media3:media3-common:1.4.1")
                implementation("androidx.media3:media3-datasource-okhttp:1.4.1")
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)
            iosX64().compilations.getByName("main").defaultSourceSet.dependsOn(this)
            iosArm64().compilations.getByName("main").defaultSourceSet.dependsOn(this)
            iosSimulatorArm64().compilations.getByName("main").defaultSourceSet.dependsOn(this)
            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktor_version")
            }
        }
    }
}

android {
    namespace = "com.nas.musicplayer.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 26
        buildConfigField("String", "GEMINI_API_KEY", "\"YOUR_API_KEY\"")
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    add("ksp", "androidx.room:room-compiler:$room_version")
}