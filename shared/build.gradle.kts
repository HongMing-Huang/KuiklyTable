plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("com.tencent.kuikly-open.kuikly")
}

group = Publishing.kuiklyGroup
version = "1.0.0"

kotlin {
    androidTarget {
        publishLibraryVariantsGroupedByFlavor = true
        publishLibraryVariants("release")
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    macosX64()
    macosArm64()

    sourceSets {
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }

        val commonMain by getting {
            dependencies {
                implementation(project(":kuikly-table"))
                implementation("com.tencent.kuikly-open:core:${Version.getKuiklyVersion()}")
                implementation("com.tencent.kuikly-open:core-annotations:${Version.getKuiklyVersion()}")
            }
        }

        val appleMain by creating {
            dependsOn(commonMain)
        }

        val iosMain by creating {
            dependsOn(appleMain)
        }

        val macosMain by creating {
            dependsOn(appleMain)
        }

        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }

        val macosX64Main by getting { dependsOn(macosMain) }
        val macosArm64Main by getting { dependsOn(macosMain) }
    }
}

ksp {
    arg("pageName", "")
    arg("pageNameList", "")
}

dependencies {
    compileOnly("com.tencent.kuikly-open:core-ksp:${Version.getKuiklyVersion()}") {
        add("kspIosArm64", this)
        add("kspIosX64", this)
        add("kspIosSimulatorArm64", this)
        add("kspAndroid", this)
        add("kspMacosX64", this)
        add("kspMacosArm64", this)
    }
}

android {
    namespace = "com.tencent.kuikly.table.demo"
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = 21
    }

    sourceSets {
        named("main") {
            jniLibs.srcDirs("src/androidMain/libs/")
            assets.srcDirs("src/commonMain/assets")
        }
    }
}
