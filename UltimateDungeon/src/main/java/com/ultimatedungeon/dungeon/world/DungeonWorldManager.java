package com.ultimatedungeon.dungeon.world;

import com.ultimatedungeon.core.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns dungeon worlds. Each dungeon instance gets its <strong>own</strong>
 * isolated void world, so two dungeons can never share space or overlap — every
 * instance builds at its world's origin without colliding with any other.
 *
 * <p>World creation and deletion touch Bukkit world state and must run on the
 * main server thread.</p>
 */
public final class DungeonWorldManager {

    private final DungeonWorldFactory factory;
    private final PluginLogger logger;

    /** Shared fallback world (used only if a per-instance world cannot be made). */
    private volatile World sharedWorld;
    private final Map<UUID, World> instanceWorlds = new ConcurrentHashMap<>();

    public DungeonWorldManager(@NotNull final DungeonWorldFactory factory,
                               @NotNull final PluginLogger logger) {
        this.factory = factory;
        this.logger = logger;
    }

    /** Ensures the shared fallback world exists. Call on the main thread at startup. */
    public void initialise() {
        if (sharedWorld == null) {
            sharedWorld = factory.createShared();
            if (sharedWorld == null) {
                logger.warning("Shared fallback dungeon world could not be created.");
            }
        }
    }

    /**
     * Creates a fresh isolated world for one dungeon instance. Must run on the
     * main thread. Falls back to the shared world if creation fails.
     */
    @Nullable
    public World createInstanceWorld(@NotNull final UUID instanceId) {
        final String name = "ud_dungeon_" + instanceId.toString().substring(0, 8);
        final World world = factory.createNamed(name);
        if (world != null) {
            instanceWorlds.put(instanceId, world);
            return world;
        }
        logger.warning("Per-instance world creation failed for " + instanceId + "; using shared world.");
        return sharedWorld;
    }

    /** Unloads and deletes an instance's world. Must run on the main thread. */
    public void destroyInstanceWorld(@NotNull final UUID instanceId) {
        final World world = instanceWorlds.remove(instanceId);
        if (world == null) return;

        final World fallback = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
        if (fallback != null) {
            for (final org.bukkit.entity.Player p : world.getPlayers()) {
                p.teleport(fallback.getSpawnLocation());
            }
        }
        final File folder = world.getWorldFolder();
        if (Bukkit.unloadWorld(world, false)) {
            deleteRecursively(folder);
            logger.debug("Destroyed dungeon world: " + world.getName());
        } else {
            logger.warning("Could not unload dungeon world: " + world.getName());
        }
    }

    /** Returns the shared fallback world, or {@code null} if it is unavailable. */
    @Nullable
    public World getDungeonWorld() {
        return sharedWorld;
    }

    private void deleteRecursively(@Nullable final File file) {
        if (file == null || !file.exists()) return;
        final File[] children = file.listFiles();
        if (children != null) {
            for (final File child : children) deleteRecursively(child);
        }
        if (!file.delete()) {
            logger.debug("Could not delete dungeon world file: " + file.getName());
        }
    }
}
