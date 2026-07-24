plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
    id("com.tencent.kuiklybase.knoi.plugin")
}

knoi {
    tsGenDir = projectDir.absolutePath + "/../ohosApp/entry/src/main/ets/ts-api/"
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targets.all {
        compilations.all {
            kotlinOptions {
                freeCompilerArgs += listOf(
                    "-Xinline-classes",
                    "-opt-in=kotlin.ExperimentalStdlibApi",
                    "-opt-in=kotlinx.cinterop.ExperimentalForeignApi",
                    "-opt-in=kotlin.experimental.ExperimentalNativeApi",
                    "-opt-in=kotlin.contracts.ExperimentalContracts"
                )
            }
        }
    }

    ohosArm64 {
        binaries.sharedLib("shared") {
            freeCompilerArgs += "-Xadd-light-debug=enable"
            linkerOpts += "--build-id=sha1"
            if (buildType == org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.RELEASE) {
                val CLANG_OPT_FLAGS = "-Os   -ffunction-sections"
                val CLANG_FLAGS = "clangOptFlags.ios_arm64=$CLANG_OPT_FLAGS;clangDebugFlags.ios_arm64=$CLANG_OPT_FLAGS;clangOptFlags.ohos_arm64=$CLANG_OPT_FLAGS;clangDebugFlags.ohos_arm64=$CLANG_OPT_FLAGS"
                freeCompilerArgs += "-Xoverride-konan-properties=$CLANG_FLAGS"
                linkerOpts += "--pack-dyn-relocs=relr"
                linkerOpts += "--gc-sections"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":kuikly-table"))
                implementation("com.tencent.kuikly-open:core:${Version.getKuiklyVersion()}")
                implementation("com.tencent.kuikly-open:core-annotations:${Version.getKuiklyVersion()}")
            }
        }
    }
}

dependencies {
    add("kspOhosArm64", "com.tencent.kuikly-open:core-ksp:${Version.getKuiklyVersion()}")
}

ksp {
    arg("pageName", "")
    arg("pageNameList", "")
}
