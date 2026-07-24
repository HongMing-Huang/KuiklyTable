// OHOS 独立构建配置
// 注意：OHOS 构建不包含 androidApp 模块，
// 因为 shared 模块在 OHOS 构建中不作为 Android library 编译。

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven {
            url = uri("https://mirrors.tencent.com/repository/maven-tencent/")
        }
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/gradle-plugins/")
        }
    }
}

val buildFileName = "build.ohos.gradle.kts"
rootProject.buildFileName = buildFileName

rootProject.name = "KuiklyTable"

include(":kuikly-table")
project(":kuikly-table").buildFileName = buildFileName

include(":shared")
project(":shared").buildFileName = buildFileName
