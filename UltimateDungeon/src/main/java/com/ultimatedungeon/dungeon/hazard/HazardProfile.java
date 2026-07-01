package com.ultimatedungeon.dungeon.hazard;

import org.bukkit.Particle;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A single ambient-hazard profile: how much damage to apply, an optional lingering
 * potion effect, and a telegraph particle. Built from {@code dungeon.yml}.
 *
 * @param id             profile id as written in config
 * @param damage         damage dealt each hazard tick (may be {@code 0})
 * @param effect         optional lingering potion effect ({@code null} to skip)
 * @param effectTicks    duration of the potion effect, in ticks
 * @param amplifier      potion effect amplifier (0 = level I)
 * @param particle       optional telegraph particle ({@code null} to skip)
 */
public record HazardProfile(
        @NotNull String id,
        double damage,
        @Nullable PotionEffectType effect,
        int effectTicks,
        int amplifier,
        @Nullable Particle particle
) {
}
