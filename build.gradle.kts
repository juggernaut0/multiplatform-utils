import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("multiplatform") version "1.5.10"
    `maven-publish`
    kotlin("plugin.serialization").version("1.5.10")
}

allprojects {
    group = "com.github.juggernaut0"
    version = "0.6.3-graphql-SNAPSHOT"

    repositories {
        mavenCentral()
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
    js {
        browser {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }

    val serializationVersion = "1.2.1"
    sourceSets {
        all {
            languageSettings {
                useExperimentalAnnotation("kotlin.RequiresOptIn")
            }
        }

        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                api("org.jetbrains.kotlinx:kotlinx-serialization-properties:$serializationVersion")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        val jsMain by getting {
            dependencies {
                api("com.github.juggernaut0:async-lite:0.2.0")
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

tasks {
    withType<Kotlin2JsCompile> {
        kotlinOptions {
            moduleKind = "umd"
            sourceMap = true
            sourceMapEmbedSources = "always"
        }
    }
}
