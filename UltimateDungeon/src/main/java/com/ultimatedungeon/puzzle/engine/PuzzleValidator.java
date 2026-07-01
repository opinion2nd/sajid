package com.ultimatedungeon.puzzle.engine;

import com.ultimatedungeon.api.puzzle.IPuzzle;
import org.jetbrains.annotations.NotNull;

/** Validates whether a puzzle has been fully and correctly solved. */
public final class PuzzleValidator {

    public boolean isComplete(@NotNull final IPuzzle puzzle) {
        return puzzle.isSolved();
    }
}
