package com.ultimatedungeon.puzzle.engine;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.puzzle.puzzles.AbstractPuzzle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the active puzzle for each dungeon instance and routes player
 * interactions to it, firing the room-clear callback once it is solved.
 */
public final class PuzzleEngine {

    private final PluginLogger logger;
    private final PuzzleValidator validator = new PuzzleValidator();
    private final Map<UUID, AbstractPuzzle> active = new ConcurrentHashMap<>();

    public PuzzleEngine(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    /** Starts a puzzle for an instance with a callback to run when solved. */
    public void startPuzzle(@NotNull final UUID instanceId, @NotNull final AbstractPuzzle puzzle,
                            @NotNull final Runnable onSolved) {
        puzzle.setOnSolved(() -> {
            onSolved.run();
            active.remove(instanceId);
        });
        puzzle.start();
        active.put(instanceId, puzzle);
        logger.debug("Puzzle started for instance " + instanceId + ": " + puzzle.getPuzzleId());
    }

    /** Routes an interaction to the instance's active puzzle. */
    public void handleInteract(@NotNull final UUID instanceId, @NotNull final Player player) {
        final AbstractPuzzle puzzle = active.get(instanceId);
        if (puzzle != null && !validator.isComplete(puzzle)) {
            puzzle.onPlayerInteract(player);
        }
    }

    public boolean hasActivePuzzle(@NotNull final UUID instanceId) {
        return active.containsKey(instanceId);
    }

    public void cleanup(@NotNull final UUID instanceId) {
        active.remove(instanceId);
    }
}
