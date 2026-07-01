package com.ultimatedungeon.api.puzzle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;

/** Contract for puzzle type registration and lookup. */
public interface IPuzzleRegistry {
    void register(@NotNull IPuzzle puzzle);
    @Nullable IPuzzle getPuzzle(@NotNull String puzzleId);
    @NotNull Collection<IPuzzle> getAllPuzzles();
}
