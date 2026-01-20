pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            version("agp", "8.4.1")
            version("kotlin", "2.0.0")
            version("compose-multiplatform", "1.6.10")
            version("androidx-activityCompose", "1.9.3")
            version("androidx-appcompat", "1.7.0")
            version("androidx-constraintlayout", "2.1.4")
            version("androidx-core-ktx", "1.13.1")
            version("androidx-espresso-core", "3.6.1")
            version("androidx-lifecycle", "2.8.3")
            version("androidx-material", "1.12.0")
            version("androidx-test-junit", "1.2.1")
            version("compose-bom", "2024.09.00")
            version("junit", "4.13.2")
            version("kotlinx-coroutines", "1.9.0")
            version("kotlinx-serialization", "1.7.3")
            version("ktor", "3.0.1")
            version("coil", "3.0.0-rc01")

            library("kotlin-test", "org.jetbrains.kotlin", "kotlin-test").versionRef("kotlin")
            library("androidx-activity-compose", "androidx.activity", "activity-compose").versionRef("androidx-activityCompose")
            library("androidx-lifecycle-viewmodel", "org.jetbrains.androidx.lifecycle", "lifecycle-viewmodel").versionRef("androidx-lifecycle")
            library("androidx-lifecycle-runtime-compose", "org.jetbrains.androidx.lifecycle", "lifecycle-runtime-compose").versionRef("androidx-lifecycle")
            library("kotlinx-coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").versionRef("kotlinx-coroutines")
            library("kotlinx-coroutines-android", "org.jetbrains.kotlinx", "kotlinx-coroutines-android").versionRef("kotlinx-coroutines")
            library("kotlinx-serialization-json", "org.jetbrains.kotlinx", "kotlinx-serialization-json").versionRef("kotlinx-serialization")
            library("ktor-client-core", "io.ktor", "ktor-client-core").versionRef("ktor")
            library("ktor-client-okhttp", "io.ktor", "ktor-client-okhttp").versionRef("ktor")
            library("ktor-client-darwin", "io.ktor", "ktor-client-darwin").versionRef("ktor")
            library("ktor-client-content-negotiation", "io.ktor", "ktor-client-content-negotiation").versionRef("ktor")
            library("ktor-serialization-kotlinx-json", "io.ktor", "ktor-serialization-kotlinx-json").versionRef("ktor")
            library("ktor-client-logging", "io.ktor", "ktor-client-logging").versionRef("ktor")
            library("coil-compose", "io.coil-kt.coil3", "coil-compose").versionRef("coil")
            library("coil-network-ktor", "io.coil-kt.coil3", "coil-network-ktor3").versionRef("coil")
            
            plugin("androidApplication", "com.android.application").versionRef("agp")
            plugin("androidLibrary", "com.android.library").versionRef("agp")
            plugin("jetbrainsCompose", "org.jetbrains.compose").versionRef("compose-multiplatform")
            plugin("composeCompiler", "org.jetbrains.kotlin.plugin.compose").versionRef("kotlin")
            plugin("kotlinAndroid", "org.jetbrains.kotlin.android").versionRef("kotlin")
            plugin("kotlinMultiplatform", "org.jetbrains.kotlin.multiplatform").versionRef("kotlin")
            plugin("kotlinSerialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")
        }
    }
}

rootProject.name = "GommiNasPlayer"
include(":app")
include(":shared")
