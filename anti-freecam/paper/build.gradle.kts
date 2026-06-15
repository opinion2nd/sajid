plugins {
    id("com.gradleup.shadow")
}

dependencies {
    implementation(project(":common"))

    compileOnly("io.papermc.paper:paper-api:${rootProject.property("paperVersion")}")
    compileOnly("com.comphenix.protocol:ProtocolLib:${rootProject.property("protocolLibVersion")}")
    compileOnly("com.google.code.gson:gson:2.11.0")
}

tasks.shadowJar {
    archiveBaseName.set("AntiFreeam-Paper")
    archiveClassifier.set("")
    relocate("dev.thewindows.antifreecam.common", "dev.thewindows.antifreecam.paper.shaded.common")
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(mapOf("version" to project.version))
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
