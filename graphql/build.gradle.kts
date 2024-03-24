plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    `maven-publish`
    id("dev.twarner.download-firefox")
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
                    dependsOn(tasks.downloadFirefox)
                    doFirst {
                        environment("FIREFOX_BIN", tasks.downloadFirefox.flatMap { it.outputBin }.get().asFile.absolutePath)
                    }
                }
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.RequiresOptIn")
            }
        }

        val commonMain by getting {
            dependencies {
                api(project(":"))
            }
        }

        val jvmMain by getting {
            dependencies {
                api("com.graphql-java:graphql-java:21.4")
                compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
