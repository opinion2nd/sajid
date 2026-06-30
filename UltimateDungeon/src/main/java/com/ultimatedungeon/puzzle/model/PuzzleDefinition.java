package com.ultimatedungeon.puzzle.model;

import org.jetbrains.annotations.NotNull;

/**
 * Lightweight configuration for a puzzle instance: how many correct steps solve
 * it and, for timed puzzles, the window in ticks.
 */
public record PuzzleDefinition(
        @NotNull String puzzleId,
        int requiredSteps,
        long timeLimitTicks
) {
    public static PuzzleDefinition of(@NotNull final String id, final int steps) {
        return new PuzzleDefinition(id, Math.max(1, steps), 0L);
    }

    public static PuzzleDefinition timed(@NotNull final String id, final int steps, final long ticks) {
        return new PuzzleDefinition(id, Math.max(1, steps), ticks);
    }
}
