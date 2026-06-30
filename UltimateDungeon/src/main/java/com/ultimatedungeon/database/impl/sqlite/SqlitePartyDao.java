package com.ultimatedungeon.database.impl.sqlite;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.database.DatabaseConnectionFactory;
import com.ultimatedungeon.database.dao.IPartyDao;
import com.ultimatedungeon.database.impl.AbstractDao;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** SQLite implementation of {@link IPartyDao}. */
public class SqlitePartyDao extends AbstractDao implements IPartyDao {

    public SqlitePartyDao(
            @NotNull final DatabaseConnectionFactory factory,
            @NotNull final PluginLogger              logger
    ) {
        super(factory, logger);
    }

    @Override
    public long insert(
            @NotNull final UUID       leaderUuid,
            @NotNull final List<UUID> memberUuids
    ) throws SQLException {
        final String membersJson = "[" + memberUuids.stream()
                .map(u -> "\"" + u + "\"")
                .collect(Collectors.joining(",")) + "]";
        final String sql = """
            INSERT INTO ud_party_log (leader_uuid, member_uuids, created_at)
            VALUES (?, ?, CURRENT_TIMESTAMP)
            """;
        try (final Connection conn = connection();
             final PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, leaderUuid.toString());
            ps.setString(2, membersJson);
            ps.executeUpdate();
            try (final ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        }
        throw new SQLException("Insert into ud_party_log returned no generated key.");
    }

    @Override
    public void linkDungeon(final long partyLogId, final long dungeonId) throws SQLException {
        final String sql = "UPDATE ud_party_log SET dungeon_id = ? WHERE id = ?";
        try (final Connection conn = connection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, dungeonId);
            ps.setLong(2, partyLogId);
            ps.executeUpdate();
        }
    }

    @Override
    public void markDisbanded(final long partyLogId) throws SQLException {
        final String sql = "UPDATE ud_party_log SET disbanded_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (final Connection conn = connection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, partyLogId);
            ps.executeUpdate();
        }
    }
}
