package com.ultimatedungeon.dungeon.world;

import com.ultimatedungeon.core.PluginLogger;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Factory that names and creates dungeon worlds through the
 * {@link IsolatedWorldProvider}.
 *
 * <p>Keeps world-naming policy in one place so every dungeon world shares a
 * common prefix and is easy to identify and clean up.</p>
 */
public final class DungeonWorldFactory {

    private static final String WORLD_PREFIX = "ud_dungeon";

    private final IsolatedWorldProvider provider;
    private final PluginLogger logger;

    public DungeonWorldFactory(@NotNull final IsolatedWorldProvider provider,
                               @NotNull final PluginLogger logger) {
        this.provider = provider;
        this.logger = logger;
    }

    /** The shared dungeon world name used when instances are not world-isolated. */
    @NotNull
    public String sharedWorldName() {
        return WORLD_PREFIX + "_shared";
    }

    /** Creates (or returns) the shared dungeon world. */
    @Nullable
    public World createShared() {
        return provider.getOrCreateVoidWorld(sharedWorldName());
    }
}
