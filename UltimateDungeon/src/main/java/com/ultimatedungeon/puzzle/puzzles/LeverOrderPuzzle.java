package com.ultimatedungeon.puzzle.puzzles;

import com.ultimatedungeon.puzzle.model.PuzzleDefinition;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Players must pull levers in the correct sequence. */
public final class LeverOrderPuzzle extends AbstractPuzzle {
    public LeverOrderPuzzle() { super(PuzzleDefinition.of("LeverOrderPuzzle", 3)); }
    public LeverOrderPuzzle(@NotNull final PuzzleDefinition def) { super(def); }
    @Override public void onPlayerInteract(@NotNull final Player player) { advance(player); }
}
