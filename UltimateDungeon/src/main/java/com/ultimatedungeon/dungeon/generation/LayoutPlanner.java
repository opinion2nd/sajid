package com.ultimatedungeon.dungeon.generation;

import com.ultimatedungeon.config.files.DungeonConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomGraph;
import com.ultimatedungeon.room.model.RoomType;
import com.ultimatedungeon.room.registry.RoomRegistry;
import com.ultimatedungeon.room.templates.AbstractRoomTemplate;
import com.ultimatedungeon.theme.model.LayoutStyle;
import com.ultimatedungeon.theme.model.ThemeDefinition;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Plans the spatial layout of rooms for one dungeon run.
 *
 * <h3>Layout archetypes</h3>
 * Each theme selects one of five {@link LayoutStyle} archetypes so every map
 * has its own recognisable shape:
 * <ul>
 *   <li>{@link LayoutStyle#HUB_AND_SPOKE} — central plaza, four branching wings,
 *       boss at the tip of the longest wing.</li>
 *   <li>{@link LayoutStyle#WINDING_PATH} — one long serpentine path with side
 *       pockets, boss at the far end.</li>
 *   <li>{@link LayoutStyle#SYMMETRIC_AXIS} — straight processional axis with
 *       mirrored side chambers, elite antechamber guarding the boss.</li>
 *   <li>{@link LayoutStyle#CONCENTRIC_RINGS} — outer ring breached first,
 *       fighting inward ring by ring to the boss keep at the centre.</li>
 *   <li>{@link LayoutStyle#GRID_MAZE} — depth-first maze with dead ends holding
 *       treasure; boss in the cell farthest from spawn.</li>
 * </ul>
 *
 * <p>All planning is done on an abstract 2D grid of uniform cells (large enough
 * for the biggest room template plus a corridor gap) and is fully deterministic
 * for a given seed. Rooms share the same Y level.</p>
 *
 * <h3>Async safety</h3>
 * This class only builds {@link RoomData} value objects — it does NOT touch
 * Bukkit world blocks. Block placement is deferred to {@link RoomPlacer}.
 */
public final class LayoutPlanner {

    /** Uniform grid cell size: largest room footprint (25) + corridor gap. */
    private static final int CELL_SIZE = 30;
    /** Y level all rooms are planned on. */
    private static final int ROOM_Y = 64;

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
     * @param world       the world where the dungeon will be built (used only for
     *                    Location construction — no blocks touched here)
     * @param theme       the selected dungeon theme (drives layout style and room pools)
     * @param seed        deterministic seed for reproducible layouts
     * @param targetRooms total room budget for this run (from the selected level);
     *                    values below the playable minimum are clamped
     * @param origin      world-space origin of this instance's layout (each
     *                    instance gets its own origin so concurrent dungeons
     *                    never overlap)
     * @return a fully-connected {@link RoomGraph}
     */
    @NotNull
    public RoomGraph plan(
            @NotNull final org.bukkit.World world,
            @NotNull final ThemeDefinition  theme,
            final long                      seed,
            final int                       targetRooms,
            @NotNull final Location         origin
    ) {
        final Random rng   = new Random(seed);
        final int    total = Math.max(6, Math.min(targetRooms, 80));
        final LayoutStyle style = theme.getLayoutStyle();

        logger.debug("LayoutPlanner: planning " + total + " rooms, style=" + style
                + " (seed=" + seed + ")");

        // Cells in visit order. Index 0 = spawn, last = boss; the reward cell is
        // appended separately next to the boss.
        final List<Cell> cells = switch (style) {
            case HUB_AND_SPOKE    -> planHubAndSpoke(total - 1, rng);
            case WINDING_PATH     -> planWindingPath(total - 1, rng);
            case SYMMETRIC_AXIS   -> planSymmetricAxis(total - 1, rng);
            case CONCENTRIC_RINGS -> planConcentricRings(total - 1, rng);
            case GRID_MAZE        -> planGridMaze(total - 1, rng);
        };

        return buildGraph(world, origin, cells, rng);
    }

    // ── Grid cell model ───────────────────────────────────────────────────────

    /** One planned grid cell: position plus an optional forced room type. */
    private static final class Cell {
        final int gx;
        final int gz;
        RoomType forced; // null = pick dynamically

        Cell(final int gx, final int gz) { this(gx, gz, null); }
        Cell(final int gx, final int gz, final RoomType forced) {
            this.gx = gx;
            this.gz = gz;
            this.forced = forced;
        }

        long key() { return key(gx, gz); }
        static long key(final int gx, final int gz) {
            return ((long) gx << 32) ^ (gz & 0xFFFFFFFFL);
        }
    }

    // ── Archetype 1: hub-and-spoke (Ancient Ruins) ────────────────────────────

    /**
     * Central spawn hub with four spokes radiating out in the cardinal
     * directions. Spokes bend slightly for a ruined, organic feel. The boss
     * sits at the tip of the longest spoke.
     */
    @NotNull
    private List<Cell> planHubAndSpoke(final int budget, @NotNull final Random rng) {
        final List<Cell> cells = new ArrayList<>();
        final Set<Long> used = new HashSet<>();
        final Cell hub = new Cell(0, 0, RoomType.SPAWN);
        cells.add(hub);
        used.add(hub.key());

        final int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        final int interior = budget - 1; // minus hub
        // Longest spoke gets the remainder and carries the boss.
        final int base = interior / 4;
        final int[] lengths = {base, base, base, interior - base * 3};

        Cell bossTip = hub;
        for (int s = 0; s < 4; s++) {
            int gx = 0;
            int gz = 0;
            int dx = dirs[s][0];
            int dz = dirs[s][1];
            for (int step = 0; step < lengths[s]; step++) {
                // Occasionally sidestep so wings feel crumbled, not ruler-straight.
                int nx = gx + dx;
                int nz = gz + dz;
                if (step > 0 && rng.nextDouble() < 0.3) {
                    final int[] side = (dx == 0)
                            ? new int[]{rng.nextBoolean() ? 1 : -1, 0}
                            : new int[]{0, rng.nextBoolean() ? 1 : -1};
                    if (!used.contains(Cell.key(gx + side[0], gz + side[1]))) {
                        nx = gx + side[0];
                        nz = gz + side[1];
                    }
                }
                if (used.contains(Cell.key(nx, nz))) {
                    nx = gx + dx;
                    nz = gz + dz;
                    if (used.contains(Cell.key(nx, nz))) break; // spoke blocked
                }
                final Cell cell = new Cell(nx, nz);
                cells.add(cell);
                used.add(cell.key());
                gx = nx;
                gz = nz;
                bossTip = cell;
            }
        }
        // The last placed tip of the final (longest) spoke hosts the boss.
        bossTip.forced = RoomType.BOSS;
        moveToEnd(cells, bossTip);
        return cells;
    }

    // ── Archetype 2: winding path (Frozen Cavern) ─────────────────────────────

    /**
     * One long serpentine tunnel carved by a heading-biased random walk, with
     * small side pockets branching off. The boss waits at the far end.
     */
    @NotNull
    private List<Cell> planWindingPath(final int budget, @NotNull final Random rng) {
        final List<Cell> cells = new ArrayList<>();
        final Set<Long> used = new HashSet<>();
        Cell current = new Cell(0, 0, RoomType.SPAWN);
        cells.add(current);
        used.add(current.key());

        final int pathLength = Math.max(4, (int) Math.ceil(budget * 0.72));
        final int[][] dirs = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        int heading = rng.nextInt(4);
        final List<Cell> path = new ArrayList<>();
        path.add(current);

        int placed = 1;
        while (placed < pathLength) {
            // Prefer to keep heading; otherwise snake left/right. Never reverse.
            final int[] candidates = rng.nextBoolean()
                    ? new int[]{heading, (heading + 1) % 4, (heading + 3) % 4}
                    : new int[]{heading, (heading + 3) % 4, (heading + 1) % 4};
            Cell next = null;
            int nextHeading = heading;
            for (final int h : (rng.nextDouble() < 0.45
                    ? new int[]{candidates[1], candidates[0], candidates[2]}
                    : candidates)) {
                final int nx = current.gx + dirs[h][0];
                final int nz = current.gz + dirs[h][1];
                if (!used.contains(Cell.key(nx, nz))) {
                    next = new Cell(nx, nz);
                    nextHeading = h;
                    break;
                }
            }
            if (next == null) break; // walled in — path ends here
            cells.add(next);
            used.add(next.key());
            path.add(next);
            current = next;
            heading = nextHeading;
            placed++;
        }

        // Side pockets: grottos hanging off the main tunnel holding goodies.
        int pockets = budget - placed;
        int guard = pockets * 6;
        while (pockets > 0 && guard-- > 0 && path.size() > 2) {
            final Cell host = path.get(1 + rng.nextInt(path.size() - 2));
            final int[] d = dirs[rng.nextInt(4)];
            final int nx = host.gx + d[0];
            final int nz = host.gz + d[1];
            if (used.contains(Cell.key(nx, nz))) continue;
            final Cell pocket = new Cell(nx, nz, pocketType(rng));
            cells.add(pocket);
            used.add(pocket.key());
            pockets--;
        }

        // Boss at the far end of the tunnel.
        final Cell tail = path.get(path.size() - 1);
        tail.forced = RoomType.BOSS;
        moveToEnd(cells, tail);
        return cells;
    }

    // ── Archetype 3: symmetric axis (Corrupted Temple) ────────────────────────

    /**
     * A straight processional nave running north with mirrored side chapels on
     * both flanks, an elite antechamber before the inner sanctum, and the boss
     * at the axis end.
     */
    @NotNull
    private List<Cell> planSymmetricAxis(final int budget, @NotNull final Random rng) {
        // Axis length: roughly a third of the budget; two side cells per axis room.
        final int axisLen = Math.max(4, (int) Math.ceil((budget + 2) / 3.0));
        final List<Cell> cells = new ArrayList<>();
        final Set<Long> used = new HashSet<>();

        for (int z = 0; z < axisLen; z++) {
            final Cell cell = new Cell(0, z);
            if (z == 0)               cell.forced = RoomType.SPAWN;
            else if (z == axisLen - 1) cell.forced = RoomType.BOSS;
            else if (z == axisLen - 2) cell.forced = RoomType.ELITE_COMBAT; // antechamber
            cells.add(cell);
            used.add(cell.key());
        }

        // Mirrored chapels: fill remaining budget with (±1, z) pairs.
        int remaining = budget - axisLen;
        for (int z = 1; z < axisLen - 1 && remaining > 0; z++) {
            if (rng.nextDouble() < 0.25 && z != axisLen - 2) continue; // leave gaps
            final Cell west = new Cell(-1, z);
            cells.add(cells.size() - 1, west); // keep boss last
            used.add(west.key());
            remaining--;
            if (remaining <= 0) break;
            final Cell east = new Cell(1, z);
            cells.add(cells.size() - 1, east);
            used.add(east.key());
            remaining--;
        }
        // Second row of chapels for large maps.
        for (int z = 1; z < axisLen - 1 && remaining > 0; z++) {
            for (final int gx : new int[]{-2, 2}) {
                if (remaining <= 0) break;
                if (used.contains(Cell.key(gx, z))
                        || !used.contains(Cell.key(gx > 0 ? 1 : -1, z))) continue;
                final Cell outer = new Cell(gx, z);
                cells.add(cells.size() - 1, outer);
                used.add(outer.key());
                remaining--;
            }
        }
        return cells;
    }

    // ── Archetype 4: concentric rings (Volcanic Fortress) ─────────────────────

    /**
     * Square defensive rings around a central boss keep. Players spawn on the
     * outermost ring and fight inward. The cell directly north of the keep is
     * reserved for the reward vault.
     */
    @NotNull
    private List<Cell> planConcentricRings(final int budget, @NotNull final Random rng) {
        final List<Cell> cells = new ArrayList<>();
        final Set<Long> used = new HashSet<>();

        // How many rings does the budget need? ring r has 8r cells.
        int rings = 1;
        int capacity = 1 + 8; // centre + ring 1
        while (capacity < budget && rings < 4) {
            rings++;
            capacity += 8 * rings;
        }

        // Spawn gate: middle of the outer ring's south edge.
        final Cell spawn = new Cell(0, -rings, RoomType.SPAWN);
        cells.add(spawn);
        used.add(spawn.key());
        int remaining = budget - 2; // minus spawn and boss keep

        // Fill rings from the outside in, walking each ring's perimeter.
        for (int r = rings; r >= 1 && remaining > 0; r--) {
            final List<Cell> ring = ringPerimeter(r);
            // Rotate the starting point so each run breaches the walls elsewhere.
            java.util.Collections.rotate(ring, rng.nextInt(ring.size()));
            // Inner rings hold fewer, harder rooms — skip cells when over budget.
            for (final Cell cell : ring) {
                if (remaining <= 0) break;
                if (used.contains(cell.key())) continue;
                if (cell.gx == 0 && cell.gz == 1) continue; // reserved reward vault
                // Thin out rings we cannot fully afford, keeping spacing even.
                final int ringCells = 8 * r;
                if (remaining < ringCells && rng.nextDouble() < 0.35) continue;
                cells.add(cell);
                used.add(cell.key());
                remaining--;
            }
        }

        final Cell keep = new Cell(0, 0, RoomType.BOSS);
        cells.add(keep);
        return cells;
    }

    /** All cells on the square ring of Chebyshev radius {@code r}, in walk order. */
    @NotNull
    private List<Cell> ringPerimeter(final int r) {
        final List<Cell> ring = new ArrayList<>();
        for (int x = -r; x <= r; x++)      ring.add(new Cell(x, -r));
        for (int z = -r + 1; z <= r; z++)  ring.add(new Cell(r, z));
        for (int x = r - 1; x >= -r; x--)  ring.add(new Cell(x, r));
        for (int z = r - 1; z >= -r + 1; z--) ring.add(new Cell(-r, z));
        return ring;
    }

    // ── Archetype 5: grid maze (Forgotten Catacombs) ──────────────────────────

    /**
     * Depth-first backtracking maze. Dead ends become treasure, secret and trap
     * chambers; the boss crypt is the cell farthest (by tunnel distance) from
     * the entrance.
     */
    @NotNull
    private List<Cell> planGridMaze(final int budget, @NotNull final Random rng) {
        final Map<Long, Cell> byKey = new LinkedHashMap<>();
        final Map<Long, Long> parent = new HashMap<>();
        final Deque<Cell> stack = new ArrayDeque<>();
        final int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        final Cell start = new Cell(0, 0, RoomType.SPAWN);
        byKey.put(start.key(), start);
        stack.push(start);

        while (!stack.isEmpty() && byKey.size() < budget) {
            final Cell cur = stack.peek();
            final List<int[]> shuffled = new ArrayList<>(List.of(dirs));
            java.util.Collections.shuffle(shuffled, rng);
            Cell next = null;
            for (final int[] d : shuffled) {
                final int nx = cur.gx + d[0];
                final int nz = cur.gz + d[1];
                if (byKey.containsKey(Cell.key(nx, nz))) continue;
                // Keep the maze compact so corridors stay short.
                if (Math.abs(nx) > 6 || Math.abs(nz) > 6) continue;
                next = new Cell(nx, nz);
                break;
            }
            if (next == null) {
                stack.pop(); // dead end — backtrack
                continue;
            }
            byKey.put(next.key(), next);
            parent.put(next.key(), cur.key());
            stack.push(next);
        }

        final List<Cell> cells = new ArrayList<>(byKey.values());

        // Boss crypt: the cell deepest in the maze (longest parent chain).
        Cell deepest = start;
        int deepestDepth = 0;
        for (final Cell cell : cells) {
            int depth = 0;
            Long k = cell.key();
            while (parent.containsKey(k)) {
                k = parent.get(k);
                depth++;
            }
            if (depth > deepestDepth) {
                deepestDepth = depth;
                deepest = cell;
            }
        }
        deepest.forced = RoomType.BOSS;
        moveToEnd(cells, deepest);

        // Dead ends (one neighbour only) hold the catacombs' hidden riches.
        for (final Cell cell : cells) {
            if (cell.forced != null) continue;
            int neighbours = 0;
            for (final int[] d : dirs) {
                if (byKey.containsKey(Cell.key(cell.gx + d[0], cell.gz + d[1]))) neighbours++;
            }
            if (neighbours <= 1) cell.forced = pocketType(rng);
        }
        return cells;
    }

    // ── Graph assembly (shared by all archetypes) ─────────────────────────────

    @NotNull
    private RoomGraph buildGraph(
            @NotNull final org.bukkit.World world,
            @NotNull final Location         origin,
            @NotNull final List<Cell>       cells,
            @NotNull final Random           rng
    ) {
        final RoomGraph graph = new RoomGraph();
        final Set<Long> used = new HashSet<>();
        Cell bossCell = null;

        int placed = 0;
        for (final Cell cell : cells) {
            used.add(cell.key());
            final RoomType type = cell.forced != null
                    ? cell.forced
                    : pickRoomType(graph, placed, cells.size(), rng);
            graph.addRoom(placeRoom(type, gridToWorld(world, origin, cell.gx, cell.gz)));
            if (type == RoomType.BOSS) bossCell = cell;
            placed++;
        }

        // Reward vault: first free cell adjacent to the boss room.
        if (bossCell != null) {
            final int[][] dirs = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
            for (final int[] d : dirs) {
                final int nx = bossCell.gx + d[0];
                final int nz = bossCell.gz + d[1];
                if (used.contains(Cell.key(nx, nz))) continue;
                graph.addRoom(placeRoom(RoomType.REWARD, gridToWorld(world, origin, nx, nz)));
                break;
            }
        }

        logger.debug("LayoutPlanner: placed " + graph.getRoomCount() + " rooms.");
        return graph;
    }

    /** Room types used for dead ends and side pockets. */
    @NotNull
    private RoomType pocketType(@NotNull final Random rng) {
        final double roll = rng.nextDouble();
        if (roll < 0.35) return RoomType.TREASURE;
        if (roll < 0.55) return RoomType.SECRET;
        if (roll < 0.80) return RoomType.TRAP;
        return RoomType.PUZZLE;
    }

    @NotNull
    private RoomData placeRoom(@NotNull final RoomType type, @NotNull final Location origin) {
        final var template = roomRegistry.selectForType(type);
        final int w = template != null ? ((AbstractRoomTemplate) template).getWidth()  : 15;
        final int d = template != null ? ((AbstractRoomTemplate) template).getDepth()  : 15;
        return new RoomData(
                UUID.randomUUID().toString().substring(0, 8),
                type, origin, w, 7, d);
    }

    /**
     * Picks the next room type based on what's already been placed and the
     * configured frequencies. Each special room type rolls independently so
     * the configured chances mean what they say.
     */
    @NotNull
    private RoomType pickRoomType(
            @NotNull final RoomGraph graph,
            final int                placed,
            final int                total,
            @NotNull final Random    rng
    ) {
        // Early-game: more combat
        if (placed < 3) return RoomType.COMBAT;

        if (rng.nextDouble() < dungeonConfig.getPuzzleFrequency()
                && graph.getRoomsOfType(RoomType.PUZZLE).size() < 3)
            return RoomType.PUZZLE;
        if (rng.nextDouble() < dungeonConfig.getTrapFrequency()
                && graph.getRoomsOfType(RoomType.TRAP).size() < 4)
            return RoomType.TRAP;
        if (rng.nextDouble() < dungeonConfig.getSecretRoomChance()
                && graph.getRoomsOfType(RoomType.SECRET).isEmpty())
            return RoomType.SECRET;
        if (rng.nextDouble() < dungeonConfig.getEventChance()
                && graph.getRoomsOfType(RoomType.EVENT).size() < 2)
            return RoomType.EVENT;

        // Mid-game: inject elite and mini-boss
        if (placed > total / 2) {
            if (graph.getRoomsOfType(RoomType.ELITE_COMBAT).size() < 2)
                return RoomType.ELITE_COMBAT;
            if (graph.getRoomsOfType(RoomType.MINI_BOSS).isEmpty() && rng.nextDouble() < 0.15)
                return RoomType.MINI_BOSS;
        }

        // Inject a treasure and merchant room somewhere
        if (graph.getRoomsOfType(RoomType.TREASURE).isEmpty() && placed > 4)
            return RoomType.TREASURE;
        if (graph.getRoomsOfType(RoomType.MERCHANT).isEmpty() && placed > 6)
            return RoomType.MERCHANT;

        return RoomType.COMBAT;
    }

    /** Converts a grid cell to a world Location using the uniform cell size. */
    @NotNull
    private Location gridToWorld(
            @NotNull final org.bukkit.World world,
            @NotNull final Location         origin,
            final int                       gx,
            final int                       gz
    ) {
        return new Location(
                world,
                origin.getBlockX() + (long) gx * CELL_SIZE,
                ROOM_Y,
                origin.getBlockZ() + (long) gz * CELL_SIZE
        );
    }

    /** Moves {@code cell} to the end of the list so the boss is always placed last. */
    private void moveToEnd(@NotNull final List<Cell> cells, @NotNull final Cell cell) {
        cells.remove(cell);
        cells.add(cell);
    }
}
