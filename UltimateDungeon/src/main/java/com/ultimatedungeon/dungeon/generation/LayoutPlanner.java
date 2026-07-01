package com.ultimatedungeon.dungeon.generation;

import com.ultimatedungeon.config.files.DungeonConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.room.model.RoomConnection;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomGraph;
import com.ultimatedungeon.room.model.RoomType;
import com.ultimatedungeon.room.registry.RoomRegistry;
import com.ultimatedungeon.room.templates.AbstractRoomTemplate;
import com.ultimatedungeon.theme.model.ThemeDefinition;
import com.ultimatedungeon.util.RandomUtil;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Plans the spatial layout of rooms for one dungeon run.
 *
 * <h3>Algorithm</h3>
 * <ol>
 *   <li>Always place a SPAWN room at the layout origin.</li>
 *   <li>Expand outward using BFS — for each placed room, pick a random
 *       direction and attempt to place the next room at a gap-separated offset.</li>
 *   <li>After the room budget is reached, force-place required rooms (BOSS,
 *       REWARD) if they are not yet present.</li>
 *   <li>Optionally inject SECRET, PUZZLE, TRAP rooms based on config
 *       probability weights.</li>
 * </ol>
 *
 * <p>All generated positions are on a 2D grid (rooms share the same Y level).
 * Rooms are separated by a gap of {@code corridorLengthMin} to prevent overlap
 * and to leave space for corridor carving.</p>
 *
 * <h3>Async safety</h3>
 * This class only builds {@link RoomData} value objects — it does NOT touch
 * Bukkit world blocks. Block placement is deferred to {@link RoomPlacer}.
 */
public final class LayoutPlanner {

    /**
     * Fixed cell size on the layout grid. Every room is centred inside its own
     * cell, so rooms can never overlap (as long as {@code CELL} exceeds the
     * largest room) and every room centre lands on a uniform {@code CELL} grid —
     * which keeps corridors perfectly axis-aligned between neighbours.
     */
    private static final int CELL = 32;
    /** Ground level every room floor sits on. */
    private static final int FLOOR_Y = 64;

    private final DungeonConfig dungeonConfig;
    private final RoomRegistry  roomRegistry;
    private final PluginLogger  logger;

    public LayoutPlanner(
            @NotNull final DungeonConfig dungeonConfig,
            @NotNull final RoomRegistry  roomRegistry,
            @NotNull final PluginLogger  logger
    ) {
        this.dungeonConfig = dungeonConfig;
        this.roomRegistry  = roomRegistry;
        this.logger        = logger;
    }

