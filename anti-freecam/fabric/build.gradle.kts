plugins {
    id("fabric-loom") version "1.9.+"
}

dependencies {
    implementation(project(":common"))

    minecraft("com.mojang:minecraft:${rootProject.property("minecraftVersion")}")
    mappings("net.fabricmc:yarn:${rootProject.property("yarnMappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("fabricLoaderVersion")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${rootProject.property("fabricApiVersion")}")

    include(project(":common"))
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version))
    }
}
