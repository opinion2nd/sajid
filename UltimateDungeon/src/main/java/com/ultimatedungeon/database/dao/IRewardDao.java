package com.ultimatedungeon.database.dao;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Data access interface for {@code ud_reward_log}.
 * Used for duplication prevention — every reward collection is recorded.
 */
public interface IRewardDao {

    /**
     * Inserts a new uncollected reward log entry.
     *
     * @param playerUuid  owning player
     * @param rewardType  reward type string (matches {@code RewardType} enum name)
     * @param rewardEvent event that triggered the reward (matches {@code RewardEvent} enum name)
     * @param dungeonId   owning dungeon record ID, or -1 if not applicable
     * @return generated primary key
     * @throws SQLException on insert failure
     */
    long insert(
        @NotNull UUID   playerUuid,
        @NotNull String rewardType,
        @NotNull String rewardEvent,
        long            dungeonId
    ) throws SQLException;

    /**
     * Marks a reward as collected.
     *
     * @param rewardId primary key of the reward log entry
     * @throws SQLException on update failure
     */
    void markCollected(long rewardId) throws SQLException;

    /**
     * Returns {@code true} if the reward has already been collected.
     * Used as a server-side duplication guard before delivering items.
     *
     * @param rewardId primary key to check
     * @throws SQLException on query failure
     */
    boolean isCollected(long rewardId) throws SQLException;
}
