package com.ultimatedungeon.dungeon.instance;

import com.ultimatedungeon.room.model.RoomConnection;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomGraph;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Seals a room's doorways with bedrock while an encounter is in progress, so
 * players cannot leave a wave/boss room until it is cleared. The exact blocks
 * are remembered per room so they can be restored to air afterwards.
 */
public final class RoomSealer {

    private final Map<String, List<Location>> sealed = new ConcurrentHashMap<>();

    private static String key(final UUID instanceId, final RoomData room) {
        return instanceId + ":" + room.getRoomId();
    }

    /** Walls off every doorway of {@code room} with bedrock. */
    public void seal(@NotNull final UUID instanceId, @NotNull final RoomData room,
                     @NotNull final RoomGraph graph) {
        final List<Location> placed = new ArrayList<>();
        for (final RoomConnection conn : graph.getConnections()) {
            final Location door;
            if (conn.getFromRoomId().equals(room.getRoomId())) door = conn.getStartDoor();
            else if (conn.getToRoomId().equals(room.getRoomId())) door = conn.getEndDoor();
            else continue;
            sealOpening(door, conn.getAxis(), placed);
        }
        sealed.put(key(instanceId, room), placed);
    }

    /** Restores a previously sealed room's doorways to air. */
    public void unseal(@NotNull final UUID instanceId, @NotNull final RoomData room) {
        final List<Location> placed = sealed.remove(key(instanceId, room));
        if (placed == null) return;
        for (final Location loc : placed) {
            if (loc.getWorld() != null) loc.getBlock().setType(Material.AIR, false);
        }
    }

    /** Restores every sealed doorway for an instance to air (e.g. when its boss dies). */
    public void unsealInstance(@NotNull final UUID instanceId) {
        final String prefix = instanceId + ":";
        for (final String k : new ArrayList<>(sealed.keySet())) {
            if (!k.startsWith(prefix)) continue;
            final List<Location> placed = sealed.remove(k);
            if (placed == null) continue;
            for (final Location loc : placed) {
                if (loc.getWorld() != null) loc.getBlock().setType(Material.AIR, false);
            }
        }
    }

    /** Drops any tracked seals for an instance (world already gone on teardown). */
    public void clearInstance(@NotNull final UUID instanceId) {
        sealed.keySet().removeIf(k -> k.startsWith(instanceId + ":"));
    }

    private void sealOpening(@NotNull final Location door, @NotNull final RoomConnection.Axis axis,
                             @NotNull final List<Location> out) {
        final World world = door.getWorld();
        if (world == null) return;
        final int baseY = door.getBlockY() - 1; // room floor level
        for (int p = -1; p <= 1; p++) {
            for (int dy = 1; dy <= 3; dy++) {
                final int x = axis == RoomConnection.Axis.X ? door.getBlockX() : door.getBlockX() + p;
                final int z = axis == RoomConnection.Axis.X ? door.getBlockZ() + p : door.getBlockZ();
                final Location loc = new Location(world, x, baseY + dy, z);
                loc.getBlock().setType(Material.BEDROCK, false);
                out.add(loc);
            }
        }
    }
}
