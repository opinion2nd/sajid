package com.ultimatedungeon.dungeon.lifecycle;

import com.ultimatedungeon.dungeon.instance.DungeonInstance;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomGraph;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Scores a dungeon run and grades it S/A/B/C/D, Hypixel-style.
 *
 * <p>Deaths and secrets are recorded live while the run is in progress; the
 * final grade blends completion speed (against a par time that scales with the
 * dungeon's size), deaths and secrets found. The score summary is shown on the
 * victory screen and an S or A grade earns a bonus reward roll.</p>
 */
public final class DungeonScoreService {

    /** Everything the end screen needs about one finished run. */
    public record RunScore(@NotNull String rank, int points, long durationMs,
                           int deaths, int secretsFound, int roomsCleared, int totalRooms) {

        /** mm:ss display of the run duration. */
        @NotNull
        public String formattedTime() {
            final long totalSeconds = durationMs / 1000;
            return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
        }
    }

    private static final class Counters {
        int deaths;
        int secrets;
    }

    /** Par allowance per room; runs at or under par lose no speed points. */
    private static final long PAR_MS_PER_ROOM = 45_000L;
    private static final int DEATH_PENALTY = 12;
    private static final int SECRET_BONUS = 5;
    /** Points lost per 30s over par. */
    private static final int OVERTIME_PENALTY_PER_30S = 5;

    private final Map<UUID, Counters> counters = new ConcurrentHashMap<>();

    public void recordDeath(@NotNull final UUID instanceId) {
        counters.computeIfAbsent(instanceId, k -> new Counters()).deaths++;
    }

    public void recordSecret(@NotNull final UUID instanceId) {
        counters.computeIfAbsent(instanceId, k -> new Counters()).secrets++;
    }

    /** Computes the final grade for a completed run and drops the live counters. */
    @NotNull
    public RunScore finish(@NotNull final DungeonInstance instance) {
        final Counters c = counters.remove(instance.getInstanceId());
        final int deaths = c != null ? c.deaths : 0;
        final int secrets = c != null ? c.secrets : 0;
        final long duration = instance.getContext().getElapsedMs();

        int totalRooms = 0;
        int cleared = 0;
        final RoomGraph graph = instance.getRoomGraph();
        if (graph != null) {
            totalRooms = graph.getRoomCount();
            for (final RoomData room : graph.getRooms()) {
                if (room.isCleared()) cleared++;
            }
        }

        final long parMs = Math.max(1, totalRooms) * PAR_MS_PER_ROOM;
        final long overtimeMs = Math.max(0, duration - parMs);
        int points = 100
                - deaths * DEATH_PENALTY
                - (int) (overtimeMs / 30_000L) * OVERTIME_PENALTY_PER_30S
                + secrets * SECRET_BONUS;
        points = Math.max(0, Math.min(110, points));

        final String rank;
        if (points >= 95)      rank = "S";
        else if (points >= 80) rank = "A";
        else if (points >= 60) rank = "B";
        else if (points >= 40) rank = "C";
        else                   rank = "D";

        return new RunScore(rank, points, duration, deaths, secrets, cleared, totalRooms);
    }

    /** Drops live counters for an instance that failed or was torn down. */
    public void clear(@NotNull final UUID instanceId) {
        counters.remove(instanceId);
    }
}
