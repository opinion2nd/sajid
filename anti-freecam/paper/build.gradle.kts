plugins {
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":common"))

    compileOnly("io.papermc.paper:paper-api:${rootProject.property("paperVersion")}")
    compileOnly("com.comphenix.protocol:ProtocolLib:${rootProject.property("protocolLibVersion")}")
    compileOnly("com.google.code.gson:gson:2.11.0")

    testImplementation(project(":common"))
    testImplementation("com.github.seeseemelk:MockBukkit-v1.21:3.93.0")
    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

repositories {
    maven("https://repo.jitpack.io")
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("dev.thewindows.antifreecam.common", "dev.thewindows.antifreecam.paper.shaded.common")
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(mapOf("version" to project.version))
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
