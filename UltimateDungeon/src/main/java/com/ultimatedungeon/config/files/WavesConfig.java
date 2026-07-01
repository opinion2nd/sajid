package com.ultimatedungeon.config.files;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Typed view of {@code waves.yml}: per-level mob rosters and wave scaling. */
public final class WavesConfig {

    /** One dungeon level's wave roster and scaling. */
    public record LevelWaves(@NotNull List<EntityType> mobs, double healthMultiplier, double damage) {}

    private final int wavesPerRoom;
    private final int basePerWave;
    private final int perWaveGrowth;
    private final Map<Integer, LevelWaves> levels = new HashMap<>();

    public WavesConfig(@NotNull final FileConfiguration cfg) {
        this.wavesPerRoom  = Math.max(1, cfg.getInt("waves-per-room", 5));
        this.basePerWave   = Math.max(1, cfg.getInt("base-per-wave", 4));
        this.perWaveGrowth = Math.max(0, cfg.getInt("per-wave-growth", 1));

        final ConfigurationSection levelsSec = cfg.getConfigurationSection("levels");
        if (levelsSec != null) {
            for (final String key : levelsSec.getKeys(false)) {
                final ConfigurationSection s = levelsSec.getConfigurationSection(key);
                if (s == null) continue;
                final int level;
                try {
                    level = Integer.parseInt(key);
                } catch (final NumberFormatException ex) {
                    continue;
                }
                final List<EntityType> mobs = new ArrayList<>();
                for (final String name : s.getStringList("mobs")) {
                    final EntityType type = resolve(name);
                    if (type != null) mobs.add(type);
                }
                if (mobs.isEmpty()) mobs.add(EntityType.ZOMBIE);
                levels.put(level, new LevelWaves(mobs,
                        s.getDouble("health-multiplier", 1.0),
                        s.getDouble("damage", 3.0)));
            }
        }
    }

    private static EntityType resolve(@NotNull final String name) {
        try {
            return EntityType.valueOf(name.trim().toUpperCase());
        } catch (final IllegalArgumentException ex) {
            return null;
        }
    }

    public int getWavesPerRoom()  { return wavesPerRoom; }
    public int getBasePerWave()   { return basePerWave; }
    public int getPerWaveGrowth() { return perWaveGrowth; }

    /** Roster/scaling for a level, falling back to the nearest defined level. */
    @NotNull
    public LevelWaves forLevel(final int level) {
        LevelWaves lw = levels.get(level);
        if (lw != null) return lw;
        for (int l = level; l >= 1; l--) {
            lw = levels.get(l);
            if (lw != null) return lw;
        }
        if (!levels.isEmpty()) return levels.values().iterator().next();
        return new LevelWaves(List.of(EntityType.ZOMBIE), 1.0, 3.0);
    }
}
