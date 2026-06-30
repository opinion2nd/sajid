package com.ultimatedungeon.database.dao;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Data access interface for {@code ud_dungeon_records}.
 */
public interface IDungeonRecordDao {

    /**
     * Inserts a new in-progress dungeon record.
     *
     * @param playerUuid  owning player
     * @param theme       theme identifier
     * @param difficulty  difficulty identifier
     * @param partySize   number of players in the run
     * @return the generated primary key for this record
     * @throws SQLException on insert failure
     */
    long insert(
        @NotNull UUID   playerUuid,
        @NotNull String theme,
        @NotNull String difficulty,
        int             partySize
    ) throws SQLException;

    /**
     * Marks a run as completed and records duration and boss killed.
     *
     * @param id          primary key returned by {@link #insert}
     * @param durationMs  elapsed milliseconds
     * @param bossKilled  identifier of the defeated boss, or {@code null}
     * @throws SQLException on update failure
     */
    void markCompleted(long id, long durationMs, @Nullable String bossKilled) throws SQLException;

    /**
     * Returns the {@code n} most recent records for the given player.
     *
     * @param playerUuid the player UUID
     * @param limit      maximum number of records to return
     * @throws SQLException on query failure
     */
    @NotNull List<DungeonRecord> findRecentByPlayer(@NotNull UUID playerUuid, int limit) throws SQLException;

    /**
     * Returns the fastest completed run for a specific theme and difficulty.
     *
     * @param playerUuid player UUID
     * @param theme      theme identifier
     * @param difficulty difficulty identifier
     * @return the fastest record, or {@code null} if no completed runs exist
     * @throws SQLException on query failure
     */
    @Nullable DungeonRecord findFastest(
        @NotNull UUID   playerUuid,
        @NotNull String theme,
        @NotNull String difficulty
    ) throws SQLException;

    /** Immutable snapshot of a dungeon record row. */
    record DungeonRecord(
        long    id,
        @NotNull UUID   playerUuid,
        @NotNull String theme,
        @NotNull String difficulty,
        boolean completed,
        long    durationMs,
        int     partySize,
        @Nullable String bossKilled
    ) {}
}
