package com.ultimatedungeon.room.templates;

import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomType;
import com.ultimatedungeon.theme.model.ThemeBlockPalette;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Room template: EliteCombatRoomTemplate (ELITE_COMBAT).
 *
 * <p>Width x Depth: 19 x 19, height: 7. Accent-block pillars are
 * placed for visual identity; full type-specific decoration is wired in
 * the block-placement milestone.</p>
 */
public final class EliteCombatRoomTemplate extends AbstractRoomTemplate {

    public EliteCombatRoomTemplate() {
        super("elite_combat", RoomType.ELITE_COMBAT, 15);
    }

    @Override public int getWidth() { return 13; }
    @Override public int getDepth() { return 13; }

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
