package com.ultimatedungeon.dungeon.event;

import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * One configured dynamic event that can fire in an EVENT room, parsed from the
 * {@code dynamic-events.events} section of {@code dungeon.yml}.
 *
 * @param id           config id
 * @param kind         what the event does
 * @param weight       relative selection weight (higher = more likely)
 * @param extraWaves   AMBUSH: number of extra monster waves
 * @param perWave      AMBUSH: monsters per wave
 * @param effects      BLESSING/CURSE: potion effects to apply
 * @param effectTicks  BLESSING/CURSE: effect duration in ticks
 * @param amplifier    BLESSING/CURSE: effect amplifier
 * @param lootTable    TREASURE: loot table id granted to each player
 */
public record DynamicEventSpec(
        @NotNull String id,
        @NotNull DynamicEventKind kind,
        int weight,
        int extraWaves,
        int perWave,
        @NotNull List<PotionEffectType> effects,
        int effectTicks,
        int amplifier,
        @Nullable String lootTable
) {
    /** The distinct behaviours a dynamic event can take. */
    public enum DynamicEventKind { AMBUSH, BLESSING, CURSE, TREASURE }
}
