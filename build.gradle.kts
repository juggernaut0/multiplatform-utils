import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("multiplatform") version "1.3.41"
    `maven-publish`
}

group = "com.github.juggernaut0"
version = "0.1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
}

kotlin {
    jvm()
    js()

    val serializationVersion = "0.11.1"
    val ktorVersion = "1.2.3"
    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$serializationVersion")
            }
        }

        val jvmMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationVersion")
                implementation("io.ktor:ktor-server-core:$ktorVersion")
                implementation("io.ktor:ktor-auth:$ktorVersion")
            }
        }

        val jsMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:$serializationVersion")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.2.2")
            }
        }
    }
}

tasks.withType<Kotlin2JsCompile>().forEach {
    it.kotlinOptions.freeCompilerArgs += "-Xuse-experimental=kotlin.Experimental"
    it.kotlinOptions.moduleKind = "umd"
}
