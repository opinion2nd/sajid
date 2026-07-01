package com.ultimatedungeon.config.files;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Typed wrapper for the corresponding YAML config file.
 * Full implementation arrives in the milestone that owns this system.
 */
public final class ThemesConfig {

    private final FileConfiguration config;

    public ThemesConfig(@NotNull final FileConfiguration config) {
        this.config = config;
    }

    /** Returns the raw underlying {@link FileConfiguration} for milestone-specific access. */
    @NotNull
    public FileConfiguration raw() {
        return config;
    }
}
