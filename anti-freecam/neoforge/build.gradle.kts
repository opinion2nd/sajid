plugins {
    id("net.neoforged.moddev") version "2.0.78"
    java
}

group = "dev.thewindows.antifreecam"
version = project.property("modVersion") as String

base {
    archivesName.set("AntiFreeam-NeoForge")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

neoForge {
    version = project.property("neoForgeVersion") as String

    mods {
        create("antifreecam") {
            sourceSet(sourceSets.named("main").get())
        }
    }
}

dependencies {
    // Provided by Minecraft/NeoForge at runtime; only needed to compile shared common sources
    compileOnly("com.google.code.gson:gson:2.10.1")
}

// Compile the shared common module sources directly into the NeoForge jar
sourceSets {
    named("main") {
        java.srcDir("../common/src/main/java")
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(mapOf("version" to project.version))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}
