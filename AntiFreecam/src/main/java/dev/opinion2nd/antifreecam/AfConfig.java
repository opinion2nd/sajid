package dev.opinion2nd.antifreecam;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Immutable snapshot of config.yml. Rebuilt on every /antifreecam reload so the
 * packet listeners can read fields without touching Bukkit on async threads.
 */
public final class AfConfig {

    public final Set<World.Environment> enabledEnvironments = new HashSet<>();
    public final Set<String> disabledWorlds = new HashSet<>();

    public final int hideBelowY;
    public final int revealBelowYWhenUnder;
    public final int scanRadiusChunks;
    public final Material maskBlock;
    public final boolean skipMaskIfAlreadyAir;

    public final int lazyDistance;
    public final int lazyDistanceElytra;
    public final int rescanBlocks;
    public final boolean remaskOnReturn;

    /** How many chunks around an underground player stay visible (the rest is masked). */
    public final int undergroundRevealRadius;

    public final boolean maskEntities;

    public final boolean modDetectionEnabled;
    public final boolean autoKick;
    public final String kickMessage;
    public final boolean notifyAdmins;
    public final Set<String> watchedBrands = new HashSet<>();

    public AfConfig(FileConfiguration c) {
        for (String env : c.getStringList("enabledEnvironments")) {
            try {
                enabledEnvironments.add(World.Environment.valueOf(env.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        disabledWorlds.addAll(c.getStringList("disabledWorlds"));

        this.hideBelowY = c.getInt("hideBelowY", 20);
        this.revealBelowYWhenUnder = c.getInt("revealBelowYWhenUnder", 30);
        this.scanRadiusChunks = Math.max(4, c.getInt("scanRadiusChunks", 4));

        Material mat = Material.matchMaterial(c.getString("maskBlock", "STONE"));
        this.maskBlock = (mat != null && mat.isBlock()) ? mat : Material.STONE;
        this.skipMaskIfAlreadyAir = c.getBoolean("skipMaskIfAlreadyAir", false);

        this.lazyDistance = Math.max(16, c.getInt("lazyUnmask.distance", 256));
        this.lazyDistanceElytra = Math.max(16, c.getInt("lazyUnmask.distanceElytra", 256));
        this.rescanBlocks = Math.max(1, c.getInt("lazyUnmask.rescanBlocks", 1));
        this.remaskOnReturn = c.getBoolean("remaskOnReturn", true);
        this.undergroundRevealRadius = Math.max(1, c.getInt("undergroundRevealRadius", 5));

        this.maskEntities = c.getBoolean("maskEntities", true);

        this.modDetectionEnabled = c.getBoolean("modDetection.enabled", true);
        this.autoKick = c.getBoolean("modDetection.autoKick", true);
        this.kickMessage = c.getString("modDetection.kickMessage",
                "§cYou are using a client that is not allowed on this server.");
        this.notifyAdmins = c.getBoolean("modDetection.notifyAdmins", true);
        if (c.isConfigurationSection("modDetection.detect")) {
            for (String key : c.getConfigurationSection("modDetection.detect").getKeys(false)) {
                if (c.getBoolean("modDetection.detect." + key)) {
                    watchedBrands.add(key.toLowerCase(Locale.ROOT));
                }
            }
        }
    }

    /** True if masking should run for a player currently in this world. */
    public boolean isWorldActive(World world) {
        if (world == null) return false;
        if (disabledWorlds.contains(world.getName())) return false;
        return enabledEnvironments.contains(world.getEnvironment());
    }
}
