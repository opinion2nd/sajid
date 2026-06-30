package com.ultimatedungeon.dungeon.generation;

import com.ultimatedungeon.config.files.DungeonConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.room.model.RoomConnection;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomGraph;
import com.ultimatedungeon.util.RandomUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Routes corridors between adjacent rooms to form a connected dungeon graph.
 *
 * <h3>Algorithm</h3>
 * <ol>
 *   <li>Build a minimum spanning tree (Prim's algorithm) over all rooms,
 *       treating straight-line Chebyshev distance as the edge weight.</li>
 *   <li>Add a small number of extra edges (cycles) so players have branching
 *       paths and dead ends rather than a single linear route.</li>
 *   <li>For each edge, determine the corridor axis (X or Z, whichever aligns
 *       better) and create a {@link RoomConnection} describing the tunnel.</li>
 * </ol>
 *
 * <h3>Async safety</h3>
 * Only produces {@link RoomConnection} value objects — no block placement here.
 * Block carving is done by {@link RoomPlacer}.
 */
public final class CorridorRouter {

    private final DungeonConfig dungeonConfig;
    private final PluginLogger  logger;

    public CorridorRouter(
            @NotNull final DungeonConfig dungeonConfig,
            @NotNull final PluginLogger  logger
    ) {
        this.dungeonConfig = dungeonConfig;
        this.logger        = logger;
    }

    /**
     * Routes corridors for all rooms in {@code graph}, adding
     * {@link RoomConnection} objects and updating room adjacency.
     *
     * @param graph the room graph produced by {@link LayoutPlanner}
     */
    public void route(@NotNull final RoomGraph graph) {
        final List<RoomData> rooms = new ArrayList<>(graph.getRooms());
        if (rooms.size() < 2) return;

        // ── Prim's MST ─────────────────────────────────────────────────────────
        final Set<String>          inTree = new HashSet<>();
        final List<RoomData[]>     edges  = new ArrayList<>();

        inTree.add(rooms.get(0).getRoomId());

        while (inTree.size() < rooms.size()) {
            RoomData bestFrom = null, bestTo = null;
            double   bestDist = Double.MAX_VALUE;

            for (final RoomData from : rooms) {
                if (!inTree.contains(from.getRoomId())) continue;
                for (final RoomData to : rooms) {
                    if (inTree.contains(to.getRoomId())) continue;
                    final double d = chebyshevDist(from, to);
                    if (d < bestDist) { bestDist = d; bestFrom = from; bestTo = to; }
                }
            }
            if (bestFrom == null) break;
            inTree.add(bestTo.getRoomId());
            edges.add(new RoomData[]{bestFrom, bestTo});
        }

        // ── Extra edges for branching ──────────────────────────────────────────
        final int extraEdges = Math.max(1, rooms.size() / 5);
        for (int i = 0; i < extraEdges; i++) {
            final RoomData a = RandomUtil.randomElement(rooms);
            final RoomData b = RandomUtil.randomElement(rooms);
            if (!a.getRoomId().equals(b.getRoomId())
                    && !a.getConnectedRoomIds().contains(b.getRoomId())) {
                edges.add(new RoomData[]{a, b});
            }
        }

        // ── Build connections ──────────────────────────────────────────────────
        for (final RoomData[] edge : edges) {
            final RoomConnection conn = buildConnection(edge[0], edge[1]);
            graph.addConnection(conn);
        }

        logger.debug("CorridorRouter: routed " + edges.size()
                + " corridors for " + rooms.size() + " rooms.");
    }

    // ── Private ───────────────────────────────────────────────────────────────

    /** Builds a straight corridor connection between two rooms. */
    @NotNull
    private RoomConnection buildConnection(
            @NotNull final RoomData from,
            @NotNull final RoomData to
    ) {
        final var fromCentre = from.getCentre();
        final var toCentre   = to.getCentre();

        // Determine axis — pick whichever offset is larger
        final int dx = Math.abs(toCentre.getBlockX() - fromCentre.getBlockX());
        final int dz = Math.abs(toCentre.getBlockZ() - fromCentre.getBlockZ());
        final RoomConnection.Axis axis = dx >= dz
                ? RoomConnection.Axis.X
                : RoomConnection.Axis.Z;

        final int length = RandomUtil.randomInt(
                dungeonConfig.getCorridorLengthMin(),
                dungeonConfig.getCorridorLengthMax()
        );

        return new RoomConnection(
                from.getRoomId(), to.getRoomId(),
                fromCentre, toCentre,
                axis, length
        );
    }

    private double chebyshevDist(@NotNull final RoomData a, @NotNull final RoomData b) {
        final var ac = a.getCentre(), bc = b.getCentre();
        return Math.max(
                Math.abs(bc.getBlockX() - ac.getBlockX()),
                Math.abs(bc.getBlockZ() - ac.getBlockZ())
        );
    }
}
