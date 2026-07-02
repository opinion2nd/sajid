package com.ultimatedungeon.config.files;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed wrapper for {@code waves.yml} — the per-level wave rules that replaced
 * the old monsters.yml. Invalid or missing values fall back to safe defaults.
 */
public final class WavesConfig {

    /** Wave rules for one dungeon level. */
    public record LevelWaves(
        int roomsWithWavesPercent,
        int minWavesPerRoom,
        int maxWavesPerRoom,
        int mobsPerWaveMin,
        int mobsPerWaveMax,
        @NotNull Map<EntityType, Integer> mobWeights,
        boolean bossRoomWaves
    ) {}

    private static final LevelWaves DEFAULT = new LevelWaves(
            35, 1, 1, 3, 5, Map.of(EntityType.ZOMBIE, 60, EntityType.SKELETON, 40), false);

    private final Map<String, LevelWaves> levels = new LinkedHashMap<>();
    private final int countdownSeconds;

    public WavesConfig(@NotNull final FileConfiguration cfg) {
        this.countdownSeconds = Math.max(1, cfg.getInt("countdown-seconds", 10));
        final ConfigurationSection root = cfg.getConfigurationSection("waves");
        if (root == null) return;
        for (final String key : root.getKeys(false)) {
            final ConfigurationSection s = root.getConfigurationSection(key);
            if (s == null) continue;
            // "level-1" in waves.yml maps to preset id "level_1".
            levels.put(key.replace('-', '_'), parse(s));
        }
    }

    @NotNull
    private LevelWaves parse(@NotNull final ConfigurationSection s) {
        final int[] perWave = parseRange(s.getString("mobs-per-wave", "3-5"), 3, 5);
        final Map<EntityType, Integer> weights = new LinkedHashMap<>();
        final ConfigurationSection mobs = s.getConfigurationSection("mobs");
        if (mobs != null) {
            for (final String name : mobs.getKeys(false)) {
                try {
                    final EntityType type = EntityType.valueOf(name.toUpperCase());
                    final int weight = Math.max(0, mobs.getInt(name, 0));
                    if (weight > 0) weights.put(type, weight);
                } catch (final IllegalArgumentException ignored) {
                    // Unknown entity name — skipped; validator logs separately.
                }
            }
        }
        if (weights.isEmpty()) weights.putAll(DEFAULT.mobWeights());
        return new LevelWaves(
                clamp(s.getInt("rooms-with-waves-percent", 35), 0, 100),
                clamp(s.getInt("min-waves-per-room", 1), 1, 20),
                clamp(s.getInt("max-waves-per-room", 1), 1, 20),
                perWave[0], perWave[1],
                weights,
                s.getBoolean("boss-room-waves", false));
    }

    /** Parses "a-b" (or a single number) into a sane [min, max] pair. */
    private int[] parseRange(final String raw, final int defMin, final int defMax) {
        try {
            final String[] parts = raw.trim().split("-");
            final int a = Integer.parseInt(parts[0].trim());
            final int b = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : a;
            return new int[]{clamp(Math.min(a, b), 1, 64), clamp(Math.max(a, b), 1, 64)};
        } catch (final Exception ex) {
            return new int[]{defMin, defMax};
        }
    }

    private int clamp(final int v, final int lo, final int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    /** Wave rules for a level id (e.g. "level_3"); safe defaults if missing. */
    @NotNull
    public LevelWaves forLevel(@NotNull final String levelId) {
        return levels.getOrDefault(levelId, DEFAULT);
    }

    @NotNull
    public Map<String, LevelWaves> getAllLevels() { return Collections.unmodifiableMap(levels); }

    /** Boss-style pre-fight countdown before a wave room seals and starts. */
    public int getCountdownSeconds() { return countdownSeconds; }
}
