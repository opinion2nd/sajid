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
 * <p>Width x Depth: 21 x 25, height: 7. Accent-block pillars are
 * placed for visual identity; full type-specific decoration is wired in
 * the block-placement milestone.</p>
 */
public final class ParkourRoomTemplate extends AbstractRoomTemplate {

    public ParkourRoomTemplate() {
        super("parkour", RoomType.PARKOUR, 8);
    }

    @Override public int getWidth() { return 21; }
    @Override public int getDepth() { return 25; }

    @Override
    protected void decorateRoom(
            @NotNull final Location          origin,
            @NotNull final RoomData          data,
            @NotNull final ThemeBlockPalette palette
    ) {
        placeCornerPillars(origin, palette);
        placeJumpCourse(origin, palette);
    }

    /**
     * A zig-zag jump course across the room: floating accent platforms rising
     * and falling from one end to the other, with a gold goal marker. The room
     * stays sealed until a player reaches the far side.
     */
    private void placeJumpCourse(
            @NotNull final Location          origin,
            @NotNull final ThemeBlockPalette palette
    ) {
        final int w = getWidth();
        final int d = getDepth();
        int x = w / 2;
        int step = 0;
        for (int z = 4; z <= d - 5; z += 3) {
            // Deterministic zig-zag: swing left/right, alternate platform height.
            x += (step % 2 == 0 ? 3 : -3);
            x = Math.max(3, Math.min(w - 4, x));
            final int y = 1 + (step % 3 == 2 ? 2 : 1);
            placeRelative(origin, x,     y, z,     palette.getAccent());
            placeRelative(origin, x + 1, y, z,     palette.getAccent());
            step++;
        }
        // Goal marker at the far end.
        placeRelative(origin, w / 2, 1, d - 3, Material.GOLD_BLOCK);
        placeRelative(origin, w / 2, 2, d - 3, Material.AIR);
    }

    /**
     * Places accent-block pillars at the four inner corners.
     * Concrete decoration (chests, torches, puzzles, etc.) is added when
     * the content-placement system is implemented.
     */
    private void placeCornerPillars(
            @NotNull final Location          origin,
            @NotNull final ThemeBlockPalette palette
    ) {
        final Material pillar = palette.getAccent();
        final int w = getWidth() - 2;
        final int d = getDepth() - 2;
        for (int y = 1; y <= 3; y++) {
            placeRelative(origin, 2,     y, 2,     pillar);
            placeRelative(origin, w - 1, y, 2,     pillar);
            placeRelative(origin, 2,     y, d - 1, pillar);
            placeRelative(origin, w - 1, y, d - 1, pillar);
        }
    }
}
