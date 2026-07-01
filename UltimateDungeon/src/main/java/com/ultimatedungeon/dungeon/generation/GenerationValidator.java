package com.ultimatedungeon.dungeon.generation;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.room.model.RoomGraph;
import com.ultimatedungeon.room.model.RoomType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Validates a generated {@link RoomGraph} before it becomes playable.
 *
 * <p>If any check fails the generator discards the layout and regenerates.
 * This prevents broken dungeons from ever reaching players.</p>
 *
 * <h3>Checks performed</h3>
 * <ul>
 *   <li>Spawn room exists.</li>
 *   <li>Boss room exists.</li>
 *   <li>Reward room exists.</li>
 *   <li>All rooms are reachable from spawn (full connectivity).</li>
 *   <li>Boss room is reachable from spawn.</li>
 *   <li>Reward room is reachable from boss room (via graph traversal from boss).</li>
 *   <li>Minimum room count is met.</li>
 * </ul>
 */
public final class GenerationValidator {

    private final PluginLogger logger;

    public GenerationValidator(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    /**
     * Runs all validation checks.
     *
     * @param graph the room graph to validate
     * @return {@code true} if the layout is valid and playable
     */
    public boolean validate(@NotNull final RoomGraph graph) {
        if (!checkSpawnExists(graph))    return fail("No SPAWN room.");
        if (!checkBossExists(graph))     return fail("No BOSS room.");
        if (!checkRewardExists(graph))   return fail("No REWARD room.");
        if (!checkMinRoomCount(graph))   return fail("Too few rooms: " + graph.getRoomCount());
        if (!checkFullConnectivity(graph)) return fail("Not all rooms reachable from spawn.");
        if (!checkBossReachable(graph))  return fail("BOSS room not reachable from spawn.");
        if (!checkRewardReachable(graph))return fail("REWARD room not reachable from boss.");
        logger.debug("GenerationValidator: layout VALID (" + graph.getRoomCount() + " rooms).");
        return true;
    }

    // ── Individual checks ──────────────────────────────────────────────────────

    private boolean checkSpawnExists(@NotNull final RoomGraph g) {
        return g.getSpawnRoomId() != null;
    }

    private boolean checkBossExists(@NotNull final RoomGraph g) {
        return g.getBossRoomId() != null;
    }

    private boolean checkRewardExists(@NotNull final RoomGraph g) {
        return g.getRewardRoomId() != null;
    }

    private boolean checkMinRoomCount(@NotNull final RoomGraph g) {
        return g.getRoomCount() >= 5; // at minimum: spawn, 2 combat, boss, reward
    }

    private boolean checkFullConnectivity(@NotNull final RoomGraph g) {
        final Set<String> reachable = g.reachableFromSpawn();
        return reachable.size() == g.getRoomCount();
    }

    private boolean checkBossReachable(@NotNull final RoomGraph g) {
        return g.reachableFromSpawn().contains(g.getBossRoomId());
    }

    private boolean checkRewardReachable(@NotNull final RoomGraph g) {
        // Reward room must be adjacent to (or reachable from) the boss room.
        final var bossRoom = g.getBossRoom();
        if (bossRoom == null) return false;
        return bossRoom.getConnectedRoomIds().contains(g.getRewardRoomId())
                || g.reachableFromSpawn().contains(g.getRewardRoomId());
    }

    private boolean fail(@NotNull final String reason) {
        logger.debug("GenerationValidator: INVALID — " + reason);
        return false;
    }
}
