plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "com.ultimatedungeon"
version = "1.0.0-SNAPSHOT"
description = "UltimateDungeon — Premium procedural dungeon plugin for Paper servers"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

repositories {
    mavenCentral()

    // Paper API
    maven("https://repo.papermc.io/repository/maven-public/")

    // Vault (economy API)
    maven("https://jitpack.io")
}

dependencies {
    // ── Paper API ────────────────────────────────────────────────────────────
    // Adventure (MiniMessage, BossBar, ActionBar) is bundled with Paper.
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    // ── Economy ──────────────────────────────────────────────────────────────
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    // ── Database ─────────────────────────────────────────────────────────────
    // SQLite JDBC driver — shaded into the final jar.
    implementation("org.xerial:sqlite-jdbc:3.47.1.0")

    // HikariCP connection pool for MySQL — shaded into the final jar.
    implementation("com.zaxxer:HikariCP:6.2.1")

    // MySQL Connector/J — shaded into the final jar.
    implementation("com.mysql:mysql-connector-j:9.1.0")

    // ── Caching ──────────────────────────────────────────────────────────────
    // Caffeine in-memory cache — shaded into the final jar.
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    // ── Static analysis annotations ──────────────────────────────────────────
    compileOnly("org.jetbrains:annotations:26.0.1")
}

// ── Shadow (fat-jar) configuration ───────────────────────────────────────────
tasks.shadowJar {
    archiveClassifier = ""
    archiveFileName = "UltimateDungeon-${project.version}.jar"

    // Relocate shaded dependencies to avoid classpath conflicts on the server.
    relocate("org.sqlite",               "com.ultimatedungeon.libs.sqlite")
    relocate("com.zaxxer.hikari",        "com.ultimatedungeon.libs.hikari")
    relocate("com.mysql",                "com.ultimatedungeon.libs.mysql")
    relocate("com.github.benmanes",      "com.ultimatedungeon.libs.caffeine")
    relocate("com.google.errorprone",    "com.ultimatedungeon.libs.errorprone")

    // Minimise: exclude unused classes from shaded libs.
    minimize {
        // Keep the full SQLite native library — minimise removes it otherwise.
        exclude(dependency("org.xerial:sqlite-jdbc:.*"))
    }
}

// ── run-paper (local dev server) ─────────────────────────────────────────────
tasks.runServer {
    minecraftVersion("1.21.4")
}

// ── Compile options ───────────────────────────────────────────────────────────
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing"))
}

// ── processResources: inject project version into plugin.yml ─────────────────
tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

// ── assemble wires shadow as the default artifact ────────────────────────────
tasks.assemble {
    dependsOn(tasks.shadowJar)
}
