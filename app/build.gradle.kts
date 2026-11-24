plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ps5gamehub.android"
    compileSdk = 35

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("ANDROID_KEYSTORE_PATH")
            val storePasswordValue = System.getenv("ANDROID_KEYSTORE_PASSWORD")
            val keyAliasValue = System.getenv("ANDROID_KEY_ALIAS")
            val keyPasswordValue = System.getenv("ANDROID_KEY_PASSWORD")

            // Configure signing only when the complete signing material is available.
            // The release task is separately guarded below, so a missing key can never
            // silently produce an unsigned release APK.
            if (!keystorePath.isNullOrBlank() &&
                !storePasswordValue.isNullOrBlank() &&
                !keyAliasValue.isNullOrBlank() &&
                !keyPasswordValue.isNullOrBlank() &&
                file(keystorePath).isFile
            ) {
                storeFile = file(keystorePath)
                storePassword = storePasswordValue
                keyAlias = keyAliasValue
                keyPassword = keyPasswordValue
            }
        }
    }

    defaultConfig {
        applicationId = "com.ps5gamehub.android"
        minSdk = 31
        targetSdk = 35
        versionCode = providers.gradleProperty("appVersionCode").orElse("1").get().toInt()
        versionName = providers.gradleProperty("appVersion").orElse("0.1.0").get()
    }

    // Keep Java and Kotlin bytecode targets aligned for reproducible CI builds.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }
    }
}

val validateReleaseSigning by tasks.registering {
    group = "verification"
    description = "Ensures a complete release signing identity is available."

    doLast {
        val requiredVariables = listOf(
            "ANDROID_KEYSTORE_PATH",
            "ANDROID_KEYSTORE_PASSWORD",
            "ANDROID_KEY_ALIAS",
            "ANDROID_KEY_PASSWORD"
        )
        val missing = requiredVariables.filter { System.getenv(it).isNullOrBlank() }
        if (missing.isNotEmpty()) {
            throw GradleException(
                "Refusing to build an unsigned release. Missing signing environment variables: " +
                    missing.joinToString(", ")
            )
        }

        val keystorePath = System.getenv("ANDROID_KEYSTORE_PATH")!!
        if (!file(keystorePath).isFile) {
            throw GradleException("Refusing to build release: signing keystore file does not exist.")
        }
    }
}

// Android's preReleaseBuild is a dependency of release APK and bundle packaging.
// Debug builds remain independent of production signing secrets.
tasks.configureEach {
    if (name == "preReleaseBuild") {
        dependsOn(validateReleaseSigning)
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    testImplementation("junit:junit:4.13.2")
}
