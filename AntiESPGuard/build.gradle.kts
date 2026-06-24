// Root build script. Per-module configuration lives in each module's
// build.gradle.kts; here we only share things every JVM module needs.

plugins {
    java
}

allprojects {
    group = rootProject.property("group") as String
    version = rootProject.property("modVersion") as String
}

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.codemc.io/repository/maven-releases/")  // PacketEvents
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}
