package com.ultimatedungeon.monster.engine;

import com.ultimatedungeon.config.files.DifficultyConfig.DifficultyPreset;
import com.ultimatedungeon.monster.model.MonsterDefinition;
import org.jetbrains.annotations.NotNull;

/**
 * Applies difficulty multipliers to a monster's base stats.
 */
public final class MonsterScaler {

    /** Effective stats after difficulty scaling. */
    public record Scaled(double health, double damage, double speed) {}

    @NotNull
    public Scaled scale(@NotNull final MonsterDefinition def, @NotNull final DifficultyPreset preset) {
        return new Scaled(
                Math.max(1.0, def.getHealth() * preset.healthMultiplier()),
                Math.max(0.0, def.getDamage() * preset.damageMultiplier()),
                def.getSpeed());
    }
}
