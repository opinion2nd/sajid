package com.ultimatedungeon.api.puzzle;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Contract for a dungeon puzzle. */
public interface IPuzzle {
    @NotNull String getPuzzleId();
    void start();
    void reset();
    boolean isSolved();
    void onPlayerInteract(@NotNull Player player);
}
