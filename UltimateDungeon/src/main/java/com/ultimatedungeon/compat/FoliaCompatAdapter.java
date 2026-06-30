package com.ultimatedungeon.compat;

import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

/**
 * Folia compatibility helper.
 *
 * <p>Folia regionises the server across threads and rejects the classic Bukkit
 * scheduler for region-bound work. This adapter detects Folia and surfaces that
 * to the rest of the plugin so callers can prefer region-aware scheduling where
 * available; on non-Folia platforms it is inert.</p>
 */
public final class FoliaCompatAdapter {

    private final boolean active;

    public FoliaCompatAdapter(@NotNull final VersionDetector detector, @NotNull final PluginLogger logger) {
        this.active = detector.isFolia();
        if (active) {
            logger.info("Folia detected — region-aware scheduling enabled where supported.");
        }
    }

    public boolean isActive() {
        return active;
    }
}
