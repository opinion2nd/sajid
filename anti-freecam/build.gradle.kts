plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    java
}

allprojects {
    group = "dev.thewindows.antifreecam"
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://repo.dmulloy2.net/repository/public/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://libraries.minecraft.net/")
        maven("https://repo.jitpack.io")
    }
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    tasks.withType<Jar> {
        manifest {
            attributes["Implementation-Version"] = project.version
        }
    }
}
