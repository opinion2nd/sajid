package com.ultimatedungeon.room.model;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Mutable data object representing a placed room in a dungeon layout.
 *
 * <p>Created by {@link com.ultimatedungeon.dungeon.generation.RoomPlacer} and
 * assembled into a {@link RoomGraph} by {@link com.ultimatedungeon.dungeon.generation.LayoutPlanner}.
 * Each room occupies an axis-aligned bounding box in world space.</p>
 */
public final class RoomData {

    private final String   roomId;
    private final RoomType type;
    private final Location origin;   // south-west-bottom corner (min XYZ)
    private final int      width;    // X axis
    private final int      height;   // Y axis
    private final int      depth;    // Z axis

    private volatile boolean cleared;
    private volatile boolean entered;

    /** IDs of rooms this room is directly connected to via corridors. */
    private final List<String> connectedRoomIds = new ArrayList<>();

    public RoomData(
            @NotNull final String   roomId,
            @NotNull final RoomType type,
            @NotNull final Location origin,
            final int               width,
            final int               height,
            final int               depth
    ) {
        this.roomId   = roomId;
        this.type     = type;
        this.origin   = origin.clone();
        this.width    = width;
        this.height   = height;
        this.depth    = depth;
        this.cleared  = false;
        this.entered  = false;
    }

    // ── Geometry ───────────────────────────────────────────────────────────────

    /** Returns the block at the exact centre of this room at floor level. */
    @NotNull
    public Location getCentre() {
        return origin.clone().add(width / 2.0, 1, depth / 2.0);
    }

    /**
     * Returns {@code true} if the given location falls inside this room's
     * bounding box (ignoring Y within the room height).
     */
    public boolean contains(@NotNull final Location loc) {
        if (!loc.getWorld().equals(origin.getWorld())) return false;
        return loc.getBlockX() >= origin.getBlockX()
            && loc.getBlockX() <  origin.getBlockX() + width
            && loc.getBlockZ() >= origin.getBlockZ()
            && loc.getBlockZ() <  origin.getBlockZ() + depth
            && loc.getBlockY() >= origin.getBlockY()
            && loc.getBlockY() <  origin.getBlockY() + height;
    }

    /**
     * Returns {@code true} if this room's bounding box overlaps with {@code other}.
     * Used by the generator to prevent rooms from intersecting.
     */
    public boolean overlaps(@NotNull final RoomData other) {
        final int ax1 = origin.getBlockX(),      az1 = origin.getBlockZ();
        final int ax2 = ax1 + width,             az2 = az1 + depth;
        final int bx1 = other.origin.getBlockX(),bz1 = other.origin.getBlockZ();
        final int bx2 = bx1 + other.width,       bz2 = bz1 + other.depth;
        return ax1 < bx2 && ax2 > bx1 && az1 < bz2 && az2 > bz1;
    }

    // ── Connections ────────────────────────────────────────────────────────────

    public void addConnection(@NotNull final String roomId) {
        if (!connectedRoomIds.contains(roomId)) connectedRoomIds.add(roomId);
    }

    @NotNull
    public List<String> getConnectedRoomIds() {
        return Collections.unmodifiableList(connectedRoomIds);
    }

    // ── Getters ────────────────────────────────────────────────────────────────
    @NotNull public String   getRoomId()  { return roomId;  }
    @NotNull public RoomType getType()    { return type;    }
    @NotNull public Location getOrigin()  { return origin.clone(); }
    public int               getWidth()   { return width;   }
    public int               getHeight()  { return height;  }
    public int               getDepth()   { return depth;   }
    public boolean           isCleared()  { return cleared; }
    public boolean           isEntered()  { return entered; }
    public void              setCleared() { this.cleared = true; }
    public void              setEntered() { this.entered = true; }
    /** Clears the entered/cleared flags so a wave room can be run again after its reset. */
    public void              reset()      { this.entered = false; this.cleared = false; }
}
