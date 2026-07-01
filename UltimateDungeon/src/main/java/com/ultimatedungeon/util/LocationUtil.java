package com.ultimatedungeon.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * Utility helpers for {@link Location} operations.
 */
public final class LocationUtil {

    private LocationUtil() {}

    /**
     * Returns the block centre of the given location (adds 0.5 to X and Z).
     *
     * @param location the location to centre
     * @return a new centred location (does not modify the original)
     */
    @NotNull
    public static Location centre(@NotNull final Location location) {
        return location.clone().add(0.5, 0.0, 0.5);
    }

    /**
     * Returns {@code true} if both locations are in the same world and
     * within {@code radius} blocks of each other (Euclidean distance).
     *
     * @param a      first location
     * @param b      second location
     * @param radius maximum distance
     * @return {@code true} if within range
     */
    public static boolean isWithinRadius(
            @NotNull final Location a,
            @NotNull final Location b,
            final double            radius
    ) {
        if (a.getWorld() == null || !a.getWorld().equals(b.getWorld())) return false;
        return a.distanceSquared(b) <= radius * radius;
    }

    /**
     * Returns a location offset by the given block delta values.
     *
     * @param origin the base location (not modified)
     * @param dx     block offset on the X axis
     * @param dy     block offset on the Y axis
     * @param dz     block offset on the Z axis
     * @return the offset location
     */
    @NotNull
    public static Location offset(
            @NotNull final Location origin,
            final int               dx,
            final int               dy,
            final int               dz
    ) {
        return origin.clone().add(dx, dy, dz);
    }

    /**
     * Returns a safe string representation of a location for debug logging.
     *
     * @param location the location to format
     * @return e.g. {@code "world@128,64,-256"}
     */
    @NotNull
    public static String format(@NotNull final Location location) {
        final World world = location.getWorld();
        return (world != null ? world.getName() : "null")
               + "@" + location.getBlockX()
               + "," + location.getBlockY()
               + "," + location.getBlockZ();
    }
}
