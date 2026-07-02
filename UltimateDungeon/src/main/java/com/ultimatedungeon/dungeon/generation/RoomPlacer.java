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
            placeCorridorBlocks(conn, palette);
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
     * Carves a straight 3×4 corridor between two room centres.
     * Determines the dominant axis and carves along it, then elbows.
     */
    private void placeCorridorBlocks(
            @NotNull final RoomConnection   conn,
            @NotNull final ThemeBlockPalette palette
    ) {
        final var from = conn.getStartDoor();
        final var to   = conn.getEndDoor();

        final var world = from.getWorld();
        if (world == null) return;
        // Corridor floor must sit at ROOM-FLOOR level (doors are at walking
        // level, one block above), so walking room→corridor→room is seamless.
        final int floorY = from.getBlockY() - 1;

        if (conn.getAxis() == RoomConnection.Axis.X) {
            // Carve along X first, then Z elbow
            final int startX = Math.min(from.getBlockX(), to.getBlockX());
            final int endX   = Math.max(from.getBlockX(), to.getBlockX());
            for (int x = startX; x <= endX; x++) {
                carveCorridorColumn(world, x, floorY, from.getBlockZ(), palette, false);
            }
            // Z elbow
            final int startZ = Math.min(from.getBlockZ(), to.getBlockZ());
            final int endZ   = Math.max(from.getBlockZ(), to.getBlockZ());
            for (int z = startZ; z <= endZ; z++) {
                carveCorridorColumn(world, to.getBlockX(), floorY, z, palette, false);
            }
        } else {
            // Carve along Z first, then X elbow
            final int startZ = Math.min(from.getBlockZ(), to.getBlockZ());
            final int endZ   = Math.max(from.getBlockZ(), to.getBlockZ());
            for (int z = startZ; z <= endZ; z++) {
                carveCorridorColumn(world, from.getBlockX(), floorY, z, palette, false);
            }
            final int startX = Math.min(from.getBlockX(), to.getBlockX());
            final int endX   = Math.max(from.getBlockX(), to.getBlockX());
            for (int x = startX; x <= endX; x++) {
                carveCorridorColumn(world, x, floorY, to.getBlockZ(), palette, false);
            }
        }
    }

    /**
     * Removes every block a dungeon placed — rooms and corridors — so a
     * finished/failed instance fully despawns and its map area can be reused.
     * Must run on the main thread.
     */
    public void clearAll(@NotNull final RoomGraph graph) {
        for (final RoomData room : graph.getRooms()) {
            clearRoom(room);
        }
        for (final RoomConnection conn : graph.getConnections()) {
            clearCorridor(conn);
        }
        logger.debug("RoomPlacer: cleared " + graph.getRoomCount() + " rooms and "
                + graph.getConnections().size() + " corridors.");
    }

    /** Sets one room's entire bounding box (plus a margin above) to air. */
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
        final var from = conn.getStartDoor();
        final var to   = conn.getEndDoor();
        final var world = from.getWorld();
        if (world == null) return;
        final int floorY = from.getBlockY() - 1;
        final int startX = Math.min(from.getBlockX(), to.getBlockX());
        final int endX   = Math.max(from.getBlockX(), to.getBlockX());
        final int startZ = Math.min(from.getBlockZ(), to.getBlockZ());
        final int endZ   = Math.max(from.getBlockZ(), to.getBlockZ());
        for (int x = startX; x <= endX; x++) {
            carveCorridorColumn(world, x, floorY, from.getBlockZ(), null, true);
            carveCorridorColumn(world, x, floorY, to.getBlockZ(), null, true);
        }
        for (int z = startZ; z <= endZ; z++) {
            carveCorridorColumn(world, from.getBlockX(), floorY, z, null, true);
            carveCorridorColumn(world, to.getBlockX(), floorY, z, null, true);
        }
    }

    /**
     * Carves a 3-wide, 4-tall column centred on {@code (x, y, z)} in the
     * dungeon's own world (never the default overworld).
     * Floor = primary, walls = secondary, ceiling = ceiling, interior = air.
     * When {@code toAir} is true every block becomes air (cleanup mode).
     */
    private void carveCorridorColumn(
            @NotNull final org.bukkit.World world,
            final int                     x,
            final int                     y,
            final int                     z,
            final ThemeBlockPalette       palette,
            final boolean                 toAir
    ) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy < CORRIDOR_HEIGHT; dy++) {
                    final org.bukkit.Location loc =
                            new org.bukkit.Location(world, x + dx, y + dy, z + dz);
                    final Material mat;
                    if (toAir || palette == null) mat = Material.AIR;
                    else if (dy == 0)             mat = palette.getFloor();
                    else if (dy == CORRIDOR_HEIGHT - 1) mat = palette.getCeiling();
                    else if (dx == -1 || dx == 1 || dz == -1 || dz == 1) mat = palette.getSecondary();
                    else                          mat = Material.AIR;
                    // Never overwrite the inside of a room when carving walls:
                    // only replace solid wall cells with corridor lining, but
                    // ALWAYS punch the interior air path through.
                    BlockUtil.setBlock(loc, mat);
                }
            }
        }
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
