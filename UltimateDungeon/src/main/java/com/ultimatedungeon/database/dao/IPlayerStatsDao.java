package com.ultimatedungeon.database.dao;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Data access interface for {@code ud_player_stats} operations.
 * All implementations must use prepared statements — never string concatenation.
 */
public interface IPlayerStatsDao {

    /**
     * Returns the stats row for {@code uuid}, or {@code null} if no row exists.
     *
     * @param uuid player UUID
     * @return loaded stats, or {@code null}
     * @throws SQLException on query failure
     */
    @Nullable PlayerStats findByUuid(@NotNull UUID uuid) throws SQLException;

    /**
     * Inserts a new row for a player that has never played before.
     *
     * @param uuid       player UUID
     * @param playerName current display name
     * @throws SQLException on insert failure
     */
    void insert(@NotNull UUID uuid, @NotNull String playerName) throws SQLException;

    /**
     * Atomically increments one named counter column by {@code amount}.
     *
     * @param uuid   player UUID
     * @param column one of: dungeons_completed, bosses_defeated, monsters_killed,
     *               death_count, rewards_earned, traps_triggered, puzzles_solved,
     *               secrets_found, waves_completed
     * @param amount positive increment value
     * @throws SQLException on update failure
     * @throws IllegalArgumentException if {@code column} is not an allowed column name
     */
    void increment(@NotNull UUID uuid, @NotNull String column, int amount) throws SQLException;

    /**
     * Updates the fastest-run record if {@code durationMs} is faster than the stored value.
     *
     * @param uuid       player UUID
     * @param durationMs run duration in milliseconds
     * @throws SQLException on update failure
     */
    void updateFastestRun(@NotNull UUID uuid, long durationMs) throws SQLException;

    /**
     * Ensures the player name column reflects the current name.
     *
     * @param uuid       player UUID
     * @param playerName current display name
     * @throws SQLException on update failure
     */
    void updatePlayerName(@NotNull UUID uuid, @NotNull String playerName) throws SQLException;

    /**
     * Updates the highest difficulty if {@code difficultyId} ranks higher than
     * the stored value. Ordering is determined by the difficulty config sort order.
     *
     * @param uuid         player UUID
     * @param difficultyId difficulty preset identifier
     * @throws SQLException on update failure
     */
    void updateHighestDifficulty(@NotNull UUID uuid, @NotNull String difficultyId) throws SQLException;

    // ── Stats record ──────────────────────────────────────────────────────────

    /**
     * Immutable snapshot of a player's dungeon statistics row.
     */
    record PlayerStats(
        @NotNull UUID   uuid,
        @NotNull String playerName,
        int             dungeonsCompleted,
        int             bossesDefeated,
        int             monstersKilled,
        int             deathCount,
        long            fastestRunMs,          // 0 = no run recorded
        @Nullable String highestDifficulty,
        int             rewardsEarned,
        int             trapsTriggered,
        int             puzzlesSolved,
        int             secretsFound,
        int             wavesCompleted
    ) {}
}
