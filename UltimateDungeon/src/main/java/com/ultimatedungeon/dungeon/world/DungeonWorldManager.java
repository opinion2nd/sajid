package com.ultimatedungeon.dungeon.world;

import com.ultimatedungeon.core.PluginLogger;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Owns the dungeon world(s) and keeps survival worlds untouched.
 *
 * <p>The current strategy uses a single shared, isolated void world that all
 * dungeon instances are generated into (the layout planner offsets each run so
 * they do not collide). The shared world is created once, at plugin startup, on
 * the main thread; the async generation pipeline only ever <em>reads</em> the
 * already-created world reference, which is thread-safe.</p>
 */
public final class DungeonWorldManager {

    private final DungeonWorldFactory factory;
    private final PluginLogger logger;

    private volatile World dungeonWorld;

    public DungeonWorldManager(@NotNull final DungeonWorldFactory factory,
                               @NotNull final PluginLogger logger) {
        this.factory = factory;
        this.logger = logger;
    }

    /**
     * Ensures the shared dungeon world exists. Must be called from the main
     * thread (typically during plugin startup).
     */
    public void initialise() {
        if (dungeonWorld == null) {
            dungeonWorld = factory.createShared();
            if (dungeonWorld == null) {
                logger.warning("Dungeon world could not be created — dungeons will "
                        + "fall back to the default world.");
            }
        }
    }

    /** Returns the shared dungeon world, or {@code null} if it is unavailable. */
    @Nullable
    public World getDungeonWorld() {
        return dungeonWorld;
    }
}
