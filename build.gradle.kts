// 根目录 build.gradle.kts —— 必须有 pluginManagement
pluginManagement {
    repositories {
        // 顺序很重要：gradlePluginPortal() 必须在第一位
        gradlePluginPortal()
        google()
        mavenCentral()
        // 国内镜像兜底（可选）
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
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
