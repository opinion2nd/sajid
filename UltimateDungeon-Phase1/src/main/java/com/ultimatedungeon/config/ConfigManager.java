package com.ultimatedungeon.config;

import com.ultimatedungeon.UltimateDungeon;
import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

/**
 * Manages loading, validation, and migration of all plugin configuration files.
 *
 * <p>On startup: extracts default files, validates all values, and runs
 * migration if the stored config version differs from the current plugin version.</p>
 *
 * <p>On reload: re-reads all files from disk and reapplies validation.</p>
 */
public final class ConfigManager {

    private final UltimateDungeon plugin;
    private final PluginLogger logger;

    public ConfigManager(
            @NotNull final UltimateDungeon plugin,
            @NotNull final PluginLogger logger
    ) {
        this.plugin = plugin;
        this.logger = logger;
    }

    public void load() {
        // Implemented in Phase 1: extract defaults, validate, migrate.
        logger.debug("ConfigManager.load() — pending implementation.");
    }

    public void reload() {
        // Re-read all config files from disk.
        logger.debug("ConfigManager.reload() — pending implementation.");
    }
}
