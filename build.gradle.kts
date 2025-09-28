import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

plugins {
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0"
    alias(libs.plugins.kotlinMultiplatform)
}

repositories {
    mavenCentral()
}


kotlin {
    sourceSets{
        nativeMain{
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
            }
        }
    }
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
                linkerOpts += "-lcomdlg32"
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
val compileKotlinNativeX64: KotlinNativeCompile by tasks
compileKotlinNativeX64.compilerOptions {
    freeCompilerArgs.set(listOf("-Xcontext-parameters","-Xnested-type-aliases"))
}