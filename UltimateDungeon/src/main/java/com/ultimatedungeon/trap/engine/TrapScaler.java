package com.ultimatedungeon.trap.engine;

import com.ultimatedungeon.config.files.DifficultyConfig.DifficultyPreset;
import com.ultimatedungeon.trap.model.TrapDefinition;
import org.jetbrains.annotations.NotNull;

/** Scales trap damage by the active difficulty's trap-damage multiplier. */
public final class TrapScaler {

    public double scaleDamage(@NotNull final TrapDefinition def, @NotNull final DifficultyPreset preset) {
        return Math.max(0.0, def.getBaseDamage() * preset.trapDamageMultiplier());
    }
}
