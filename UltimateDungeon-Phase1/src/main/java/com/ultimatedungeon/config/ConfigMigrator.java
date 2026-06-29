package com.ultimatedungeon.config;

import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

/** Applies safe schema migrations when upgrading between plugin versions. */
public final class ConfigMigrator {

    private final PluginLogger logger;

    public ConfigMigrator(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    public void migrate(final int fromVersion, final int toVersion) {
        // Implemented in Phase 1.
        logger.debug("ConfigMigrator.migrate(" + fromVersion + " -> " + toVersion + ") — pending.");
    }
}
