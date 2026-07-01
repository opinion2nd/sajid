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
    private static final int CELL = 18;
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
        // Size and boss-room count scale with level. Boss rooms: L1/L2 = 1,
        // L3 = 2, L4 = 3, L5 = 4 — each holds exactly one boss, placed far apart.
        final int lvl        = Math.max(1, Math.min(5, level));
        final int bossRooms  = lvl <= 2 ? 1 : lvl - 1;
        final int roomCount  = RandomUtil.randomInt(3 + lvl * 3, 5 + lvl * 3); // L1 6-8 … L5 18-20
        final int normalGoal = Math.max(3, roomCount - bossRooms - 1);

        logger.debug("LayoutPlanner: planning ~" + roomCount + " rooms, " + bossRooms
                + " boss room(s) (level " + lvl + ", seed=" + seed + ")");

        final RoomGraph      graph        = new RoomGraph();
        final List<RoomData> placed       = new ArrayList<>();
        final List<int[]>    cells        = new ArrayList<>();  // [gx, gz]
        final List<Integer>  parentIdx    = new ArrayList<>();  // spanning-tree parent per room

        final Location base = new Location(world, 0, FLOOR_Y, 0);

        // Spawn room.
        addRoom(graph, placed, cells, parentIdx, placeRoom(RoomType.SPAWN, world, base, 0, 0),
                new int[]{0, 0}, -1);

        // Grow a branching tree of normal/special rooms (random walk off any
        // existing non-boss room), which yields corridors, branches and dead
        // ends rather than a solid grid.
        int attempts = 0;
        while (placed.size() < normalGoal && attempts < normalGoal * 15) {
            attempts++;
            final int pIdx = RandomUtil.randomInt(0, placed.size() - 1);
            final int[] pg = cells.get(pIdx);
            final int[] dir = randomDirection();
            final int gx = pg[0] + dir[0];
            final int gz = pg[1] + dir[1];
            if (isGridOccupied(cells, gx, gz)) continue;
            final RoomType type = pickRoomType(graph, placed.size(), roomCount);
            addRoom(graph, placed, cells, parentIdx, placeRoom(type, world, base, gx, gz),
                    new int[]{gx, gz}, pIdx);
        }

        // Boss rooms — each in a free cell far from spawn and from the others.
        final List<int[]> bossCells = new ArrayList<>();
        for (int b = 0; b < bossRooms; b++) {
            final int[] cell = farFreeCell(cells, bossCells);
            if (cell == null) break;
            final int neigh = occupiedNeighbourIndex(cell, cells);
            addRoom(graph, placed, cells, parentIdx,
                    placeRoom(RoomType.BOSS, world, base, cell[0], cell[1]), cell, neigh);
            bossCells.add(cell);
        }

        // Reward room adjacent to the blob.
        final int[] rewardCell = freeAdjacentCell(cells);
        if (rewardCell != null) {
            final int neigh = occupiedNeighbourIndex(rewardCell, cells);
            addRoom(graph, placed, cells, parentIdx,
                    placeRoom(RoomType.REWARD, world, base, rewardCell[0], rewardCell[1]), rewardCell, neigh);
        }

        // Corridors: spanning tree from parent links, then a few extra loops.
        for (int i = 1; i < placed.size(); i++) {
            final int p = parentIdx.get(i);
            if (p >= 0 && p < placed.size()) {
                graph.addConnection(bridgeCells(world, placed.get(i), placed.get(p),
                        cells.get(i), cells.get(p)));
            }
        }
        addLoops(world, graph, placed, cells, Math.max(1, roomCount / 6));

        logger.debug("LayoutPlanner: placed " + graph.getRoomCount() + " rooms, "
                + graph.getBossRoomIds().size() + " boss room(s).");
        return graph;
    }

    private void addRoom(@NotNull final RoomGraph graph, @NotNull final List<RoomData> placed,
                         @NotNull final List<int[]> cells, @NotNull final List<Integer> parentIdx,
                         @NotNull final RoomData room, @NotNull final int[] cell, final int parent) {
        graph.addRoom(room);
        placed.add(room);
        cells.add(cell);
        parentIdx.add(parent);
    }

    /** Bridges two grid-adjacent rooms, deriving the axis/direction from their cells. */
    @NotNull
    private RoomConnection bridgeCells(@NotNull final org.bukkit.World world, @NotNull final RoomData a,
                                       @NotNull final RoomData b, @NotNull final int[] ca, @NotNull final int[] cb) {
        final int dx = cb[0] - ca[0];
        if (dx == 1)  return bridge(world, a, b, true);   // b is +X of a
        if (dx == -1) return bridge(world, b, a, true);   // a is +X of b
        final int dz = cb[1] - ca[1];
        if (dz == 1)  return bridge(world, a, b, false);  // b is +Z of a
        return bridge(world, b, a, false);                // a is +Z of b
    }

    /** Adds up to {@code count} extra corridors between grid-adjacent rooms for loops. */
    private void addLoops(@NotNull final org.bukkit.World world, @NotNull final RoomGraph graph,
                          @NotNull final List<RoomData> placed, @NotNull final List<int[]> cells, final int count) {
        for (int k = 0; k < count && placed.size() > 2; k++) {
            final int i = RandomUtil.randomInt(0, placed.size() - 1);
            final int[] c = cells.get(i);
            final int[] dir = randomDirection();
            final int j = indexOfCell(cells, c[0] + dir[0], c[1] + dir[1]);
            if (j < 0) continue;
            if (placed.get(i).getConnectedRoomIds().contains(placed.get(j).getRoomId())) continue;
            graph.addConnection(bridgeCells(world, placed.get(i), placed.get(j), cells.get(i), cells.get(j)));
        }
    }

    /** Finds a free cell adjacent to the blob, farthest from spawn and other boss cells. */
    @org.jetbrains.annotations.Nullable
    private int[] farFreeCell(@NotNull final List<int[]> occupied, @NotNull final List<int[]> avoid) {
        final int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        int[] best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (final int[] c : occupied) {
            for (final int[] d : dirs) {
                final int nx = c[0] + d[0];
                final int nz = c[1] + d[1];
                if (isGridOccupied(occupied, nx, nz)) continue;
                double score = Math.hypot(nx, nz); // distance from spawn (0,0)
                double minAvoid = Double.MAX_VALUE;
                for (final int[] a : avoid) minAvoid = Math.min(minAvoid, Math.hypot(nx - a[0], nz - a[1]));
                if (!avoid.isEmpty()) score += minAvoid * 2.0;
                if (score > bestScore) { bestScore = score; best = new int[]{nx, nz}; }
            }
        }
        return best;
    }

    /** Index of an occupied cell grid-adjacent to {@code cell}, or 0 (spawn) as fallback. */
    private int occupiedNeighbourIndex(@NotNull final int[] cell, @NotNull final List<int[]> cells) {
        final int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (final int[] d : dirs) {
            final int idx = indexOfCell(cells, cell[0] + d[0], cell[1] + d[1]);
            if (idx >= 0) return idx;
        }
        return 0;
    }

    private int indexOfCell(@NotNull final List<int[]> cells, final int gx, final int gz) {
        for (int i = 0; i < cells.size(); i++) {
            if (cells.get(i)[0] == gx && cells.get(i)[1] == gz) return i;
        }
        return -1;
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
        // The first couple of rooms are calm so players ease in.
        if (placed < 2) return RandomUtil.random() < 0.5 ? RoomType.COMBAT : RoomType.NORMAL;

        final boolean mid = placed > total / 3;
        // Each special type is rolled independently and capped, so the mix is
        // varied and random — and crucially NOT every room is a combat/wave room.
        if (graph.getRoomsOfType(RoomType.PUZZLE).size() < 2 && chance(0.12)) return RoomType.PUZZLE;
        if (graph.getRoomsOfType(RoomType.TRAP).size() < 3 && chance(0.16)) return RoomType.TRAP;
        if (graph.getRoomsOfType(RoomType.PARKOUR).isEmpty() && chance(0.14)) return RoomType.PARKOUR;
        if (graph.getRoomsOfType(RoomType.SECRET).size() < 2 && chance(0.13)) return RoomType.SECRET;
        if (graph.getRoomsOfType(RoomType.TREASURE).size() < 2 && chance(0.14)) return RoomType.TREASURE;
        if (graph.getRoomsOfType(RoomType.EVENT).size() < 2 && chance(0.10)) return RoomType.EVENT;
        if (mid && graph.getRoomsOfType(RoomType.ELITE_COMBAT).size() < 2 && chance(0.18)) return RoomType.ELITE_COMBAT;
        if (mid && graph.getRoomsOfType(RoomType.MINI_BOSS).isEmpty() && chance(0.10)) return RoomType.MINI_BOSS;

        // Otherwise a roughly even split of combat and plain (empty) rooms.
        return RandomUtil.random() < 0.5 ? RoomType.COMBAT : RoomType.NORMAL;
    }

    private boolean chance(final double p) { return RandomUtil.random() < p; }

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
