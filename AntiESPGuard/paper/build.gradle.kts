plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val paperApiVersion: String by rootProject
val packeteventsVersion: String by rootProject

dependencies {
    implementation(project(":common"))

    // Provided by the server at runtime — never shaded in.
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
    // Declared as `depend: [packetevents]` in plugin.yml, so compileOnly here.
    compileOnly("com.github.retrooper:packetevents-spigot:$packeteventsVersion")
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("plugin.yml") { expand(props) }

    // Single source of truth: ship the master config.yml from :common.
    from(project(":common").file("src/main/resources/antiespguard/config.yml")) {
        into("")
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveBaseName.set("AntiESPGuard-Paper")

    // Bundle :common + snakeyaml; relocate snakeyaml to avoid clashing with the
    // server's own bundled copy. (No minimize(): Shadow 8.1.1's minimizer uses an
    // ASM that cannot read Java 21 bytecode.)
    relocate("org.yaml.snakeyaml", "dev.opinion2nd.antiespguard.libs.snakeyaml")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
