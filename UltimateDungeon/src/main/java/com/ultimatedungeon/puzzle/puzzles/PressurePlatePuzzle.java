package com.ultimatedungeon.puzzle.puzzles;

import com.ultimatedungeon.puzzle.model.PuzzleDefinition;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Players must step on the correct pressure plates to solve. */
public final class PressurePlatePuzzle extends AbstractPuzzle {
    public PressurePlatePuzzle() { super(PuzzleDefinition.of("PressurePlatePuzzle", 4)); }
    public PressurePlatePuzzle(@NotNull final PuzzleDefinition def) { super(def); }
    @Override public void onPlayerInteract(@NotNull final Player player) { advance(player); }
}
