package dev.opinion2nd.antifreecam;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Immutable snapshot of config.yml. Rebuilt on every /antifreecam reload so the
 * packet listeners can read fields without touching Bukkit on async threads.
 */
public final class AfConfig {

    public final boolean enabled;

    public final Set<World.Environment> enabledEnvironments = new HashSet<>();
    public final Set<String> disabledWorlds = new HashSet<>();

    /** Blocks strictly below this Y are occlusion-masked. */
    public final int hideBelowY;

    /** Block sent in place of a fully-buried block. AIR = true void (default). */
    public final Material maskBlock;

    public final boolean maskEntities;

    public final boolean modDetectionEnabled;
    public final boolean autoKick;
    public final String kickMessage;
    public final boolean notifyAdmins;
    public final Set<String> watchedBrands = new HashSet<>();

    public AfConfig(FileConfiguration c) {
        this.enabled = c.getBoolean("enabled", true);

        for (String env : c.getStringList("enabledEnvironments")) {
            try {
                enabledEnvironments.add(World.Environment.valueOf(env.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        disabledWorlds.addAll(c.getStringList("disabledWorlds"));

        this.hideBelowY = c.getInt("hideBelowY", 20);

        Material mat = Material.matchMaterial(c.getString("maskBlock", "AIR"));
        this.maskBlock = (mat != null && mat.isBlock()) ? mat : Material.AIR;

        this.maskEntities = c.getBoolean("maskEntities", false);

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
