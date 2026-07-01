package com.ultimatedungeon.puzzle.puzzles;

import com.ultimatedungeon.api.puzzle.IPuzzle;
import com.ultimatedungeon.puzzle.model.PuzzleDefinition;
import com.ultimatedungeon.puzzle.model.PuzzleState;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Shared base for puzzles: tracks state, progress and a solved callback so
 * concrete puzzles only describe how an interaction advances them.
 */
public abstract class AbstractPuzzle implements IPuzzle {

    protected final PuzzleDefinition definition;
    protected PuzzleState state = PuzzleState.INACTIVE;
    protected int progress;
    private Runnable onSolved = () -> {};

    protected AbstractPuzzle(@NotNull final PuzzleDefinition definition) {
        this.definition = definition;
    }

    public void setOnSolved(@NotNull final Runnable onSolved) {
        this.onSolved = onSolved;
    }

    @Override @NotNull public String getPuzzleId() { return definition.puzzleId(); }

    @Override
    public void start() {
        state = PuzzleState.ACTIVE;
        progress = 0;
    }

    @Override
    public void reset() {
        state = PuzzleState.INACTIVE;
        progress = 0;
    }

    @Override
    public boolean isSolved() {
        return state == PuzzleState.SOLVED;
    }

    @NotNull public PuzzleState getState() { return state; }

    /** Marks a correct step; solves the puzzle once the required steps are met. */
    protected void advance(@NotNull final Player player) {
        if (state != PuzzleState.ACTIVE) return;
        progress++;
        MiniMessageUtil.sendActionBar(player,
                "<yellow>Puzzle progress: " + progress + "/" + definition.requiredSteps());
        if (progress >= definition.requiredSteps()) {
            solve();
        }
    }

    /** Marks a wrong step; resets progress. */
    protected void fail(@NotNull final Player player) {
        progress = 0;
        MiniMessageUtil.sendActionBar(player, "<red>Wrong! The puzzle resets.");
    }

    protected void solve() {
        if (state == PuzzleState.SOLVED) return;
        state = PuzzleState.SOLVED;
        onSolved.run();
    }
}