    /**
     * Plans a complete room graph for one dungeon run.
     *
     * @param world    the world where the dungeon will be built (used only for
     *                 Location construction — no blocks touched here)
     * @param theme    the selected dungeon theme (drives room pool choices)
     * @param seed     deterministic seed for reproducible layouts (for testing)
     * @return a fully-connected {@link RoomGraph}
     */
    @NotNull
    public RoomGraph plan(
            @NotNull final org.bukkit.World world,
            @NotNull final ThemeDefinition  theme,
            final long                      seed,
            final int                       level
    ) {
        // Dungeon size scales with level: level 1 is small, level 4 is large.
        // Clamped to the configured absolute min/max so config still bounds it.
        final int lvl    = Math.max(1, level);
        final int lvlMin = 5 + lvl * 2;                 // L1=7  L2=9  L3=11 L4=13
        final int lvlMax = 8 + lvl * 3;                 // L1=11 L2=14 L3=17 L4=20
        final int min    = Math.max(dungeonConfig.getDungeonSizeMin() - 5, lvlMin);
        final int max    = Math.min(dungeonConfig.getDungeonSizeMax(), lvlMax);
        final int targetRooms = RandomUtil.randomInt(Math.min(min, max), Math.max(min, max));

        logger.debug("LayoutPlanner: planning " + targetRooms + " rooms (seed=" + seed + ")");

        final RoomGraph      graph         = new RoomGraph();
        final List<RoomData> placedRooms   = new ArrayList<>();
        final List<int[]>    occupiedGrid  = new ArrayList<>(); // [gridX, gridZ]

        // ── Step 1: Place the spawn room at grid origin ────────────────────────
        final Location base = new Location(world, 0, FLOOR_Y, 0);
        final RoomData spawnRoom = placeRoom(RoomType.SPAWN, world, base, 0, 0);
        graph.addRoom(spawnRoom);
        placedRooms.add(spawnRoom);
        occupiedGrid.add(new int[]{0, 0});

        // ── Step 2: Expand room-by-room ────────────────────────────────────────
        int attempts = 0;
        while (placedRooms.size() < targetRooms - 2 && attempts < targetRooms * 5) {
            attempts++;

            // Pick a random existing room to expand from
            final RoomData parent = RandomUtil.randomElement(placedRooms);
            final int[] parentGrid = occupiedGrid.get(placedRooms.indexOf(parent));

            // Try a random direction
            final int[] dir    = randomDirection();
            final int   gx     = parentGrid[0] + dir[0];
            final int   gz     = parentGrid[1] + dir[1];

            if (isGridOccupied(occupiedGrid, gx, gz)) continue;

            final RoomType type = pickRoomType(graph, placedRooms.size(), targetRooms);
            final RoomData newRoom = placeRoom(type, world, base, gx, gz);
            graph.addRoom(newRoom);
            placedRooms.add(newRoom);
            occupiedGrid.add(new int[]{gx, gz});
        }

        // ── Step 3: Guarantee required rooms in free, non-overlapping cells ────
        if (graph.getBossRoomId() == null) {
            final int[] cell = freeAdjacentCell(occupiedGrid);
            final int gx = cell != null ? cell[0] : occupiedGrid.size();
            final int gz = cell != null ? cell[1] : 0;
            final RoomData bossRoom = placeRoom(RoomType.BOSS, world, base, gx, gz);
            graph.addRoom(bossRoom);
            placedRooms.add(bossRoom);
            occupiedGrid.add(new int[]{gx, gz});
        }
        if (graph.getRewardRoomId() == null) {
            final int[] cell = freeAdjacentCell(occupiedGrid);
            final int gx = cell != null ? cell[0] : occupiedGrid.size();
            final int gz = cell != null ? cell[1] : 1;
            final RoomData rewardRoom = placeRoom(RoomType.REWARD, world, base, gx, gz);
            graph.addRoom(rewardRoom);
            placedRooms.add(rewardRoom);
            occupiedGrid.add(new int[]{gx, gz});
        }

        // Connect every grid-adjacent pair of rooms with a short, wall-to-wall
        // corridor. Because rooms grow outward cell-by-cell the grid is one
        // connected blob, so this both guarantees connectivity and keeps every
        // corridor a clean straight road across the gap — never a tunnel driven
        // through a room's interior.
        connectGridAdjacent(world, graph, placedRooms, occupiedGrid);

        logger.debug("LayoutPlanner: placed " + graph.getRoomCount() + " rooms.");
        return graph;
    }

    /** Adds wall-to-wall corridors between rooms sitting in adjacent grid cells. */
    private void connectGridAdjacent(@NotNull final org.bukkit.World world, @NotNull final RoomGraph graph,
                                     @NotNull final List<RoomData> rooms, @NotNull final List<int[]> cells) {
        final java.util.Map<Long, RoomData> byCell = new java.util.HashMap<>();
        for (int i = 0; i < rooms.size(); i++) {
            byCell.put(cellKey(cells.get(i)[0], cells.get(i)[1]), rooms.get(i));
        }
        for (int i = 0; i < rooms.size(); i++) {
            final int gx = cells.get(i)[0];
            final int gz = cells.get(i)[1];
            final RoomData self = rooms.get(i);
            final RoomData east  = byCell.get(cellKey(gx + 1, gz)); // neighbour to +X
            final RoomData south = byCell.get(cellKey(gx, gz + 1)); // neighbour to +Z
            if (east  != null) graph.addConnection(bridge(world, self, east,  true));
            if (south != null) graph.addConnection(bridge(world, self, south, false));
        }
    }

    /**
     * Builds a corridor whose ends sit on the two rooms' facing walls, so the
     * carved road only spans the gap between them.
     *
     * @param alongX {@code true} = {@code b} is the +X neighbour of {@code a}
     */
    @NotNull
    private RoomConnection bridge(@NotNull final org.bukkit.World world, @NotNull final RoomData a,
                                  @NotNull final RoomData b, final boolean alongX) {
        final int doorY = a.getCentre().getBlockY();
        final Location start;
        final Location end;
        if (alongX) {
            final int aEast = a.getOrigin().getBlockX() + a.getWidth() - 1; // a's +X wall
            final int bWest = b.getOrigin().getBlockX();                    // b's -X wall
            final int z = a.getCentre().getBlockZ();
            start = new Location(world, aEast, doorY, z);
            end   = new Location(world, bWest, doorY, z);
            return new RoomConnection(a.getRoomId(), b.getRoomId(), start, end,
                    RoomConnection.Axis.X, Math.abs(bWest - aEast));
        }
        final int aSouth = a.getOrigin().getBlockZ() + a.getDepth() - 1;    // a's +Z wall
        final int bNorth = b.getOrigin().getBlockZ();                       // b's -Z wall
        final int x = a.getCentre().getBlockX();
        start = new Location(world, x, doorY, aSouth);
        end   = new Location(world, x, doorY, bNorth);
        return new RoomConnection(a.getRoomId(), b.getRoomId(), start, end,
                RoomConnection.Axis.Z, Math.abs(bNorth - aSouth));
    }

