import com.moowork.gradle.node.npm.NpxTask
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("multiplatform") version "1.3.61"
    `maven-publish`
    id("com.github.node-gradle.node") version "2.2.1"
    kotlin("plugin.serialization").version("1.3.61")
}

allprojects {
    group = "com.github.juggernaut0"
    version = "0.2.0"

    repositories {
        mavenCentral()
        maven { url = uri("https://kotlin.bintray.com/kotlinx") }
        maven { url = uri("https://juggernaut0.github.io/m2/repository") }
    }

    plugins.withId("maven-publish") {
        publishing {
            repositories {
                maven {
                    name = "pages"
                    url = uri("$rootDir/pages/m2/repository")
                }
            }
        }
    }
}

kotlin {
    jvm()
    js()

    val serializationVersion = "0.14.0"
    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$serializationVersion")
            }
        }

        val jvmMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationVersion")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        val jsMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:$serializationVersion")
                api("com.github.juggernaut0:async-lite:0.1.0")
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

node {
    download = true
    version = "12.16.0"
}

tasks {
    withType<Kotlin2JsCompile>().forEach {
        //it.kotlinOptions.freeCompilerArgs += "-Xuse-experimental=kotlin.Experimental"
        it.kotlinOptions.moduleKind = "umd"
    }

    val populateNodeModules by registering(Copy::class) {
        val jsJar = named<Jar>("jsJar")
        dependsOn(jsJar)

        from(zipTree(jsJar.flatMap { it.archiveFile }).matching { include("*.js") })

        configurations["jsTestCompileClasspath"].filter { it.isFile }.forEach {
            from(zipTree(it.absolutePath).matching { include("*.js") })
        }

        into("$buildDir/node_modules")
    }

    val runJest by registering(NpxTask::class) {
        dependsOn("jsTestClasses", npmInstall)
        dependsOn(populateNodeModules)
        command = "jest"
    }

    named("jsTest") {
        dependsOn(runJest)
    }
}
