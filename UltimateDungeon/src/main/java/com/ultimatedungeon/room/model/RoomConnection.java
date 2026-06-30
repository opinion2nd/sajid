package com.ultimatedungeon.room.model;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a corridor connection between two rooms.
 *
 * <p>Corridors run as straight tunnels along one axis (X or Z).
 * The {@code startDoor} and {@code endDoor} are the block positions
 * at each end where the corridor meets its rooms.</p>
 */
public final class RoomConnection {

    public enum Axis { X, Z }

    private final String   fromRoomId;
    private final String   toRoomId;
    private final Location startDoor;
    private final Location endDoor;
    private final Axis     axis;
    private final int      length;

    public RoomConnection(
            @NotNull final String   fromRoomId,
            @NotNull final String   toRoomId,
            @NotNull final Location startDoor,
            @NotNull final Location endDoor,
            @NotNull final Axis     axis,
            final int               length
    ) {
        this.fromRoomId = fromRoomId;
        this.toRoomId   = toRoomId;
        this.startDoor  = startDoor.clone();
        this.endDoor    = endDoor.clone();
        this.axis       = axis;
        this.length     = length;
    }

    @NotNull public String   getFromRoomId() { return fromRoomId; }
    @NotNull public String   getToRoomId()   { return toRoomId;   }
    @NotNull public Location getStartDoor()  { return startDoor.clone(); }
    @NotNull public Location getEndDoor()    { return endDoor.clone();   }
    @NotNull public Axis     getAxis()       { return axis;       }
    public int               getLength()     { return length;     }
}
