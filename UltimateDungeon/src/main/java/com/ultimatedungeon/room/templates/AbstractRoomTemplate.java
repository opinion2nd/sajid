package com.ultimatedungeon.room.templates;

import com.ultimatedungeon.api.room.IRoom;
import com.ultimatedungeon.api.room.IRoomTemplate;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomType;
import com.ultimatedungeon.theme.model.ThemeBlockPalette;
import com.ultimatedungeon.util.BlockUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Base class for all dungeon room templates.
 *
 * <p>Provides the common hollow-box building logic: walls, floor, ceiling, and
 * interior air clearing. Concrete subclasses call {@link #buildHollowBox} and
 * then add type-specific decorations and spawn points.</p>
 *
 * <h3>Room coordinate system</h3>
 * {@code origin} is the south-west-bottom corner (min X, min Y, min Z).
 * All placements use positive offsets from this corner.
 *
 * <h3>Thread safety</h3>
 * Block placement must happen on the main server thread. The async generation
 * pipeline schedules placement via the main thread scheduler.
 */
public abstract class AbstractRoomTemplate implements IRoomTemplate {

    // Default room dimensions — subclasses may override
    protected static final int DEFAULT_WIDTH  = 15;
    protected static final int DEFAULT_HEIGHT = 7;
    protected static final int DEFAULT_DEPTH  = 15;

    private final String   templateId;
    private final RoomType roomType;
    private final int      weight;

    protected AbstractRoomTemplate(
            @NotNull final String   templateId,
            @NotNull final RoomType roomType,
            final int               weight
    ) {
        this.templateId = templateId;
        this.roomType   = roomType;
        this.weight     = weight;
    }

    // ── IRoomTemplate ─────────────────────────────────────────────────────────

    @Override @NotNull public String   getTemplateId() { return templateId; }
    @Override          public int      getWeight()     { return weight;     }
    @NotNull           public RoomType getRoomType()   { return roomType;   }

    @Override
    @NotNull
    public IRoom place(@NotNull final Location origin) {
        final String   roomId  = UUID.randomUUID().toString().substring(0, 8);
        final RoomData data    = new RoomData(
                roomId, roomType, origin,
                getWidth(), getHeight(), getDepth());
        placeBlocks(origin, null); // palette supplied at generate time
        return new PlacedRoom(data);
    }

    /**
     * Places this room's blocks using the given theme palette.
     * Called by the generation pipeline with the correct theme.
     *
     * @param origin  south-west-bottom corner
     * @param palette theme palette (null = use stone defaults for testing)
     * @return the created RoomData
     */
    @NotNull
    public RoomData placeWithPalette(
            @NotNull final Location        origin,
            @NotNull final ThemeBlockPalette palette
    ) {
        final String   roomId = UUID.randomUUID().toString().substring(0, 8);
        final RoomData data   = new RoomData(
                roomId, roomType, origin,
                getWidth(), getHeight(), getDepth());
        buildHollowBox(origin, palette);
        decorateRoom(origin, data, palette);
        return data;
    }

    // ── Dimension hooks (subclasses may override) ─────────────────────────────

    public int getWidth()  { return DEFAULT_WIDTH;  }
    public int getHeight() { return DEFAULT_HEIGHT; }
    public int getDepth()  { return DEFAULT_DEPTH;  }

    // ── Abstract hooks ────────────────────────────────────────────────────────

    /**
     * Called after the hollow box is built. Subclasses place type-specific
     * decorations, chest positions, pressure plates, etc.
     */
    protected abstract void decorateRoom(
            @NotNull Location        origin,
            @NotNull RoomData        data,
            @NotNull ThemeBlockPalette palette
    );

    // ── Shared building helpers ───────────────────────────────────────────────

    /**
     * Builds a hollow rectangular box: solid walls/floor/ceiling, air inside.
     * Must be called on the main server thread.
     */
    protected void buildHollowBox(
            @NotNull final Location        origin,
            @NotNull final ThemeBlockPalette palette
    ) {
        final int w = getWidth(), h = getHeight(), d = getDepth();

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    final Location loc = origin.clone().add(x, y, z);
                    final Material mat;
                    if (y == 0) {
                        mat = palette.getFloor();
                    } else if (y == h - 1) {
                        // Dungeon worlds are pitch black — inlay ceiling lights on
                        // a grid so every room is naturally lit.
                        mat = (x > 0 && z > 0 && x % 4 == 2 && z % 4 == 2)
                                ? Material.GLOWSTONE : palette.getCeiling();
                    } else if (x == 0 || x == w - 1 || z == 0 || z == d - 1) {
                        // Vary wall material slightly for visual interest
                        mat = ((x + z) % 5 == 0) ? palette.getAccent()
                            : ((x + z) % 3 == 0) ? palette.getSecondary()
                            : palette.getPrimary();
                    } else {
                        mat = Material.AIR;
                    }
                    BlockUtil.setBlock(loc, mat);
                }
            }
        }
    }

    /** Places a block relative to the room origin. */
    protected void placeRelative(
            @NotNull final Location origin,
            final int               dx,
            final int               dy,
            final int               dz,
            @NotNull final Material material
    ) {
        BlockUtil.setBlock(origin.clone().add(dx, dy, dz), material);
    }

    // Fallback — called when no palette is provided (testing only)
    private void placeBlocks(@NotNull final Location origin, final ThemeBlockPalette ignored) {
        // no-op for the interface-only place() call
    }

    // ── PlacedRoom inner class ────────────────────────────────────────────────

    /** Simple IRoom adapter backed by a RoomData instance. */
    public static final class PlacedRoom implements IRoom {

        private final RoomData data;

        public PlacedRoom(@NotNull final RoomData data) { this.data = data; }

        @Override @NotNull public String   getRoomId()  { return data.getRoomId(); }
        @Override @NotNull public Location getOrigin()  { return data.getOrigin(); }
        @Override          public boolean  isCleared()  { return data.isCleared(); }
        @Override          public void     onEnter()    { data.setEntered(); }
        @Override          public void     onClear()    { data.setCleared(); }

        @NotNull public RoomData getData() { return data; }
    }
}
