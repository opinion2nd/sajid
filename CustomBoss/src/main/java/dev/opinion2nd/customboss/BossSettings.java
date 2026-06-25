package dev.opinion2nd.customboss;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Immutable snapshot of config.yml. Re-created on every /boss reload so the
 * rest of the plugin never touches the raw configuration directly.
 */
public final class BossSettings {

    public final String displayName;
    public final EntityType entityType;
    public final double health;
    public final double damage;
    public final double movementSpeed;

    public final BarColor barColor;
    public final BarStyle barStyle;

    public final int abilityIntervalTicks;

    public final boolean fireballEnabled;

    public final boolean summonEnabled;
    public final EntityType minionType;
    public final int minionCount;

    public final boolean aoeEnabled;
    public final double aoeRadius;
    public final double aoeDamage;
    public final double aoeStrength;

    public final boolean teleportEnabled;

    public final boolean enrageEnabled;
    public final double enrageThreshold;
    public final double enrageSpeedMultiplier;

    public final String broadcast;
    public final List<String> rewardCommands;
    public final List<ItemStack> drops;

    public BossSettings(ConfigurationSection config, Logger logger) {
        ConfigurationSection boss = section(config, "boss");

        this.displayName = color(boss.getString("display-name", "&c&lCustom Boss"));
        this.entityType = livingType(boss.getString("entity-type", "WITHER_SKELETON"),
                EntityType.WITHER_SKELETON, logger);
        this.health = Math.max(1.0, boss.getDouble("health", 500.0));
        this.damage = Math.max(0.0, boss.getDouble("damage", 12.0));
        this.movementSpeed = Math.max(0.0, boss.getDouble("movement-speed", 0.28));

        ConfigurationSection bar = section(boss, "bossbar");
        this.barColor = enumOrDefault(BarColor.class, bar.getString("color", "RED"), BarColor.RED, logger);
        this.barStyle = enumOrDefault(BarStyle.class, bar.getString("style", "SEGMENTED_10"),
                BarStyle.SEGMENTED_10, logger);

        ConfigurationSection ab = section(boss, "abilities");
        this.abilityIntervalTicks = Math.max(20, ab.getInt("interval-seconds", 6) * 20);

        ConfigurationSection fb = section(ab, "fireball");
        this.fireballEnabled = fb.getBoolean("enabled", true);

        ConfigurationSection sm = section(ab, "summon-minions");
        this.summonEnabled = sm.getBoolean("enabled", true);
        this.minionType = livingType(sm.getString("type", "ZOMBIE"), EntityType.ZOMBIE, logger);
        this.minionCount = Math.max(1, sm.getInt("count", 3));

        ConfigurationSection aoe = section(ab, "aoe-knockback");
        this.aoeEnabled = aoe.getBoolean("enabled", true);
        this.aoeRadius = Math.max(1.0, aoe.getDouble("radius", 6.0));
        this.aoeDamage = Math.max(0.0, aoe.getDouble("damage", 8.0));
        this.aoeStrength = Math.max(0.0, aoe.getDouble("strength", 2.0));

        ConfigurationSection tp = section(ab, "teleport");
        this.teleportEnabled = tp.getBoolean("enabled", true);

        ConfigurationSection en = section(ab, "enrage");
        this.enrageEnabled = en.getBoolean("enabled", true);
        this.enrageThreshold = clamp01(en.getDouble("health-threshold", 0.3));
        this.enrageSpeedMultiplier = Math.max(1.0, en.getDouble("speed-multiplier", 1.5));

        ConfigurationSection rw = section(boss, "rewards");
        this.broadcast = color(rw.getString("broadcast", ""));
        this.rewardCommands = new ArrayList<>(rw.getStringList("commands"));
        this.drops = parseDrops(rw.getStringList("drops"), logger);
    }

    // ----- helpers --------------------------------------------------------

    private static ConfigurationSection section(ConfigurationSection parent, String path) {
        ConfigurationSection s = parent.getConfigurationSection(path);
        return s != null ? s : parent.createSection(path);
    }

    private static String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static EntityType livingType(String name, EntityType def, Logger logger) {
        EntityType type = enumOrDefault(EntityType.class, name, def, logger);
        Class<?> clazz = type.getEntityClass();
        if (clazz == null || !org.bukkit.entity.LivingEntity.class.isAssignableFrom(clazz)) {
            logger.warning("Entity type '" + name + "' is not a living entity, using " + def + ".");
            return def;
        }
        return type;
    }

    private static <T extends Enum<T>> T enumOrDefault(Class<T> type, String name, T def, Logger logger) {
        if (name == null) {
            return def;
        }
        try {
            return Enum.valueOf(type, name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            logger.warning("Unknown " + type.getSimpleName() + " '" + name + "', using " + def + ".");
            return def;
        }
    }

    private static List<ItemStack> parseDrops(List<String> raw, Logger logger) {
        List<ItemStack> result = new ArrayList<>();
        for (String entry : raw) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            String[] parts = entry.trim().split("\\s+");
            Material material = Material.matchMaterial(parts[0]);
            if (material == null || !material.isItem()) {
                logger.warning("Unknown drop material '" + parts[0] + "', skipping.");
                continue;
            }
            int amount = 1;
            if (parts.length > 1) {
                try {
                    amount = Math.max(1, Integer.parseInt(parts[1]));
                } catch (NumberFormatException ex) {
                    logger.warning("Invalid drop amount in '" + entry + "', defaulting to 1.");
                }
            }
            result.add(new ItemStack(material, amount));
        }
        return result;
    }
}
