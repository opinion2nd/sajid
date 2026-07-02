package com.ultimatedungeon.room.templates;

import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomType;
import com.ultimatedungeon.theme.model.ThemeBlockPalette;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Room template: PuzzleRoomTemplate (PUZZLE).
 *
 * <p>Width x Depth: 15 x 15, height: 7. Accent-block pillars are
 * placed for visual identity; full type-specific decoration is wired in
 * the block-placement milestone.</p>
 */
public final class PuzzleRoomTemplate extends AbstractRoomTemplate {

    public PuzzleRoomTemplate() {
        super("puzzle", RoomType.PUZZLE, 10);
    }

    @Override public int getWidth() { return 15; }
    @Override public int getDepth() { return 15; }

    @Override
    protected void decorateRoom(
            @NotNull final Location          origin,
            @NotNull final RoomData          data,
            @NotNull final ThemeBlockPalette palette
    ) {
        placeCornerPillars(origin, palette);
        placeFateChests(origin, palette);
    }

    /**
     * The Chest of Fate: three chests on pedestals across the room's centre.
     * A player may open only ONE — fortune, ambush or curse decides the rest.
     */
    private void placeFateChests(
            @NotNull final Location          origin,
            @NotNull final ThemeBlockPalette palette
    ) {
        final int midZ = getDepth() / 2;
        final int midX = getWidth() / 2;
        for (final int dx : new int[]{-4, 0, 4}) {
            placeRelative(origin, midX + dx, 1, midZ, palette.getAccent());
            placeRelative(origin, midX + dx, 2, midZ, Material.CHEST);
        }
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
