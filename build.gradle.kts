import dev.twarner.gradle.DownloadFirefoxTask
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform") version "1.9.22"
    `maven-publish`
    kotlin("plugin.serialization") version "1.9.22"
    id("dev.twarner.download-firefox") version "0.3.6"
}

allprojects {
    group = "com.github.juggernaut0"

    repositories {
        mavenCentral()
        maven("https://juggernaut0.github.io/m2/repository")
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

val downloadFirefox = tasks.named("downloadFirefox", DownloadFirefoxTask::class) {
    version.set("122.0.1")
}

kotlin {
    jvm()
    js(IR) {
        browser {
            testTask {
                if (System.getProperty("os.name").contains("Mac")) {
                    doFirst {
                        environment("FIREFOX_BIN", "/Applications/Firefox.app/Contents/MacOS/firefox")
                    }
                } else {
                    dependsOn(downloadFirefox)
                    doFirst {
                        environment("FIREFOX_BIN", downloadFirefox.flatMap { it.outputBin }.get().asFile.absolutePath)
                    }
                }
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }

    val serializationVersion = "1.6.2"
    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.RequiresOptIn")
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
                implementation("io.mockk:mockk:1.13.9")
            }
        }

        val jsMain by getting {
            dependencies {
                api("com.github.juggernaut0:async-lite:0.3.0")
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
    withType<Kotlin2JsCompile>().configureEach {
        kotlinOptions {
            moduleKind = "umd"
            sourceMap = true
            sourceMapEmbedSources = "always"
        }
    }

    withType<JavaCompile>().configureEach {
        options.release = 17
    }

    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "17"
    }
}
