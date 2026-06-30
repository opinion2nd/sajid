package com.ultimatedungeon.compat;

import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

/**
 * Spigot compatibility helper. On plain Spigot the Adventure/Paper conveniences
 * the plugin relies on are bundled by modern Spigot builds, but this adapter
 * flags the environment so any Paper-only enhancements can be skipped safely.
 */
public final class SpigotCompatAdapter {

    private final boolean active;

    public SpigotCompatAdapter(@NotNull final VersionDetector detector, @NotNull final PluginLogger logger) {
        this.active = detector.isSpigotOnly();
        if (active) {
            logger.warning("Plain Spigot detected — Paper is recommended for best performance.");
        }
    }

    public boolean isActive() {
        return active;
    }
}
