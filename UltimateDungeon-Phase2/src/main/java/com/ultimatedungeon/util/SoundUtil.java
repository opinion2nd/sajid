package com.ultimatedungeon.util;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Utility for playing sounds at locations or to individual players.
 */
public final class SoundUtil {

    private SoundUtil() {}

    /**
     * Plays a sound to a single player at their current location.
     *
     * @param player the recipient
     * @param sound  the sound key
     * @param volume volume (1.0 = normal)
     * @param pitch  pitch (1.0 = normal, 0.5 = half speed, 2.0 = double)
     */
    public static void playToPlayer(
            @NotNull final Player player,
            @NotNull final Sound  sound,
            final float           volume,
            final float           pitch
    ) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    /**
     * Plays a sound at a world location, audible to all nearby players.
     *
     * @param location the source location
     * @param sound    the sound key
     * @param volume   volume (1.0 = normal, controls range)
     * @param pitch    pitch multiplier
     */
    public static void playAtLocation(
            @NotNull final Location location,
            @NotNull final Sound    sound,
            final float             volume,
            final float             pitch
    ) {
        final World world = location.getWorld();
        if (world == null) return;
        world.playSound(location, sound, volume, pitch);
    }

    /**
     * Plays a UI sound to a player (click, confirm, deny, etc.).
     * Uses a fixed volume and pitch suitable for inventory GUIs.
     *
     * @param player the recipient
     * @param sound  the sound key
     */
    public static void playUiSound(@NotNull final Player player, @NotNull final Sound sound) {
        playToPlayer(player, sound, 0.5f, 1.0f);
    }
}
