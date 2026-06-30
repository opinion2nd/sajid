package com.ultimatedungeon.compat;

import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

/**
 * Purpur compatibility helper. Purpur is a Paper fork, so all Paper APIs the
 * plugin uses work unchanged; this adapter simply records that Purpur-specific
 * niceties are available and degrades gracefully on plain Paper.
 */
public final class PurpurCompatAdapter {

    private final boolean active;

    public PurpurCompatAdapter(@NotNull final VersionDetector detector, @NotNull final PluginLogger logger) {
        this.active = detector.isPurpur();
        if (active) {
            logger.debug("Purpur detected — Purpur extensions available.");
        }
    }

    public boolean isActive() {
        return active;
    }
}
