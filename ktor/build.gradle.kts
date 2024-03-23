plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    `maven-publish`
}

kotlin {
    jvm()
    js(IR) {
        browser {
            testTask {
                useKarma {
                    useFirefoxHeadless()
                }
            }
        }
    }

    val ktorVersion = "2.3.8"
    sourceSets {
        commonMain {
            dependencies {
                api(project(":"))
                api("io.ktor:ktor-client-core:$ktorVersion")
            }
        }

        named("jvmMain") {
            dependencies {
                api("io.ktor:ktor-server-core:$ktorVersion")
                api("io.ktor:ktor-server-auth:$ktorVersion")
                compileOnly("io.ktor:ktor-server-status-pages:$ktorVersion")
            }
        }

        named("jvmTest") {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("io.ktor:ktor-server-test-host:$ktorVersion")
                implementation("io.ktor:ktor-client-mock:$ktorVersion")
                implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
            }
        }

        named("jsMain") {
            dependencies {
                api("io.ktor:ktor-client-js:$ktorVersion")
            }
        }

        named("jsTest") {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
