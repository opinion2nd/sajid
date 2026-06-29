package com.ultimatedungeon.compat;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

/**
 * Detects the running server platform and version at startup.
 *
 * <p>Used to select the appropriate compat adapter for platform-specific APIs
 * (Folia region scheduling, Purpur extensions, Spigot degradation).</p>
 */
public final class VersionDetector {

    private final boolean folia;
    private final boolean purpur;
    private final boolean spigotOnly;

    public VersionDetector() {
        this.folia = classExists("io.papermc.paper.threadedregions.RegionizedServer");
        this.purpur = classExists("org.purpurmc.purpur.PurpurConfig");
        this.spigotOnly = !classExists("io.papermc.paper.configuration.Configuration");
    }

    public boolean isFolia() { return folia; }
    public boolean isPurpur() { return purpur; }
    public boolean isSpigotOnly() { return spigotOnly; }
    public boolean isPaper() { return !spigotOnly; }

    @NotNull
    public String getVersionString() {
        return Bukkit.getServer().getVersion();
    }

    private boolean classExists(@NotNull final String className) {
        try {
            Class.forName(className);
            return true;
        } catch (final ClassNotFoundException ignored) {
            return false;
        }
    }
}
