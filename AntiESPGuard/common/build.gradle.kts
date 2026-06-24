// Common module: pure-Java shared logic (config model, mask rules, mod
// signatures, seed scrambler). No Minecraft / platform dependencies so it can
// be consumed by every loader module.

plugins {
    java
    `java-library`
}

dependencies {
    // YAML parser so every platform reads the same config.yml format.
    api("org.yaml:snakeyaml:2.2")
    compileOnly("org.jetbrains:annotations:24.1.0")
}
