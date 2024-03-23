pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://juggernaut0.github.io/m2/repository")
    }
}

rootProject.name = "multiplatform-utils"

include("graphql", "javalin", "ktor")

project(":graphql").name = "${rootProject.name}-graphql"
project(":javalin").name = "${rootProject.name}-javalin"
project(":ktor").name = "${rootProject.name}-ktor"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
