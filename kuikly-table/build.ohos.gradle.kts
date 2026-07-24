plugins {
    kotlin("multiplatform")
    id("com.android.library")
    `maven-publish`
}

group = Publishing.kuiklyGroup
version = "1.0.0"

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }

    ohosArm64()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }

        val commonMain by getting {
            dependencies {
                api("com.tencent.kuikly-open:core:${Version.getKuiklyVersion()}")
            }
        }

        val appleMain by creating {
            dependsOn(commonMain)
        }

        val iosMain by creating {
            dependsOn(appleMain)
        }

        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }

        val ohosArm64Main by getting {
            dependsOn(commonMain)
        }
    }

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        val appleMain by sourceSets.getting
        when {
            konanTarget.family.isAppleFamily -> {
                val main by compilations.getting
                main.defaultSourceSet.dependsOn(appleMain)
            }
        }
    }
}

android {
    namespace = "com.tencent.kuikly.table"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }
}
