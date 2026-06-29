package com.ultimatedungeon.database.impl.sqlite;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.database.DatabaseConnectionFactory;
import com.ultimatedungeon.database.dao.IRewardDao;
import com.ultimatedungeon.database.impl.AbstractDao;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.UUID;

/** SQLite implementation of {@link IRewardDao}. */
public final class SqliteRewardDao extends AbstractDao implements IRewardDao {

    public SqliteRewardDao(
            @NotNull final DatabaseConnectionFactory factory,
            @NotNull final PluginLogger              logger
    ) {
        super(factory, logger);
    }

    @Override
    public long insert(
            @NotNull final UUID   playerUuid,
            @NotNull final String rewardType,
            @NotNull final String rewardEvent,
            final long            dungeonId
    ) throws SQLException {
        final String sql = """
            INSERT INTO ud_reward_log
            (player_uuid, reward_type, reward_event, dungeon_id, created_at)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;
        try (final Connection conn = connection();
             final PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, playerUuid.toString());
            ps.setString(2, rewardType);
            ps.setString(3, rewardEvent);
            if (dungeonId > 0) ps.setLong(4, dungeonId); else ps.setNull(4, Types.BIGINT);
            ps.executeUpdate();
            try (final ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        }
        throw new SQLException("Insert into ud_reward_log returned no generated key.");
    }

    @Override
    public void markCollected(final long rewardId) throws SQLException {
        final String sql = "UPDATE ud_reward_log SET collected = 1 WHERE id = ?";
        try (final Connection conn = connection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, rewardId);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean isCollected(final long rewardId) throws SQLException {
        final String sql = "SELECT collected FROM ud_reward_log WHERE id = ?";
        try (final Connection conn = connection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, rewardId);
            try (final ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }
}
