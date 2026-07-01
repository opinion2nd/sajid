package com.ultimatedungeon.room.templates;

import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomType;
import com.ultimatedungeon.theme.model.ThemeBlockPalette;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Room template: ParkourRoomTemplate (PARKOUR).
 *
 * <p>A jump course of floating blocks leading to a gold reward pad. The blocks
 * float above the room floor, so a missed jump drops the player safely back to
 * the floor to retry rather than into the void.</p>
 */
public final class ParkourRoomTemplate extends AbstractRoomTemplate {

    /** The jump path as (x, z) offsets from the origin; each hop is reachable. */
    private static final int[][] PATH = {
            {3, 3}, {5, 4}, {7, 5}, {9, 6}, {11, 7}, {11, 9}, {9, 10}, {7, 11}, {5, 11}
    };
    private static final int JUMP_Y = 1;

    public ParkourRoomTemplate() {
        super("parkour", RoomType.PARKOUR, 8);
    }

    @Override public int getWidth() { return 15; }
    @Override public int getDepth() { return 15; }

    @Override
    protected void decorateRoom(
            @NotNull final Location          origin,
            @NotNull final RoomData          data,
            @NotNull final ThemeBlockPalette palette
    ) {
        // Floating jump blocks.
        for (final int[] p : PATH) {
            placeRelative(origin, p[0], JUMP_Y, p[1], palette.getAccent());
        }
        // Reward pad at the end of the course.
        final int[] end = PATH[PATH.length - 1];
        placeRelative(origin, end[0], JUMP_Y, end[1], Material.GOLD_BLOCK);
        placeRelative(origin, end[0], JUMP_Y + 1, end[1], Material.SEA_LANTERN);
    }
}
