plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    `maven-publish`
}

kotlin {
    jvm {
        // TODO required for tests that use local serializable classes, can remove in kxs 1.5.30
        compilations.named("test") {
            kotlinOptions.useOldBackend = true
        }
    }
    js {
        browser {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                useExperimentalAnnotation("kotlin.RequiresOptIn")
            }
        }

        val commonMain by getting {
            dependencies {
                api(project(":"))
            }
        }

        val jvmMain by getting {
            dependencies {
                api("com.graphql-java:graphql-java:2020-12-21T21-14-06-a12f84b")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.0")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        val jsMain by getting {
            dependencies {
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
