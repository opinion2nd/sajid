package com.ultimatedungeon.boss.arena;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Physically seals rooms during encounters: every opening in the room's walls
 * (the corridor doorways) is filled with BEDROCK when locked and restored to
 * air when unlocked — nobody gets in or out mid-fight. Supports several locked
 * rooms per instance (boss rooms, wave rooms, parkour and puzzle rooms).
 */
public final class ArenaLockdownManager {

    /** One sealed room: which room, and which blocks we bedrocked. */
    private static final class LockRecord {
        final RoomData room;
        final List<Location> sealedBlocks = new ArrayList<>();
        LockRecord(final RoomData room) { this.room = room; }
    }

    private final PluginLogger logger;
    private final Map<UUID, Map<String, LockRecord>> locks = new ConcurrentHashMap<>();

    public ArenaLockdownManager(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    /** Seals a room: every air gap in its walls becomes bedrock. */
    public void lock(@NotNull final UUID instanceId, @NotNull final RoomData room) {
        final Map<String, LockRecord> instanceLocks =
                locks.computeIfAbsent(instanceId, k -> new ConcurrentHashMap<>());
        if (instanceLocks.containsKey(room.getRoomId())) return;

        final LockRecord record = new LockRecord(room);
        sealOpenings(room, record);
        instanceLocks.put(room.getRoomId(), record);
        logger.debug("Room sealed for instance " + instanceId + ": " + room.getRoomId());
    }

    /** Unseals one room, restoring its doorways to air. */
    public void unlock(@NotNull final UUID instanceId, @NotNull final String roomId) {
        final Map<String, LockRecord> instanceLocks = locks.get(instanceId);
        if (instanceLocks == null) return;
        final LockRecord record = instanceLocks.remove(roomId);
        if (record != null) restore(record);
        if (instanceLocks.isEmpty()) locks.remove(instanceId);
    }

    /** Unseals every locked room of an instance (cleanup path). */
    public void unlock(@NotNull final UUID instanceId) {
        final Map<String, LockRecord> instanceLocks = locks.remove(instanceId);
        if (instanceLocks == null) return;
        for (final LockRecord record : instanceLocks.values()) restore(record);
        logger.debug("All rooms unsealed for instance " + instanceId);
    }

    public boolean isLocked(@NotNull final UUID instanceId) {
        final Map<String, LockRecord> instanceLocks = locks.get(instanceId);
        return instanceLocks != null && !instanceLocks.isEmpty();
    }

    public boolean isRoomLocked(@NotNull final UUID instanceId, @NotNull final String roomId) {
        final Map<String, LockRecord> instanceLocks = locks.get(instanceId);
        return instanceLocks != null && instanceLocks.containsKey(roomId);
    }

    /** The locked room a location falls inside, if any. */
    @Nullable
    public RoomData getLockedRoomAt(@NotNull final UUID instanceId, @NotNull final Location loc) {
        final Map<String, LockRecord> instanceLocks = locks.get(instanceId);
        if (instanceLocks == null) return null;
        for (final LockRecord record : instanceLocks.values()) {
            if (record.room.contains(loc)) return record.room;
        }
        return null;
    }

    /** Kept for compatibility: the instance's locked BOSS room (or any locked room). */
    @Nullable
    public RoomData getArena(@NotNull final UUID instanceId) {
        final Map<String, LockRecord> instanceLocks = locks.get(instanceId);
        if (instanceLocks == null) return null;
        RoomData any = null;
        for (final LockRecord record : instanceLocks.values()) {
            any = record.room;
            if (record.room.getType() == RoomType.BOSS) return record.room;
        }
        return any;
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void sealOpenings(@NotNull final RoomData room, @NotNull final LockRecord record) {
        final Location origin = room.getOrigin();
        final World world = origin.getWorld();
        if (world == null) return;
        final int w = room.getWidth();
        final int d = room.getDepth();
        // Scan every wall cell at walking heights; air = doorway -> bedrock.
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                if (x != 0 && x != w - 1 && z != 0 && z != d - 1) continue;
                for (int y = 1; y <= 3; y++) {
                    final Location at = origin.clone().add(x, y, z);
                    if (world.getBlockAt(at).getType().isAir()) {
                        world.getBlockAt(at).setType(Material.BEDROCK, false);
                        record.sealedBlocks.add(at);
                    }
                }
            }
        }
    }

    private void restore(@NotNull final LockRecord record) {
        for (final Location at : record.sealedBlocks) {
            final World world = at.getWorld();
            if (world != null && world.getBlockAt(at).getType() == Material.BEDROCK) {
                world.getBlockAt(at).setType(Material.AIR, false);
            }
        }
        record.sealedBlocks.clear();
    }
}
