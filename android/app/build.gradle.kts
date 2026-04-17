import java.util.Properties
import java.io.FileInputStream

val keystorePropertiesFile = rootProject.file("key.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

plugins {
    id("com.android.application")
    id("kotlin-android")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

android {
    namespace = "com.exampleapp.flutter_devops_lab"
    compileSdk = flutter.compileSdkVersion
    ndkVersion = flutter.ndkVersion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    defaultConfig {
        applicationId = "com.exampleapp.flutter_devops_lab"
        // You can update the following values to match your application needs.
        // For more information, see: https://flutter.dev/to/review-gradle-config.
        minSdk = flutter.minSdkVersion
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName
    }

    // ────────── SIGNING CONFIGURATION ──────────
    signingConfigs {
        create("release") {
            val storeFile: String? = keystoreProperties["storeFile"]?.toString()
            val storePassword: String? = keystoreProperties["storePassword"]?.toString()
            val keyAlias: String? = keystoreProperties["keyAlias"]?.toString()
            val keyPassword: String? = keystoreProperties["keyPassword"]?.toString()

            if (storeFile != null && storePassword != null && keyAlias != null && keyPassword != null) {
                this.storeFile = file(storeFile)
                this.storePassword = storePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        debug {
            // Debug automatically uses the default debug keystore (~/.android/debug.keystore)
            // No explicit configuration needed
        }

        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

flutter {
    source = "../.."
}
