package com.ultimatedungeon.dungeon.instance;

import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Tears down a completed or failed dungeon instance fully and safely.
 *
 * <p>Every engine registers its own cleanup action at startup (wave mobs,
 * bosses + BossBars, traps, arena locks, origin-slot release...), so one call
 * here despawns everything the run created and frees its map area for reuse.</p>
 */
public final class DungeonCleanupService {

    private final PluginLogger logger;
    private final List<Consumer<UUID>> actions = new ArrayList<>();

    public DungeonCleanupService(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    /** Registers a per-instance cleanup action (called with the instance id). */
    public void registerAction(@NotNull final Consumer<UUID> action) {
        actions.add(action);
    }

    public void cleanup(@NotNull final DungeonInstance instance) {
        final UUID id = instance.getInstanceId();
        for (final Consumer<UUID> action : actions) {
            try {
                action.accept(id);
            } catch (final Exception e) {
                logger.warning("Cleanup action failed for " + id + ": " + e.getMessage());
            }
        }
        instance.cleanup();
        logger.debug("Instance cleaned up: " + id);
    }
}