    private long cellKey(final int gx, final int gz) {
        return (((long) gx) << 32) ^ (gz & 0xffffffffL);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Creates a room of {@code type} centred inside grid cell {@code (gx, gz)}.
     * Centring keeps every room's centre on the uniform {@code CELL} grid so
     * corridors between neighbours are always straight and aligned.
     */
    @NotNull
    private RoomData placeRoom(@NotNull final RoomType type, @NotNull final org.bukkit.World world,
                               @NotNull final Location base, final int gx, final int gz) {
        final var template = roomRegistry.selectForType(type);
        final AbstractRoomTemplate at =
                (template instanceof final AbstractRoomTemplate a) ? a : null;
        final int w = at != null ? at.getWidth() : 15;
        final int d = at != null ? at.getDepth() : 15;
        final Location origin = new Location(world,
                base.getBlockX() + (long) gx * CELL + (CELL - w) / 2.0,
                FLOOR_Y,
                base.getBlockZ() + (long) gz * CELL + (CELL - d) / 2.0);
        return new RoomData(
                UUID.randomUUID().toString().substring(0, 8),
                type, origin, w, 7, d);
    }

    /**
     * Picks the next room type based on what's already been placed and
     * the configured weights / frequencies.
     */
    @NotNull
    private RoomType pickRoomType(
            @NotNull final RoomGraph graph,
            final int                placed,
            final int                total
    ) {
        // Early-game: more combat
        if (placed < 3) return RoomType.COMBAT;

        // Probability-weighted injection of special rooms
        final double roll = RandomUtil.random();
        if (roll < dungeonConfig.getPuzzleFrequency()
                && graph.getRoomsOfType(RoomType.PUZZLE).size() < 3)
            return RoomType.PUZZLE;
        if (roll < dungeonConfig.getTrapFrequency()
                && graph.getRoomsOfType(RoomType.TRAP).size() < 4)
            return RoomType.TRAP;
        if (roll < dungeonConfig.getSecretRoomChance()
                && graph.getRoomsOfType(RoomType.SECRET).isEmpty())
            return RoomType.SECRET;
        if (roll < dungeonConfig.getEventChance()
                && graph.getRoomsOfType(RoomType.EVENT).size() < 2)
            return RoomType.EVENT;

        // Mid-game: inject elite and mini-boss
        if (placed > total / 2) {
            if (graph.getRoomsOfType(RoomType.ELITE_COMBAT).size() < 2)
                return RoomType.ELITE_COMBAT;
            if (graph.getRoomsOfType(RoomType.MINI_BOSS).isEmpty() && roll < 0.15)
                return RoomType.MINI_BOSS;
        }

        // Inject a treasure and merchant room somewhere
        if (graph.getRoomsOfType(RoomType.TREASURE).isEmpty() && placed > 4)
            return RoomType.TREASURE;
        if (graph.getRoomsOfType(RoomType.MERCHANT).isEmpty() && placed > 6)
            return RoomType.MERCHANT;

        return RoomType.COMBAT;
    }

    /** Finds a free cell adjacent to any already-placed room, or {@code null}. */
    @org.jetbrains.annotations.Nullable
    private int[] freeAdjacentCell(@NotNull final List<int[]> grid) {
        final int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (final int[] cell : grid) {
            for (final int[] dir : dirs) {
                final int nx = cell[0] + dir[0];
                final int nz = cell[1] + dir[1];
                if (!isGridOccupied(grid, nx, nz)) return new int[]{nx, nz};
            }
        }
        return null;
    }

    /** Returns a random cardinal direction as [dx, dz]. */
    @NotNull
    private int[] randomDirection() {
        return switch (RandomUtil.randomInt(0, 3)) {
            case 0 -> new int[]{ 1,  0};
            case 1 -> new int[]{-1,  0};
            case 2 -> new int[]{ 0,  1};
            default-> new int[]{ 0, -1};
        };
    }

    private boolean isGridOccupied(@NotNull final List<int[]> grid, final int gx, final int gz) {
        for (final int[] cell : grid) {
            if (cell[0] == gx && cell[1] == gz) return true;
        }
        return false;
    }
}
