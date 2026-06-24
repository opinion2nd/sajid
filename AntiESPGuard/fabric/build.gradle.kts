plugins {
    java
    id("fabric-loom") version "1.7-SNAPSHOT"
}

val minecraftVersion: String by rootProject
val fabricLoaderVersion: String by rootProject
val fabricApiVersion: String by rootProject

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    // Use official Mojang mappings so NMS names match the Paper module and the
    // NeoForge module (single mental model across platforms).
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

    // Shared, platform-agnostic logic.
    implementation(project(":common"))
    include(project(":common"))
    // snakeyaml is pulled in transitively by :common and bundled via include().
    include("org.yaml:snakeyaml:2.2")
}

tasks.processResources {
    val props = mapOf("version" to project.version, "mcVersion" to minecraftVersion)
    inputs.properties(props)
    filesMatching("fabric.mod.json") { expand(props) }

    // Single source of truth for the config template.
    from(project(":common").file("src/main/resources/antiespguard/config.yml")) {
        into("antiespguard")
    }
}
