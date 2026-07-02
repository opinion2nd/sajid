package com.ultimatedungeon.dungeon.world;

import com.ultimatedungeon.core.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Low-level provider that creates fully isolated, empty (void) worlds for
 * dungeon instances.
 *
 * <p>Dungeon worlds use a no-op {@link ChunkGenerator} so no terrain, mobs, or
 * structures are generated — only the blocks the dungeon generator places exist.
 * Natural mob spawning, weather and the day/night cycle are disabled, and
 * auto-save is turned off because dungeon worlds are disposable.</p>
 *
 * <p>World creation touches Bukkit world state and therefore must be called
 * from the main server thread.</p>
 */
public final class IsolatedWorldProvider {

    /** A chunk generator that produces nothing — an empty void world. */
    private static final class VoidChunkGenerator extends ChunkGenerator {
        // Intentionally empty: the base implementation generates no blocks,
        // which yields a void world across all supported platforms.
    }

    private final PluginLogger logger;

    public IsolatedWorldProvider(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    /**
     * Returns the world named {@code worldName}, creating it as an isolated void
     * world if it does not already exist.
     *
     * @return the world, or {@code null} if creation failed
     */
    @Nullable
    public World getOrCreateVoidWorld(@NotNull final String worldName) {
        final World existing = Bukkit.getWorld(worldName);
        if (existing != null) return existing;

        // Dungeon worlds are disposable: any world folder left from a previous
        // server session still contains old dungeon blocks, and new runs would
        // generate right on top of them (maps visibly "mixing"). Wipe it so
        // every server start begins with a truly empty void world.
        wipeStaleWorldFolder(worldName);

        try {
            final World world = new WorldCreator(worldName)
                    .generator(new VoidChunkGenerator())
                    .environment(World.Environment.NORMAL)
                    .generateStructures(false)
                    .createWorld();
            if (world == null) {
                logger.severe("Failed to create dungeon world: " + worldName);
                return null;
            }
            applyDungeonRules(world);
            logger.info("Isolated dungeon world ready: " + worldName);
            return world;
        } catch (final Exception e) {
            logger.severe("Exception creating dungeon world " + worldName, e);
            return null;
        }
    }

    /** Deletes a stale (unloaded) dungeon world folder from a previous session. */
    private void wipeStaleWorldFolder(@NotNull final String worldName) {
        final java.io.File folder = new java.io.File(Bukkit.getWorldContainer(), worldName);
        if (!folder.exists()) return;
        try {
            deleteRecursively(folder);
            logger.info("Wiped stale dungeon world from previous session: " + worldName);
        } catch (final Exception e) {
            logger.warning("Could not wipe stale dungeon world " + worldName
                    + ": " + e.getMessage());
        }
    }

    private void deleteRecursively(@NotNull final java.io.File file) {
        final java.io.File[] children = file.listFiles();
        if (children != null) {
            for (final java.io.File child : children) deleteRecursively(child);
        }
        if (!file.delete()) {
            logger.debug("Could not delete: " + file.getAbsolutePath());
        }
    }

    private void applyDungeonRules(@NotNull final World world) {
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.MOB_GRIEFING, false);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setTime(6000L);
        world.setStorm(false);
        world.setThundering(false);
        world.setAutoSave(false);
        world.setDifficulty(org.bukkit.Difficulty.HARD);
    }
}
