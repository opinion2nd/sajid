package com.ultimatedungeon.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * Utility for spawning particles at world locations.
 *
 * <p>All particle spawning must occur on the main server thread.</p>
 */
public final class ParticleUtil {

    private ParticleUtil() {}

    /**
     * Spawns {@code count} particles of the given type at {@code location}.
     *
     * @param location  the centre position
     * @param particle  the particle type
     * @param count     number of particles
     * @param offsetX   random offset radius on X
     * @param offsetY   random offset radius on Y
     * @param offsetZ   random offset radius on Z
     * @param speed     extra data / speed (interpretation depends on particle type)
     */
    public static void spawn(
            @NotNull final Location location,
            @NotNull final Particle particle,
            final int               count,
            final double            offsetX,
            final double            offsetY,
            final double            offsetZ,
            final double            speed
    ) {
        final World world = location.getWorld();
        if (world == null) return;
        world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
    }

    /**
     * Spawns a single directional particle (e.g. REDSTONE or DUST).
     *
     * @param location the spawn location
     * @param particle the particle type
     * @param data     particle data (e.g. {@link org.bukkit.Particle.DustOptions})
     * @param <T>      data type
     */
    public static <T> void spawnWithData(
            @NotNull final Location location,
            @NotNull final Particle particle,
            final T                 data
    ) {
        final World world = location.getWorld();
        if (world == null) return;
        world.spawnParticle(particle, location, 1, 0, 0, 0, 0, data);
    }

    /**
     * Spawns particles in a circle ring around a centre point at y-level {@code location.getY()}.
     *
     * @param centre   the centre of the ring
     * @param particle the particle type
     * @param radius   radius of the ring in blocks
     * @param density  number of particles evenly distributed around the ring
     */
    public static void spawnRing(
            @NotNull final Location centre,
            @NotNull final Particle particle,
            final double            radius,
            final int               density
    ) {
        final World world = centre.getWorld();
        if (world == null) return;
        final double step = 2.0 * Math.PI / density;
        for (int i = 0; i < density; i++) {
            final double angle = step * i;
            final double x     = centre.getX() + radius * Math.cos(angle);
            final double z     = centre.getZ() + radius * Math.sin(angle);
            world.spawnParticle(particle, x, centre.getY(), z, 1, 0, 0, 0, 0);
        }
    }
}
