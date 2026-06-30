package com.ultimatedungeon.boss.model;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable data for a single boss phase, parsed from {@code bosses.yml}.
 *
 * <p>A phase becomes active once the boss's health ratio drops to or below
 * {@link #threshold()}. Phases are ordered from highest threshold (1.0, full
 * health) down to the lowest.</p>
 */
public record BossPhaseData(
        @NotNull String phaseId,
        double threshold
) {

    /** Reads a single phase entry from a {@code phases:} list element. */
    @NotNull
    public static BossPhaseData fromSection(@NotNull final ConfigurationSection section) {
        return new BossPhaseData(
                section.getString("phase-id", "phase"),
                section.getDouble("threshold", 1.0)
        );
    }
}
