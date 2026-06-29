package com.ultimatedungeon.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Utility helpers for block inspection and placement.
 *
 * <p><strong>Async safety:</strong> Block placement must only be called from the
 * main server thread. Async tasks that need to place blocks must schedule the
 * placement via {@link com.ultimatedungeon.core.PluginScheduler#runSync(Runnable)}.</p>
 */
public final class BlockUtil {

    private BlockUtil() {}

    /**
     * Returns {@code true} if the block at {@code location} is solid (can stand on).
     *
     * @param location the location to check (block coordinates used)
     * @return {@code true} if the block is solid
     */
    public static boolean isSolid(@NotNull final Location location) {
        if (location.getWorld() == null) return false;
        return location.getBlock().getType().isSolid();
    }

    /**
     * Returns {@code true} if the block at {@code location} is passable (air, fluids).
     *
     * @param location the location to check
     * @return {@code true} if a player can pass through
     */
    public static boolean isPassable(@NotNull final Location location) {
        if (location.getWorld() == null) return false;
        return location.getBlock().isPassable();
    }

    /**
     * Returns {@code true} if the column of two blocks starting at {@code feet}
     * is a valid player spawn position (solid floor below, two passable blocks above).
     *
     * @param feet the feet-level block location
     * @return {@code true} if safe to teleport a player here
     */
    public static boolean isSafeSpawn(@NotNull final Location feet) {
        if (feet.getWorld() == null) return false;
        final Block floor  = feet.clone().subtract(0, 1, 0).getBlock();
        final Block body   = feet.getBlock();
        final Block head   = feet.clone().add(0, 1, 0).getBlock();
        return floor.getType().isSolid()
            && body.isPassable()
            && head.isPassable();
    }

    /**
     * Sets a block to the given material. Must be called on the main thread.
     *
     * @param location the target location
     * @param material the material to set
     */
    public static void setBlock(@NotNull final Location location, @NotNull final Material material) {
        if (location.getWorld() == null) return;
        location.getBlock().setType(material, false); // false = skip physics updates
    }

    /**
     * Replaces a block only if it currently matches {@code expected}.
     *
     * @param location the target location
     * @param expected the expected current material
     * @param replacement the material to place
     * @return {@code true} if the replacement was made
     */
    public static boolean replaceBlock(
            @NotNull final Location location,
            @NotNull final Material expected,
            @NotNull final Material replacement
    ) {
        if (location.getWorld() == null) return false;
        final Block block = location.getBlock();
        if (block.getType() != expected) return false;
        block.setType(replacement, false);
        return true;
    }
}
