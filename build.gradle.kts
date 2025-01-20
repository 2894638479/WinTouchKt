plugins {
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.20-Beta1"
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler) apply false
}

repositories {
    google()
    mavenCentral()
}


kotlin {
    sourceSets{
        commonMain{
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            }
        }
        nativeMain
        jvmMain {
            dependencies {
//                implementation(compose.runtime)
//                implementation(compose.foundation)
//                implementation(compose.material)
//                implementation(compose.ui)
//                implementation(compose.components.resources)
//                implementation(compose.components.uiToolingPreview)
//                implementation(libs.androidx.lifecycle.viewmodel)
//                implementation(libs.androidx.lifecycle.runtime.compose)
//                implementation(compose.desktop.currentOs)
//                implementation(libs.kotlinx.coroutines.swing)
            }
        }
    }
    jvm("jvm")
    mingwX64("nativeX64"){
        compilations.getByName("main"){
            cinterops {
                val Clib by creating {
                    defFile(project.file("lib/Clib.def"))
                    includeDirs(projectDir.absolutePath + "/lib")
                    packageName("libs.Clib")
                }
            }
        }
        binaries{
            executable(listOf(RELEASE)){
//                linkerOpts += "-mwindows"
            }
        }
        tasks.named("linkReleaseExecutableNativeX64"){
            doLast {
                exec {
                    commandLine("mt","-manifest","main.manifest","-outputresource:build/bin/nativeX64/releaseExecutable/WinTouchKt.exe")
                }
            }
        }
    }
}