package com.ultimatedungeon.database.impl.sqlite;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.database.DatabaseConnectionFactory;
import com.ultimatedungeon.database.dao.IDungeonRecordDao;
import com.ultimatedungeon.database.impl.AbstractDao;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** SQLite implementation of {@link IDungeonRecordDao}. */
public final class SqliteDungeonRecordDao extends AbstractDao implements IDungeonRecordDao {

    public SqliteDungeonRecordDao(
            @NotNull final DatabaseConnectionFactory factory,
            @NotNull final PluginLogger              logger
    ) {
        super(factory, logger);
    }

    @Override
    public long insert(
            @NotNull final UUID   playerUuid,
            @NotNull final String theme,
            @NotNull final String difficulty,
            final int             partySize
    ) throws SQLException {
        final String sql = """
            INSERT INTO ud_dungeon_records
            (player_uuid, theme, difficulty, party_size, completed_at)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;
        try (final Connection conn = connection();
             final PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, playerUuid.toString());
            ps.setString(2, theme);
            ps.setString(3, difficulty);
            ps.setInt(4, partySize);
            ps.executeUpdate();
            try (final ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        }
        throw new SQLException("Insert into ud_dungeon_records returned no generated key.");
    }

    @Override
    public void markCompleted(
            final long             id,
            final long             durationMs,
            @Nullable final String bossKilled
    ) throws SQLException {
        final String sql = """
            UPDATE ud_dungeon_records
            SET completed = 1, duration_ms = ?, boss_killed = ?
            WHERE id = ?
            """;
        try (final Connection conn = connection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, durationMs);
            ps.setString(2, bossKilled);
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    @Override
    @NotNull
    public List<DungeonRecord> findRecentByPlayer(
            @NotNull final UUID uuid,
            final int           limit
    ) throws SQLException {
        final String sql = """
            SELECT id, player_uuid, theme, difficulty, completed, duration_ms,
                   party_size, boss_killed
            FROM ud_dungeon_records
            WHERE player_uuid = ?
            ORDER BY completed_at DESC
            LIMIT ?
            """;
        final List<DungeonRecord> results = new ArrayList<>();
        try (final Connection conn = connection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, limit);
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
        }
        return results;
    }

    @Override
    @Nullable
    public DungeonRecord findFastest(
            @NotNull final UUID   playerUuid,
            @NotNull final String theme,
            @NotNull final String difficulty
    ) throws SQLException {
        final String sql = """
            SELECT id, player_uuid, theme, difficulty, completed, duration_ms,
                   party_size, boss_killed
            FROM ud_dungeon_records
            WHERE player_uuid = ? AND theme = ? AND difficulty = ? AND completed = 1
            ORDER BY duration_ms ASC
            LIMIT 1
            """;
        try (final Connection conn = connection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setString(2, theme);
            ps.setString(3, difficulty);
            try (final ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @NotNull
    private DungeonRecord mapRow(@NotNull final ResultSet rs) throws SQLException {
        return new DungeonRecord(
            rs.getLong(1),
            UUID.fromString(rs.getString(2)),
            rs.getString(3),
            rs.getString(4),
            rs.getBoolean(5),
            rs.getLong(6),
            rs.getInt(7),
            rs.getString(8)
        );
    }
}
