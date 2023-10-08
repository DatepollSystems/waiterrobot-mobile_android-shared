import com.android.build.gradle.internal.dsl.NdkOptions.DebugSymbolLevel
import com.github.triplet.gradle.androidpublisher.ReleaseStatus
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import java.util.Date
import java.util.Properties

plugins {
    id("com.android.application")
    id("com.github.triplet.play") version "3.8.4"
    kotlin("android")
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
}

private val versionProperty by lazy {
    Properties().apply {
        File(project.projectDir, "version.properties").inputStream().use { load(it) }
    }
}

val SHARED_GROUP: String by project
val SHARED_BASE_VERSION: String by project

version = versionProperty.getProperty("androidVersion")
group = SHARED_GROUP

android {
    namespace = "org.datepollsystems.waiterrobot.android"
    compileSdk = Versions.androidCompileSdk

    androidResources {
        generateLocaleConfig = true
    }

    defaultConfig {
        applicationId = this@android.namespace

        minSdk = Versions.androidMinSdk
        targetSdk = Versions.androidTargetSdk
        buildToolsVersion = Versions.androidBuildTools

        versionName = version.toString()
        versionCode = run {
            // Generate VersionCode from VersionName (e.g. 1.2.3 -> 10203, 1.23.45 -> 12345)
            val (major, minor, patch) = versionName!!.split(".").map(String::toInt)
            major * 10_000 + minor * 100 + patch
        }

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        val keyPassword: String? = project.findProperty("keyPassword")?.toString()
        val storePassword: String? = project.findProperty("storePassword")?.toString()
        val keyStoreFile = file(".keys/app_sign.jks")

        // Only create signingConfig, when all needed configs are available
        if (keyPassword != null && storePassword != null && keyStoreFile.exists()) {
            create("release") {
                keyAlias = "WaiterRobot"
                storeFile = keyStoreFile
                this.keyPassword = keyPassword
                this.storePassword = storePassword
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
        }

        release {
            isMinifyEnabled = false // TODO enable proguard
            signingConfig = signingConfigs.findByName("release")
            ndk.debugSymbolLevel = DebugSymbolLevel.FULL.name
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.composeCompiler
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    flavorDimensions += "environment"

    productFlavors {
        create("lava") {
            dimension = "environment"
            applicationIdSuffix = ".lava"
            buildConfigField("String", "API_BASE", "\"https://lava.kellner.team/api\"")
            manifestPlaceholders["host"] = "lava.kellner.team"

            // Use time-based versionCode for lava to allow multiple build per "base version"
            // versionCode is limited to "2100000000" by google play.
            // If using epochSeconds this would overflow in 2036.
            // -> use epochMinutes (overflow would be in 5962).
            // (conversion to int is save as java int is bigger as the max versionCode allowed by google play)
            val epochMinutes = (Date().toInstant().epochSecond / 60).toInt()
            versionNameSuffix = "-lava-$epochMinutes"
            versionCode = epochMinutes
        }

        create("prod") {
            dimension = "environment"
            buildConfigField("String", "API_BASE", "\"https://my.kellner.team/api\"")
            manifestPlaceholders["host"] = "my.kellner.team"
        }
    }

    applicationVariants.all variant@{
        // Include the generated navigation sources
        kotlin.sourceSets {
            getByName(name) {
                kotlin.srcDir("${project.buildDir}/generated/ksp/$name/kotlin")
            }
        }

        // Write built version to file after creating a bundle (needed for ci, to create the version tag)
        if (this.name.endsWith("Release")) {
            tasks.findByName("publish${this.name.capitalizeAsciiOnly()}Bundle")!!.doLast {
                File(project.buildDir, "version.tag")
                    .writeText(this@variant.versionName)
            }
        }
    }
}

ksp {
    arg(
        "compose-destinations.codeGenPackageName",
        "org.datepollsystems.waiterrobot.android.generated.navigation"
    )
}

play {
    defaultToAppBundles.set(true)
    serviceAccountCredentials.set(file(".keys/service-account.json"))
    track.set("internal")
    releaseStatus.set(ReleaseStatus.COMPLETED)
}

val remoteBuild = project.findProperty("remoteBuild") == "true" // Default false
if (remoteBuild) {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/DatepollSystems/WaiterRobot-Shared-Android")
            credentials {
                username = project.property("GITHUB_PACKAGES_USERNAME") as String
                password = project.property("GITHUB_PACKAGES_PASSWORD") as String
            }
        }
    }
}

dependencies {
    if (remoteBuild) {
        implementation("${SHARED_GROUP}:shared-android:${SHARED_BASE_VERSION}.+")
    } else {
        implementation(project(":shared"))
    }

    implementation("androidx.lifecycle:lifecycle-process:${Versions.androidxLifecycle}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Versions.androidxLifecycle}")
    implementation("androidx.appcompat:appcompat:1.6.1")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    // Compose
    runtimeOnly("androidx.compose.compiler:compiler:${Versions.composeCompiler}")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.foundation:foundation:${Versions.compose}")
    implementation("androidx.compose.foundation:foundation-layout:${Versions.compose}")
    implementation("androidx.compose.ui:ui-graphics:${Versions.compose}")
    implementation("androidx.compose.ui:ui:${Versions.compose}")
    implementation("androidx.compose.ui:ui-tooling:${Versions.compose}")
    implementation("androidx.compose.ui:ui-tooling-preview:${Versions.compose}")
    implementation("androidx.compose.material:material:${Versions.compose}")
    implementation("androidx.compose.material:material-icons-core:${Versions.compose}")
    implementation("androidx.compose.material:material-icons-extended:${Versions.compose}")

    // Compose helpers
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Architecture (MVI)
    implementation("org.orbit-mvi:orbit-compose:${Versions.orbitMvi}")

    // Dependency injection
    implementation("io.insert-koin:koin-androidx-compose:3.4.6") // Not aligned with other koin version

    // SafeCompose Navigation Args
    implementation("io.github.raamcosta.compose-destinations:core:${Versions.composeDestinations}")
    ksp("io.github.raamcosta.compose-destinations:ksp:${Versions.composeDestinations}")

    // CameraX
    implementation("androidx.camera:camera-camera2:${Versions.camera}")
    implementation("androidx.camera:camera-view:${Versions.camera}")
    implementation("androidx.camera:camera-lifecycle:${Versions.camera}")

    // QrCode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // In-App-Update support
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
}
