pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://juggernaut0.github.io/m2/repository")
    }
}

rootProject.name = "multiplatform-utils"

include("javalin", "ktor")

project(":javalin").name = "${rootProject.name}-javalin"
project(":ktor").name = "${rootProject.name}-ktor"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
