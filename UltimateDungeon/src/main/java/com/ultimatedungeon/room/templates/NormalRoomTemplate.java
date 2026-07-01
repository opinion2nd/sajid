package com.ultimatedungeon.room.templates;

import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomType;
import com.ultimatedungeon.theme.model.ThemeBlockPalette;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Room template: NormalRoomTemplate (NORMAL).
 *
 * <p>A plain, empty room with no encounter — used so that not every room in a
 * dungeon contains a fight, giving the layout a more natural rhythm. Lightly
 * decorated with accent pillars.</p>
 */
public final class NormalRoomTemplate extends AbstractRoomTemplate {

    public NormalRoomTemplate() {
        super("normal", RoomType.NORMAL, 30);
    }

    @Override public int getWidth() { return 11; }
    @Override public int getDepth() { return 11; }

    @Override
    protected void decorateRoom(
            @NotNull final Location          origin,
            @NotNull final RoomData          data,
            @NotNull final ThemeBlockPalette palette
    ) {
        final Material pillar = palette.getAccent();
        final int w = getWidth() - 2;
        final int d = getDepth() - 2;
        for (int y = 1; y <= 2; y++) {
            placeRelative(origin, 2, y, 2, pillar);
            placeRelative(origin, w - 1, y, 2, pillar);
            placeRelative(origin, 2, y, d - 1, pillar);
            placeRelative(origin, w - 1, y, d - 1, pillar);
        }
    }
}
