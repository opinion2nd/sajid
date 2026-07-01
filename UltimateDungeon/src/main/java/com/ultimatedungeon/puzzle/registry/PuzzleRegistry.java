package com.ultimatedungeon.puzzle.registry;

import com.ultimatedungeon.api.puzzle.IPuzzle;
import com.ultimatedungeon.api.puzzle.IPuzzleRegistry;
import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

/** Holds all registered puzzle type definitions. */
public final class PuzzleRegistry implements IPuzzleRegistry {

    private final PluginLogger logger;
    private final Map<String, IPuzzle> puzzles = new LinkedHashMap<>();

    public PuzzleRegistry(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    @Override public void register(@NotNull final IPuzzle puzzle) {
        puzzles.put(puzzle.getPuzzleId(), puzzle);
        logger.debug("Registered puzzle: " + puzzle.getPuzzleId());
    }

    @Override @Nullable public IPuzzle getPuzzle(@NotNull final String id) { return puzzles.get(id); }
    @Override @NotNull public Collection<IPuzzle> getAllPuzzles() { return Collections.unmodifiableCollection(puzzles.values()); }
}
