package com.ultimatedungeon.room.templates;

import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomType;
import com.ultimatedungeon.theme.model.ThemeBlockPalette;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Room template: MiniBossRoomTemplate (MINI_BOSS).
 *
 * <p>Width x Depth: 21 x 21, height: 7. Accent-block pillars are
 * placed for visual identity; full type-specific decoration is wired in
 * the block-placement milestone.</p>
 */
public final class MiniBossRoomTemplate extends AbstractRoomTemplate {

    public MiniBossRoomTemplate() {
        super("mini_boss", RoomType.MINI_BOSS, 3);
    }

    @Override public int getWidth() { return 21; }
    @Override public int getDepth() { return 21; }

    @Override
    protected void decorateRoom(
            @NotNull final Location          origin,
            @NotNull final RoomData          data,
            @NotNull final ThemeBlockPalette palette
    ) {
        placeCornerPillars(origin, palette);
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
