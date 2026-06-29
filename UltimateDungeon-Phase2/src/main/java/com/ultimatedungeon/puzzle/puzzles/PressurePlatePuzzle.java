package com.ultimatedungeon.puzzle.puzzles;

import com.ultimatedungeon.api.puzzle.IPuzzle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** PressurePlatePuzzle — puzzle implementation. Milestone 4. */
public final class PressurePlatePuzzle implements IPuzzle {
    @Override @NotNull public String getPuzzleId() { return "PressurePlatePuzzle"; }
    @Override public void start() {}
    @Override public void reset() {}
    @Override public boolean isSolved() { return false; }
    @Override public void onPlayerInteract(@NotNull final Player player) {}
}
