package com.ultimatedungeon.database.dao;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Data access interface for {@code ud_party_log}.
 * Records party lifecycle events for analytics and debugging.
 */
public interface IPartyDao {

    /**
     * Inserts a new party log entry when a party is formed.
     *
     * @param leaderUuid   UUID of the party leader
     * @param memberUuids  all member UUIDs (including leader), serialised as JSON
     * @return generated primary key
     * @throws SQLException on insert failure
     */
    long insert(@NotNull UUID leaderUuid, @NotNull List<UUID> memberUuids) throws SQLException;

    /**
     * Associates a dungeon run record with this party log entry.
     *
     * @param partyLogId party log primary key
     * @param dungeonId  dungeon record primary key
     * @throws SQLException on update failure
     */
    void linkDungeon(long partyLogId, long dungeonId) throws SQLException;

    /**
     * Records the timestamp when the party disbanded.
     *
     * @param partyLogId party log primary key
     * @throws SQLException on update failure
     */
    void markDisbanded(long partyLogId) throws SQLException;
}
