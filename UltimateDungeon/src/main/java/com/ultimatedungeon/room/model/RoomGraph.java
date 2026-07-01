package com.ultimatedungeon.room.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Directed graph of all rooms and corridor connections for one dungeon layout.
 *
 * <p>Built incrementally by {@link com.ultimatedungeon.dungeon.generation.LayoutPlanner}
 * and {@link com.ultimatedungeon.dungeon.generation.CorridorRouter}, then
 * validated by {@link com.ultimatedungeon.dungeon.generation.GenerationValidator}
 * before the dungeon becomes playable.</p>
 *
 * <p>Room ordering in {@code rooms} reflects insertion order (LinkedHashMap),
 * which is the breadth-first placement order from LayoutPlanner.</p>
 */
public final class RoomGraph {

    private final Map<String, RoomData>       rooms       = new LinkedHashMap<>();
    private final List<RoomConnection>        connections = new ArrayList<>();
    private String                            spawnRoomId;
    private String                            bossRoomId;
    private String                            rewardRoomId;
    private final List<String>                bossRoomIds = new ArrayList<>();

    // ── Mutation ───────────────────────────────────────────────────────────────

    public void addRoom(@NotNull final RoomData room) {
        rooms.put(room.getRoomId(), room);
        if (room.getType() == RoomType.SPAWN)  spawnRoomId  = room.getRoomId();
        if (room.getType() == RoomType.BOSS) {
            if (bossRoomId == null) bossRoomId = room.getRoomId();
            bossRoomIds.add(room.getRoomId());
        }
        if (room.getType() == RoomType.REWARD) rewardRoomId = room.getRoomId();
    }

    public void addConnection(@NotNull final RoomConnection connection) {
        connections.add(connection);
        final RoomData from = rooms.get(connection.getFromRoomId());
        final RoomData to   = rooms.get(connection.getToRoomId());
        if (from != null) from.addConnection(connection.getToRoomId());
        if (to   != null) to.addConnection(connection.getFromRoomId());
    }

    // ── Query ──────────────────────────────────────────────────────────────────

    @NotNull public Collection<RoomData>  getRooms()       { return Collections.unmodifiableCollection(rooms.values()); }
    @NotNull public List<RoomConnection>  getConnections() { return Collections.unmodifiableList(connections); }
    public int                            getRoomCount()   { return rooms.size(); }

    @Nullable public RoomData  getRoom(@NotNull final String id)       { return rooms.get(id);        }
    @Nullable public String    getSpawnRoomId()                        { return spawnRoomId;           }
    @Nullable public String    getBossRoomId()                         { return bossRoomId;            }
    @Nullable public String    getRewardRoomId()                       { return rewardRoomId;          }
    @Nullable public RoomData  getSpawnRoom()  { return spawnRoomId  != null ? rooms.get(spawnRoomId)  : null; }
    @Nullable public RoomData  getBossRoom()   { return bossRoomId   != null ? rooms.get(bossRoomId)   : null; }
    @Nullable public RoomData  getRewardRoom() { return rewardRoomId != null ? rooms.get(rewardRoomId) : null; }
    /** All boss-room ids (a dungeon can have several, each with its own boss). */
    @NotNull public List<String> getBossRoomIds() { return Collections.unmodifiableList(bossRoomIds); }

    /**
     * Returns all rooms of a specific type.
     */
    @NotNull
    public List<RoomData> getRoomsOfType(@NotNull final RoomType type) {
        return rooms.values().stream().filter(r -> r.getType() == type).toList();
    }

    /**
     * BFS from the spawn room — returns all room IDs reachable from spawn.
     * Used by the validator to check dungeon connectivity.
     */
    @NotNull
    public Set<String> reachableFromSpawn() {
        if (spawnRoomId == null) return Set.of();
        final Set<String>   visited = new LinkedHashSet<>();
        final Queue<String> queue   = new ArrayDeque<>();
        queue.add(spawnRoomId);
        while (!queue.isEmpty()) {
            final String current = queue.poll();
            if (!visited.add(current)) continue;
            final RoomData room = rooms.get(current);
            if (room != null) queue.addAll(room.getConnectedRoomIds());
        }
        return visited;
    }
}
