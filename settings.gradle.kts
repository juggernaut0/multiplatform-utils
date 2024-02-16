pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://juggernaut0.github.io/m2/repository")
    }
}

rootProject.name = "multiplatform-utils"

include("ktor")

project(":ktor").name = "${rootProject.name}-ktor"
