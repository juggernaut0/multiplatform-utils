import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `maven-publish`
    kotlin("plugin.serialization")
}

dependencies {
    api(projects.multiplatformUtils)
    api("io.javalin:javalin:6.1.0")

    testImplementation(kotlin("test"))
    testImplementation("io.javalin:javalin-testtools:6.1.0")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.11")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<JavaCompile>().configureEach {
        options.release = 17
    }

    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "17"
    }
}
