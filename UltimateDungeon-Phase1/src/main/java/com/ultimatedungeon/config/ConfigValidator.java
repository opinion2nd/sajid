package com.ultimatedungeon.config;

import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

/** Validates all configuration values on load. Reports problems clearly. */
public final class ConfigValidator {

    private final PluginLogger logger;

    public ConfigValidator(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    public boolean validate() {
        // Implemented in Phase 1.
        return true;
    }
}
