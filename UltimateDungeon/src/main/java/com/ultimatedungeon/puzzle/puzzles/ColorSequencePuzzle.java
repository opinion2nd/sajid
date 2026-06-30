package com.ultimatedungeon.puzzle.puzzles;

import com.ultimatedungeon.puzzle.model.PuzzleDefinition;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Players must activate coloured nodes in the shown order. */
public final class ColorSequencePuzzle extends AbstractPuzzle {
    public ColorSequencePuzzle() { super(PuzzleDefinition.of("ColorSequencePuzzle", 4)); }
    public ColorSequencePuzzle(@NotNull final PuzzleDefinition def) { super(def); }
    @Override public void onPlayerInteract(@NotNull final Player player) { advance(player); }
}
