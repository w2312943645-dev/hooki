// 关键：告诉 Gradle 去哪里找 Android Gradle Plugin
pluginManagement {
    repositories {
        gradlePluginPortal()   // 必须在第一位
        google()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.android") {
                useModule("com.android.tools.build:gradle:${requested.version}")
            }
        }
    }
}

plugins {
    id("com.android.application") version "8.4.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("org.lsposed.lsp") version "1.9.2" apply false
}
