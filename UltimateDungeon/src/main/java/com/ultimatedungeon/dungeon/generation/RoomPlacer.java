package com.ultimatedungeon.dungeon.generation;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.room.model.RoomConnection;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomGraph;
import com.ultimatedungeon.room.registry.RoomRegistry;
import com.ultimatedungeon.room.templates.AbstractRoomTemplate;
import com.ultimatedungeon.theme.model.ThemeBlockPalette;
import com.ultimatedungeon.util.BlockUtil;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Places room and corridor blocks into the world.
 *
 * <p><strong>Must be called on the main server thread.</strong> All block
 * mutations are synchronous. The async pipeline calls this class via
 * {@link com.ultimatedungeon.core.PluginScheduler#runSync(Runnable)} after
 * generating the room graph on an async thread.</p>
 *
 * <h3>Placement order</h3>
 * <ol>
 *   <li>For each room: build the hollow box, then call the template's
 *       {@code decorateRoom} hook.</li>
 *   <li>For each corridor: carve a 3-wide, 3-tall tunnel connecting the
 *       two room centres along the dominant axis.</li>
 * </ol>
 */
public final class RoomPlacer {

    /** Corridor width (blocks) — 3 gives comfortable player movement. */
    private static final int CORRIDOR_WIDTH  = 3;
    /** Corridor height (blocks). */
    private static final int CORRIDOR_HEIGHT = 4;

    private final RoomRegistry roomRegistry;
    private final PluginLogger logger;

    public RoomPlacer(
            @NotNull final RoomRegistry roomRegistry,
            @NotNull final PluginLogger logger
    ) {
        this.roomRegistry = roomRegistry;
        this.logger       = logger;
    }

    /**
     * Places all rooms and corridors from {@code graph} into the world using
     * the given theme palette.
     *
     * @param graph   validated room graph
     * @param palette theme block palette
     */
    public void placeAll(
            @NotNull final RoomGraph        graph,
            @NotNull final ThemeBlockPalette palette
    ) {
        // Place rooms
        for (final RoomData room : graph.getRooms()) {
            placeRoom(room, palette);
        }
        // Carve corridors
        for (final RoomConnection conn : graph.getConnections()) {
            placeCorridorBlocks(conn, palette, graph);
        }
        logger.debug("RoomPlacer: placed " + graph.getRoomCount()
                + " rooms and " + graph.getConnections().size() + " corridors.");
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void placeRoom(
            @NotNull final RoomData         room,
            @NotNull final ThemeBlockPalette palette
    ) {
        final var template = roomRegistry.selectForType(room.getType());
        if (template instanceof final AbstractRoomTemplate abs) {
            // placeWithPalette builds the hollow box and decorates
            abs.placeWithPalette(room.getOrigin(), palette);
        } else {
            // Fallback: plain hollow box
            buildHollowBoxFallback(room, palette);
        }
    }

    /**
     * Builds an OPEN walkway between two rooms: a 3-wide floor path with clear
     * air above — no side walls, no ceiling, nothing that can block movement.
     *
     * <p>Inside a room's footprint nothing is built except the doorway punch
     * (3-wide, 3-high air through the wall), so corridors can never wall off
     * or slice through a room's interior.</p>
     */
    private void placeCorridorBlocks(
            @NotNull final RoomConnection   conn,
            @NotNull final ThemeBlockPalette palette,
            @NotNull final RoomGraph        graph
    ) {
        walkCorridor(conn, (world, x, floorY, z, alongX) ->
                carveOpenColumn(world, x, floorY, z, alongX, palette, graph));
    }

    /**
     * Removes every block a dungeon placed — rooms and corridors — so a
     * finished/failed instance fully despawns. Must run on the main thread.
     */
    public void clearAll(@NotNull final RoomGraph graph) {
        for (final RoomData room : graph.getRooms()) clearRoom(room);
        for (final RoomConnection conn : graph.getConnections()) clearCorridor(conn);
    }

    /** Sets one room's entire bounding box (plus a margin) to air. */
    public void clearRoom(@NotNull final RoomData room) {
        final var world = room.getOrigin().getWorld();
        if (world == null) return;
        final var origin = room.getOrigin();
        for (int x = -1; x <= room.getWidth(); x++) {
            for (int y = -1; y <= room.getHeight() + 1; y++) {
                for (int z = -1; z <= room.getDepth(); z++) {
                    BlockUtil.setBlock(origin.clone().add(x, y, z), Material.AIR);
                }
            }
        }
    }

    /** Clears one corridor's carved blocks back to air. */
    public void clearCorridor(@NotNull final RoomConnection conn) {
        walkCorridor(conn, (world, x, floorY, z, alongX) -> {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    for (int dy = 0; dy <= 3; dy++) {
                        BlockUtil.setBlock(new org.bukkit.Location(
                                world, x + dx, floorY + dy, z + dz), Material.AIR);
                    }
                }
            }
        });
    }

    /** Visits every column along a connection's path (main axis, then elbow). */
    private void walkCorridor(@NotNull final RoomConnection conn,
                              @NotNull final CorridorColumnVisitor visitor) {
        final var from = conn.getStartDoor();
        final var to   = conn.getEndDoor();
        final var world = from.getWorld();
        if (world == null) return;
        // Corridor floor sits at ROOM-FLOOR level (doors are one block above).
        final int floorY = from.getBlockY() - 1;

        final int startX = Math.min(from.getBlockX(), to.getBlockX());
        final int endX   = Math.max(from.getBlockX(), to.getBlockX());
        final int startZ = Math.min(from.getBlockZ(), to.getBlockZ());
        final int endZ   = Math.max(from.getBlockZ(), to.getBlockZ());

        if (conn.getAxis() == RoomConnection.Axis.X) {
            for (int x = startX; x <= endX; x++) visitor.visit(world, x, floorY, from.getBlockZ(), true);
            for (int z = startZ; z <= endZ; z++) visitor.visit(world, to.getBlockX(), floorY, z, false);
        } else {
            for (int z = startZ; z <= endZ; z++) visitor.visit(world, from.getBlockX(), floorY, z, false);
            for (int x = startX; x <= endX; x++) visitor.visit(world, x, floorY, to.getBlockZ(), true);
        }
    }

    @FunctionalInterface
    private interface CorridorColumnVisitor {
        void visit(@NotNull org.bukkit.World world, int x, int floorY, int z, boolean alongX);
    }

    /**
     * One slice of a decorated open walkway:
     * <ul>
     *   <li>3-wide floor with an accent centre line woven into it,</li>
     *   <li>low side railings (secondary block) so the path reads as a bridge,</li>
     *   <li>lanterns on the railings every few blocks for light and looks,</li>
     *   <li>clear air above — never a wall or ceiling that could block the way.</li>
     * </ul>
     * Inside a room's footprint only the 3-wide doorway is punched; the
     * walkway never places a block inside a room.
     */
    private void carveOpenColumn(
            @NotNull final org.bukkit.World world,
            final int x, final int floorY, final int z,
            final boolean alongX,
            @NotNull final ThemeBlockPalette palette,
            @NotNull final RoomGraph graph
    ) {
        final int along = alongX ? x : z;
        for (int o = -2; o <= 2; o++) {
            final int bx = alongX ? x : x + o;
            final int bz = alongX ? z + o : z;

            if (isInsideAnyRoom(graph, bx, bz)) {
                // Doorway punch only (3-wide): clear walking space through walls.
                if (Math.abs(o) <= 1) {
                    for (int dy = 1; dy <= 3; dy++) {
                        BlockUtil.setBlock(new org.bukkit.Location(world, bx, floorY + dy, bz), Material.AIR);
                    }
                }
                continue;
            }

            if (Math.abs(o) <= 1) {
                // Walkway floor: accent centre line woven every other pair.
                final Material floorMat = (o == 0 && ((along >> 1) & 1) == 0)
                        ? palette.getAccent() : palette.getFloor();
                BlockUtil.setBlock(new org.bukkit.Location(world, bx, floorY, bz), floorMat);
                for (int dy = 1; dy <= 3; dy++) {
                    BlockUtil.setBlock(new org.bukkit.Location(world, bx, floorY + dy, bz), Material.AIR);
                }
            } else {
                // Railing edge: support block, low rail, and a lantern every
                // 6 blocks (alternating sides) to light the way.
                BlockUtil.setBlock(new org.bukkit.Location(world, bx, floorY, bz), palette.getFloor());
                BlockUtil.setBlock(new org.bukkit.Location(world, bx, floorY + 1, bz), palette.getSecondary());
                final boolean lanternHere = Math.floorMod(along, 6) == 0
                        && ((Math.floorMod(along, 12) == 0) == (o < 0));
                BlockUtil.setBlock(new org.bukkit.Location(world, bx, floorY + 2, bz),
                        lanternHere ? Material.LANTERN : Material.AIR);
                BlockUtil.setBlock(new org.bukkit.Location(world, bx, floorY + 3, bz), Material.AIR);
            }
        }
    }

    /** True when the column lies strictly INSIDE a room (not on its wall shell). */
    private boolean isInsideAnyRoom(@NotNull final RoomGraph graph, final int x, final int z) {
        for (final RoomData room : graph.getRooms()) {
            final var o = room.getOrigin();
            if (x > o.getBlockX() && x < o.getBlockX() + room.getWidth() - 1
                    && z > o.getBlockZ() && z < o.getBlockZ() + room.getDepth() - 1) {
                return true;
            }
        }
        return false;
    }

    private void buildHollowBoxFallback(
            @NotNull final RoomData         room,
            @NotNull final ThemeBlockPalette palette
    ) {
        final int w = room.getWidth(), h = room.getHeight(), d = room.getDepth();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    final org.bukkit.Location loc = room.getOrigin().clone().add(x, y, z);
                    final Material mat;
                    if (y == 0)                               mat = palette.getFloor();
                    else if (y == h - 1)                      mat = palette.getCeiling();
                    else if (x == 0 || x == w-1 || z == 0 || z == d-1) mat = palette.getPrimary();
                    else                                      mat = Material.AIR;
                    BlockUtil.setBlock(loc, mat);
                }
            }
        }
    }
}
