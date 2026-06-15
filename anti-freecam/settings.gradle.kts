pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }
}

rootProject.name = "anti-freecam"

include(
    "common",
    "paper",
    "fabric",
    "neoforge"
)
