
plugins {
    kotlin("multiplatform") version "2.0.20"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0-Beta1"
}

repositories {
    mavenCentral()
}
kotlin {
    sourceSets{
        nativeMain{
            dependencies{
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
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
                linkerOpts("-mwindows")
            }
        }
    }
}