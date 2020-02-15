plugins {
    kotlin("multiplatform")
    `maven-publish`
}

kotlin {
    jvm()
    js()

    val ktorVersion = "1.2.6"
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
            }
        }

        val jsMain by getting {
            dependencies {
                api("io.ktor:ktor-client-js:$ktorVersion")
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
