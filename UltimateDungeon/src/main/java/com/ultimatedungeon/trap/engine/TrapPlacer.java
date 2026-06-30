package com.ultimatedungeon.trap.engine;

import com.ultimatedungeon.room.model.RoomData;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Chooses floor positions inside a room to place traps. */
public final class TrapPlacer {

    /** Picks up to {@code count} distinct floor spots spread across a room. */
    @NotNull
    public List<Location> pickSpots(@NotNull final RoomData room, final int count) {
        final List<Location> spots = new ArrayList<>();
        final Location origin = room.getOrigin();
        if (origin.getWorld() == null) return spots;
        final int w = Math.max(1, room.getWidth() - 2);
        final int d = Math.max(1, room.getDepth() - 2);
        for (int i = 0; i < count; i++) {
            final int x = 1 + ThreadLocalRandom.current().nextInt(w);
            final int z = 1 + ThreadLocalRandom.current().nextInt(d);
            spots.add(origin.clone().add(x, 1, z));
        }
        return spots;
    }
}
