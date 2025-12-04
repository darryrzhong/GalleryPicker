pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    rulesMode.set(RulesMode.FAIL_ON_PROJECT_RULES)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "GalleryPicker"
//可视化依赖lib
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
//使用共享构建服务而不通过方法声明要求的任务Task.usesService将发出弃用警告
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

include(":app")
include(":PhotoPicker")
