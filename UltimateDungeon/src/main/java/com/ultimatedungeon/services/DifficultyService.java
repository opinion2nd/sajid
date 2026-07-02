package com.ultimatedungeon.services;

import com.ultimatedungeon.config.files.DifficultyConfig;
import com.ultimatedungeon.config.files.DifficultyConfig.DifficultyPreset;
import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

/**
 * Resolves difficulty scaling multipliers for a given preset id.
 *
 * <p>Stateless wrapper over {@link DifficultyConfig} that every combat system
 * consults so monster/boss/trap scaling and loot tier bonuses all derive from
 * one configurable source.</p>
 */
public final class DifficultyService {

    private final DifficultyConfig config;
    private final PluginLogger logger;

    public DifficultyService(@NotNull final DifficultyConfig config, @NotNull final PluginLogger logger) {
        this.config = config;
        this.logger = logger;
    }

    @NotNull
    public DifficultyPreset resolve(@NotNull final String difficultyId) {
        return config.getPresetOrDefault(difficultyId);
    }

    public boolean isValid(@NotNull final String difficultyId) {
        return config.hasPreset(difficultyId);
    }

    public double healthMultiplier(@NotNull final String id)  { return resolve(id).healthMultiplier(); }
    public double damageMultiplier(@NotNull final String id)  { return resolve(id).damageMultiplier(); }
    public double cooldownMultiplier(@NotNull final String id){ return resolve(id).cooldownMultiplier(); }
    public double spawnRateMultiplier(@NotNull final String id){ return resolve(id).spawnRateMultiplier(); }
    public double trapDamageMultiplier(@NotNull final String id){ return resolve(id).trapDamageMultiplier(); }
    public int    lootTierBonus(@NotNull final String id)     { return resolve(id).lootTierBonus(); }
    public int    bossCount(@NotNull final String id)         { return Math.max(1, resolve(id).bossCount()); }
}
