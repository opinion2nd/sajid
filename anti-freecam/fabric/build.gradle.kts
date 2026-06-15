plugins {
    id("fabric-loom") version "1.9.2"
    java
}

group = "dev.thewindows.antifreecam"
version = project.property("modVersion") as String

base {
    archivesName.set("AntiFreeam-Fabric")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraftVersion")}")
    mappings("net.fabricmc:yarn:${property("yarnMappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("fabricLoaderVersion")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabricApiVersion")}")

    // Provided by Minecraft at runtime; only needed to compile the shared common sources
    compileOnly("com.google.code.gson:gson:2.10.1")
}

// Compile the shared common module sources directly into the Fabric jar
sourceSets {
    named("main") {
        java.srcDir("../common/src/main/java")
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}
