package com.ultimatedungeon.puzzle.puzzles;

import com.ultimatedungeon.puzzle.model.PuzzleDefinition;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Players must complete all activations before the timer expires. */
public final class TimedActivationPuzzle extends AbstractPuzzle {
    private long deadlineMs;
    public TimedActivationPuzzle() { super(PuzzleDefinition.timed("TimedActivationPuzzle", 5, 200L)); }
    public TimedActivationPuzzle(@NotNull final PuzzleDefinition def) { super(def); }
    @Override
    public void start() {
        super.start();
        deadlineMs = System.currentTimeMillis() + definition.timeLimitTicks() * 50L;
    }
    @Override
    public void onPlayerInteract(@NotNull final Player player) {
        if (definition.timeLimitTicks() > 0 && System.currentTimeMillis() > deadlineMs) {
            fail(player);
            deadlineMs = System.currentTimeMillis() + definition.timeLimitTicks() * 50L;
            return;
        }
        advance(player);
    }
}
