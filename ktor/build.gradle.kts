plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    `maven-publish`
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.useOldBackend = true // TODO test with IR backend later
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

    val ktorVersion = "1.5.3"
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":"))
                api("io.ktor:ktor-client-core:$ktorVersion")
            }
        }

        val jvmMain by getting {
            dependencies {
                api("io.ktor:ktor-server-core:$ktorVersion")
                api("io.ktor:ktor-auth:$ktorVersion")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("io.ktor:ktor-server-test-host:$ktorVersion")
            }
        }

        val jsMain by getting {
            dependencies {
                api("io.ktor:ktor-client-js:$ktorVersion")
                // TODO webpack doesn't polyfill these anymore, maybe can be removed when ktor updates
                implementation(npm("crypto-browserify", "3.12"))
                implementation(npm("stream-browserify", "3.0"))
                implementation(npm("buffer", "6.0"))
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
