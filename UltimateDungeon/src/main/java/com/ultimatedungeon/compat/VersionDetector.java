package com.ultimatedungeon.compat;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

/**
 * Detects the running server platform and Minecraft version at startup.
 *
 * <p>Results are cached as {@code final} booleans on construction so every
 * subsequent call is a trivial field read with no class-loading overhead.</p>
 *
 * <p>Used by {@link com.ultimatedungeon.core.PluginBootstrap} to log the
 * detected environment and by compat adapters to gate platform-specific
 * API calls.</p>
 */
public final class VersionDetector {

    private final boolean folia;
    private final boolean purpur;
    private final boolean paper;
    private final boolean spigotOnly;
    private final String  versionString;
    private final int     majorVersion;
    private final int     minorVersion;

    public VersionDetector() {
        this.folia         = classExists("io.papermc.paper.threadedregions.RegionizedServer");
        this.purpur        = classExists("org.purpurmc.purpur.PurpurConfig");
        this.paper         = classExists("com.destroystokyo.paper.PaperConfig")
                          || classExists("io.papermc.paper.configuration.Configuration");
        this.spigotOnly    = !paper && !folia;
        this.versionString = Bukkit.getServer().getVersion();

        // Parse "1.21.4" style from the Bukkit version string.
        int major = 1, minor = 21;
        try {
            final String bukkit = Bukkit.getBukkitVersion(); // e.g. "1.21.4-R0.1-SNAPSHOT"
            final String[] parts = bukkit.split("\\.");
            if (parts.length >= 2) {
                major = Integer.parseInt(parts[0]);
                minor = Integer.parseInt(parts[1].split("-")[0]);
            }
        } catch (final NumberFormatException ignored) { /* keep defaults */ }
        this.majorVersion = major;
        this.minorVersion = minor;
    }

    // ── Platform detection ────────────────────────────────────────────────────

    /** Returns {@code true} if the server runs Folia (regionised multithreading). */
    public boolean isFolia()     { return folia; }

    /** Returns {@code true} if the server is Purpur or a Purpur fork. */
    public boolean isPurpur()    { return purpur; }

    /** Returns {@code true} if the server is Paper (or Paper fork). */
    public boolean isPaper()     { return paper || folia; }

    /** Returns {@code true} if this is a plain Spigot server with no Paper API. */
    public boolean isSpigotOnly(){ return spigotOnly; }

    // ── Version numbers ───────────────────────────────────────────────────────

    /**
     * Returns the Minecraft major version component (always {@code 1}).
     *
     * @return major version
     */
    public int getMajorVersion() { return majorVersion; }

    /**
     * Returns the Minecraft minor version component (e.g. {@code 21} for 1.21.x).
     *
     * @return minor version
     */
    public int getMinorVersion() { return minorVersion; }

    /**
     * Returns {@code true} if the server is running at least {@code 1.minor}.
     *
     * @param minor the required minor version
     * @return {@code true} if the server meets or exceeds this version
     */
    public boolean isAtLeast(final int minor) {
        return minorVersion >= minor;
    }

    /**
     * Returns the full server version string as reported by Bukkit.
     *
     * @return server version string
     */
    @NotNull
    public String getVersionString() { return versionString; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static boolean classExists(@NotNull final String className) {
        try {
            Class.forName(className);
            return true;
        } catch (final ClassNotFoundException ignored) {
            return false;
        }
    }
}
