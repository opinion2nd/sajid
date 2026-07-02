plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.opinion2nd"
version = "1.0.5"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")   // Paper API
    maven("https://repo.codemc.io/repository/maven-releases/")   // PacketEvents
}

dependencies {
    // Provided by the server at runtime — never shaded in.
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    // We `depend: [packetevents]` in plugin.yml, so it is compileOnly here too.
    compileOnly("com.github.retrooper:packetevents-spigot:2.5.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
