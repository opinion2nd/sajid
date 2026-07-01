package com.ultimatedungeon.config.files;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Typed wrapper for {@code difficulty.yml}. */
public final class DifficultyConfig {

    /** Immutable snapshot of a single difficulty preset. */
    public record DifficultyPreset(
        @NotNull String id,
        @NotNull String displayName,
        int    level,
        double healthMultiplier,
        double damageMultiplier,
        double cooldownMultiplier,
        double spawnRateMultiplier,
        int    lootTierBonus,
        double trapDamageMultiplier
    ) {}

    private final Map<String, DifficultyPreset> presets;

    public DifficultyConfig(@NotNull final FileConfiguration cfg) {
        final Map<String, DifficultyPreset> map = new LinkedHashMap<>();
        final ConfigurationSection section = cfg.getConfigurationSection("difficulties");
        if (section != null) {
            for (final String id : section.getKeys(false)) {
                final ConfigurationSection ps = section.getConfigurationSection(id);
                if (ps == null) continue;
                map.put(id, new DifficultyPreset(
                    id,
                    ps.getString("display-name", id),
                    ps.getInt("level", 1),
                    ps.getDouble("health-multiplier", 1.0),
                    ps.getDouble("damage-multiplier", 1.0),
                    ps.getDouble("cooldown-multiplier", 1.0),
                    ps.getDouble("spawn-rate-multiplier", 1.0),
                    ps.getInt("loot-tier-bonus", 0),
                    ps.getDouble("trap-damage-multiplier", 1.0)
                ));
            }
        }
        this.presets = Collections.unmodifiableMap(map);
    }

    @Nullable
    public DifficultyPreset getPreset(@NotNull final String id) {
        return presets.get(id);
    }

    @NotNull
    public DifficultyPreset getPresetOrDefault(@NotNull final String id) {
        return presets.getOrDefault(id, new DifficultyPreset(
            "normal", "Normal", 1, 1.0, 1.0, 1.0, 1.0, 0, 1.0
        ));
    }

    @NotNull public Set<String> getPresetIds() { return presets.keySet(); }
    @NotNull public Map<String, DifficultyPreset> getAllPresets() { return presets; }
    public boolean hasPreset(@NotNull final String id) { return presets.containsKey(id); }
}
