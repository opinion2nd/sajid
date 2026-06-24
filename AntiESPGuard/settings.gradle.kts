pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/")                 // Fabric Loom
        maven("https://maven.neoforged.net/releases")         // NeoForged / ModDevGradle
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "AntiESPGuard"

include("common")
include("paper")
include("fabric")
include("neoforge")
