package com.ultimatedungeon.dungeon.instance;

import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

/** Tears down a completed or failed dungeon instance fully and safely. */
public final class DungeonCleanupService {

    private final PluginLogger logger;

    public DungeonCleanupService(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    public void cleanup(@NotNull final DungeonInstance instance) {
        // Phase 3: cancel tasks, remove entities, unload world, release memory.
        logger.debug("DungeonCleanupService.cleanup() — pending implementation.");
        instance.cleanup();
    }
}
