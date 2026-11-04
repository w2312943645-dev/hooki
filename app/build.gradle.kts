plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.lsposed.lsp")
}

android {
    namespace = "com.example.globalvpnhook"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.globalvpnhook"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("org.lsposed:lsp-api:1.9.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
}
