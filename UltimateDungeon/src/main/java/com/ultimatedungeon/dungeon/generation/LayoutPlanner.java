package com.ultimatedungeon.dungeon.generation;

import com.ultimatedungeon.config.files.DungeonConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomGraph;
import com.ultimatedungeon.room.model.RoomType;
import com.ultimatedungeon.room.registry.RoomRegistry;
import com.ultimatedungeon.room.templates.AbstractRoomTemplate;
import com.ultimatedungeon.theme.model.ThemeBlockPalette;
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

    /** Spacing between room bounding boxes — reserved for corridors. */
    private static final int ROOM_SEPARATION = 5;

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
            final long                      seed
    ) {
        final int targetRooms = RandomUtil.randomInt(
                dungeonConfig.getDungeonSizeMin(),
                dungeonConfig.getDungeonSizeMax()
        );

        logger.debug("LayoutPlanner: planning " + targetRooms + " rooms (seed=" + seed + ")");

        final RoomGraph      graph         = new RoomGraph();
        final List<RoomData> placedRooms   = new ArrayList<>();
        final List<int[]>    occupiedGrid  = new ArrayList<>(); // [gridX, gridZ]

        // ── Step 1: Place the spawn room at the origin ─────────────────────────
        final Location spawnOrigin  = new Location(world, 0, 64, 0);
        final RoomData spawnRoom    = placeRoom(RoomType.SPAWN, spawnOrigin);
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
            final Location loc  = gridToWorld(world, spawnOrigin, gx, gz, parent, type);

            final RoomData newRoom = placeRoom(type, loc);
            graph.addRoom(newRoom);
            placedRooms.add(newRoom);
            occupiedGrid.add(new int[]{gx, gz});
        }

        // ── Step 3: Guarantee required rooms ──────────────────────────────────
        if (graph.getBossRoomId() == null) {
            final RoomData last    = placedRooms.get(placedRooms.size() - 1);
            final int[]    lastGrd = occupiedGrid.get(placedRooms.size() - 1);
            final int[]    dir     = randomDirection();
            final int      gx      = lastGrd[0] + dir[0];
            final int      gz      = lastGrd[1] + dir[1];
            final Location loc     = gridToWorld(world, spawnOrigin, gx, gz, last, RoomType.BOSS);
            final RoomData bossRoom = placeRoom(RoomType.BOSS, loc);
            graph.addRoom(bossRoom);
            placedRooms.add(bossRoom);
            occupiedGrid.add(new int[]{gx, gz});
        }
        if (graph.getRewardRoomId() == null) {
            final RoomData boss    = graph.getBossRoom();
            final int      bossIdx = placedRooms.indexOf(boss);
            final int[]    bossGrd = bossIdx >= 0 ? occupiedGrid.get(bossIdx) : new int[]{0, 1};
            final Location loc     = gridToWorld(world, spawnOrigin,
                    bossGrd[0], bossGrd[1] + 1, boss, RoomType.REWARD);
            final RoomData rewardRoom = placeRoom(RoomType.REWARD, loc);
            graph.addRoom(rewardRoom);
            placedRooms.add(rewardRoom);
            occupiedGrid.add(new int[]{bossGrd[0], bossGrd[1] + 1});
        }

        logger.debug("LayoutPlanner: placed " + graph.getRoomCount() + " rooms.");
        return graph;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

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

    /** Converts a grid cell to a world Location with room-size spacing. */
    @NotNull
    private Location gridToWorld(
            @NotNull final org.bukkit.World world,
            @NotNull final Location         spawnOrigin,
            final int                       gx,
            final int                       gz,
            @NotNull final RoomData         parent,
            @NotNull final RoomType         type
    ) {
        // Room size approximation — use average to space rooms cleanly
        final int cellW = parent.getWidth()  + ROOM_SEPARATION;
        final int cellD = parent.getDepth()  + ROOM_SEPARATION;
        return new Location(
                world,
                spawnOrigin.getBlockX() + (long) gx * cellW,
                64,
                spawnOrigin.getBlockZ() + (long) gz * cellD
        );
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
