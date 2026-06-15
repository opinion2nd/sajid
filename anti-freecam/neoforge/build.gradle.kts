plugins {
    id("net.neoforged.gradle.userdev") version "7.0.+"
}

dependencies {
    implementation(project(":common"))
    implementation("net.neoforged:neoforge:${rootProject.property("neoForgeVersion")}")
    jarJar(project(":common"))
}

minecraft {
    accessTransformers {
        file("src/main/resources/META-INF/accesstransformer.cfg")
    }
}

runs {
    create("server") {
        server()
        workingDirectory(project.file("run"))
        modSource(project.sourceSets["main"])
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(mapOf("version" to project.version))
    }
}
