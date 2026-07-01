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
     * Carves an L-shaped corridor between two room centres as two straight
     * halls. Each hall is walled only on the sides <em>perpendicular</em> to its
     * travel direction, so a hall never overwrites its own path — the bug that
     * previously left corridors sealed.
     */
    private void placeCorridorBlocks(
            @NotNull final RoomConnection   conn,
            @NotNull final ThemeBlockPalette palette
    ) {
        final var from = conn.getStartDoor();
        final var to   = conn.getEndDoor();
        // Carve in the same world the rooms live in (the dungeon world), taken
        // from the door location — never assume the default overworld.
        final org.bukkit.World world = from.getWorld() != null
                ? from.getWorld() : org.bukkit.Bukkit.getWorlds().get(0);
        // Doors sit one block above the room floor (room centre). Carve the
        // corridor floor flush with the room floor so players walk straight
        // through the doorway instead of hitting a one-block step.
        final int floorY = from.getBlockY() - 1;

        if (conn.getAxis() == RoomConnection.Axis.X) {
            carveHall(world, true,  from.getBlockZ(), from.getBlockX(), to.getBlockX(), floorY, palette);
            if (from.getBlockZ() != to.getBlockZ()) {
                carveHall(world, false, to.getBlockX(), from.getBlockZ(), to.getBlockZ(), floorY, palette);
            }
        } else {
            carveHall(world, false, from.getBlockX(), from.getBlockZ(), to.getBlockZ(), floorY, palette);
            if (from.getBlockX() != to.getBlockX()) {
                carveHall(world, true,  to.getBlockZ(), from.getBlockX(), to.getBlockX(), floorY, palette);
            }
        }
    }

    /**
     * Carves one straight hall: a 3-wide, 2-tall walkable passage with a floor,
     * ceiling and side walls on the two perpendicular edges.
     *
     * @param alongX     {@code true} = the hall runs along X (perpendicular = Z)
     * @param perpFixed  the fixed perpendicular coordinate (centre line)
     * @param travelFrom start of the travel range (either order)
     * @param travelTo   end of the travel range (either order)
     * @param floorY     world Y of the hall floor (flush with room floors)
     */
    private void carveHall(
            @NotNull final org.bukkit.World  world,
            final boolean                    alongX,
            final int                        perpFixed,
            final int                        travelFrom,
            final int                        travelTo,
            final int                        floorY,
            @NotNull final ThemeBlockPalette palette
    ) {
        final int start = Math.min(travelFrom, travelTo);
        final int end   = Math.max(travelFrom, travelTo);
        for (int t = start; t <= end; t++) {
            for (int p = -2; p <= 2; p++) {
                final int x = alongX ? t : perpFixed + p;
                final int z = alongX ? perpFixed + p : t;
                final boolean edge = (p == -2 || p == 2);
                setColumn(world, x, z, floorY, edge, t, palette);
            }
        }
    }

    /** Places a single corridor column: floor, two-tall body, ceiling. */
    private void setColumn(
            @NotNull final org.bukkit.World  world,
            final int                        x,
            final int                        z,
            final int                        floorY,
            final boolean                    edge,
            final int                        travel,
            @NotNull final ThemeBlockPalette palette
    ) {
        BlockUtil.setBlock(new org.bukkit.Location(world, x, floorY, z), palette.getFloor());
        final Material body = edge ? palette.getSecondary() : Material.AIR;
        BlockUtil.setBlock(new org.bukkit.Location(world, x, floorY + 1, z), body);
        BlockUtil.setBlock(new org.bukkit.Location(world, x, floorY + 2, z), body);
        // Light the ceiling periodically so corridors are not pitch black.
        final Material ceiling = (!edge && travel % 6 == 0) ? Material.GLOWSTONE : palette.getCeiling();
        BlockUtil.setBlock(new org.bukkit.Location(world, x, floorY + 3, z), ceiling);
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
