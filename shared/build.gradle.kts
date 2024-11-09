import com.codingfeline.buildkonfig.compiler.FieldSpec.Type
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBinary
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    `maven-publish`
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.touchlab.kmmbridge)
    alias(libs.plugins.touchlab.skie)
    alias(libs.plugins.kmmresources)
    alias(libs.plugins.realm)
}

val generatedLocalizationRoot: String =
    File(project.layout.buildDirectory.asFile.get(), "generated/localizations").absolutePath
val iosFrameworkName = "shared"

group = project.property("SHARED_GROUP") as String
version = project.property(
    if (project.hasProperty("AUTO_VERSION")) "AUTO_VERSION" else "SHARED_BASE_VERSION"
) as String
val sharedNamespace = "$group.shared"

kotlin {
    jvmToolchain(17)

    androidTarget()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            // Must be set to false for shared localization (otherwise resources are not available)
            isStatic = false
            freeCompilerArgs += "-Xobjc-generics"
            export(libs.sentry)
        }
    }

    // needed to export kotlin documentation in objective-c headers
    targets.withType<KotlinNativeTarget> {
        compilations["main"].kotlinOptions.freeCompilerArgs += "-Xexport-kdoc"
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCRefinement")
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
        }

        commonMain {
            // Include the generated localization source
            kotlin.srcDir("$generatedLocalizationRoot/commonMain/kotlin")

            dependencies {
                // Logger
                api(libs.touchlab.kermit)

                // Dependency injection
                implementation(libs.koin.core)

                // Architecture
                api(libs.orbit.core) // MVI
                api(libs.moko.mvvm) // ViewModelScope
                implementation(libs.touchlab.skie.annotations)

                // Permissions
                api(libs.moko.permissions)

                // Ktor (HTTP client)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.encoding)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.serialization.json)
                implementation(libs.ktor.client.auth)
                implementation(libs.ktor.client.logging)

                // Realm (Database)
                implementation(libs.realm)

                // SharedSettings
                implementation(libs.settings)
                implementation(libs.settings.coroutines)

                // Helper
                api(libs.kotlinx.datetime)
                // Also needed by android for ComposeDestination parameter serialization
                api(libs.kotlinx.serialization.json)

                api(libs.sentry)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        androidMain {
            // Include the generated localization source
            kotlin.srcDir("$generatedLocalizationRoot/androidMain/kotlin")

            dependencies {
                // Dependency injection
                api(libs.koin.android)

                // Ktor (HTTP client)
                implementation(libs.ktor.client.cio)
            }
        }

        iosMain {
            // Include the generated localization source
            kotlin.srcDir("$generatedLocalizationRoot/iosMain/kotlin")

            dependencies {
                // Ktor (HTTP client)
                implementation(libs.ktor.client.darwin)
            }
        }
    }

    sourceSets.all {
        languageSettings.optIn("kotlin.RequiresOptIn")
    }
}

android {
    namespace = sharedNamespace
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    // Include the generated localization string resources
    sourceSets["main"].res.srcDir("$generatedLocalizationRoot/androidMain/res")
}

addGithubPackagesRepository()
kmmbridge {
    mavenPublishArtifacts()
    spm()
}

kmmResourcesConfig {
    androidApplicationId.set(sharedNamespace) // appId of the shared module
    packageName.set("$sharedNamespace.generated.localization")
    defaultLanguage.set("en")
    input.set(File(project.projectDir, "localization.yml"))
    output.set(project.projectDir)
    srcFolder.set(generatedLocalizationRoot) // place the generated files in the build folder
}

buildkonfig {
    packageName = "$sharedNamespace.buildkonfig"
    defaultConfigs {
        buildConfigField(Type.STRING, "sharedVersion", version as String, const = true)
    }
}

tasks {
    val generateLocalizationsTask = named("generateLocalizations")

    // Plutil generates the localizations for ios
    val plutil = named("executePlutil") {
        dependsOn(generateLocalizationsTask)
    }

    // Generate the localizations for all ios targets
    listOf("IosX64", "IosArm64", "IosSimulatorArm64").forEach { arch ->
        // Ensure that localizations are up to date on compile
        named("compileKotlin$arch") {
            dependsOn(plutil)
        }
    }

    afterEvaluate {
        // Link the Sentry framework to the iOS targets
        val action = Action<KotlinNativeTarget> target@{
            val frameworkArchitecture = when (name) {
                "iosSimulatorArm64", "iosX64" -> "ios-arm64_x86_64-simulator"
                "iosArm64" -> "ios-arm64"
                else -> {
                    logger.warn("Skipping linking of Sentry for target $name - unsupported architecture.")
                    return@target
                }
            }

            val frameworkPath =
                project.file("Sentry-Dynamic.xcframework/$frameworkArchitecture").absolutePath
            val action = Action<NativeBinary> binary@{
                if (this is TestExecutable) {
                    linkerOpts("-rpath", frameworkPath, "-F$frameworkPath")
                }

                if (this is Framework) {
                    linkerOpts("-F$frameworkPath")
                    logger.info("Linked framework for target ${this@target.name} from $frameworkPath")
                }
            }
            binaries.all(action)
        }
        kotlin.targets.withType<KotlinNativeTarget>()
            .matching { it.konanTarget.family.isAppleFamily }
            .all(action)

        // Copy the generated iOS localizations to the framework and set some task dependencies
        listOf("Release", "Debug").forEach { buildType ->
            named("assembleShared${buildType}XCFramework") {
                dependsOn(generateLocalizationsTask)
                doLast {
                    listOf("ios-arm64", "ios-arm64_x86_64-simulator").forEach { arch ->
                        copy {
                            from("$generatedLocalizationRoot/commonMain/resources/ios")
                            into(
                                File(
                                    project.layout.buildDirectory.asFile.get(),
                                    "XCFrameworks/${buildType.lowercase()}/" +
                                        "$iosFrameworkName.xcframework/$arch/$iosFrameworkName.framework"
                                )
                            )
                        }
                    }
                }
            }
            listOf("X64", "Arm64", "SimulatorArm64").forEach { arch ->
                findByName("skiePackageCustomSwift${buildType}FrameworkIos$arch")?.apply {
                    dependsOn(generateLocalizationsTask)
                }
                findByName("skieProcessSwiftSourcesIos$arch")?.apply {
                    dependsOn(generateLocalizationsTask)
                }
            }
        }

        // Make sure that the localizations are up to date for release
        named("androidReleaseSourcesJar") {
            dependsOn(generateLocalizationsTask)
        }
    }

    // Make sure that the localizations are always up to date
    named("preBuild") {
        dependsOn(named("generateLocalizations"))
    }
}

detekt {
    source.from(
        "src/androidMain/kotlin",
        "src/commonMain/kotlin",
        "src/iosMain/kotlin",
    )
}

skie {
    analytics {
        disableUpload.set(true)
        enabled.set(false)
    }
}
