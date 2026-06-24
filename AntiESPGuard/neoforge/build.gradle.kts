plugins {
    java
    id("net.neoforged.moddev") version "1.0.21"
}

val minecraftVersion: String by rootProject
val neoforgeVersion: String by rootProject

neoForge {
    version.set(neoforgeVersion)

    // Mixins are declared in the mods.toml + this config file.
    mods {
        create("antiespguard") {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
    implementation(project(":common"))
    // snakeyaml (transitive from :common) must be on the runtime/jar classpath.
    jarJar("org.yaml:snakeyaml:2.2")
    jarJar(project(":common"))
}

tasks.processResources {
    val props = mapOf("version" to project.version, "mcVersion" to minecraftVersion)
    inputs.properties(props)
    filesMatching("META-INF/neoforge.mods.toml") { expand(props) }

    from(project(":common").file("src/main/resources/antiespguard/config.yml")) {
        into("antiespguard")
    }
}
