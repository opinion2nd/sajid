package com.ultimatedungeon.database.impl.sqlite;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.database.DatabaseConnectionFactory;
import com.ultimatedungeon.database.dao.IPlayerStatsDao;
import com.ultimatedungeon.database.impl.AbstractDao;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.Set;
import java.util.UUID;

/** SQLite implementation of {@link IPlayerStatsDao}. */
public class SqlitePlayerStatsDao extends AbstractDao implements IPlayerStatsDao {

    private static final Set<String> ALLOWED_INCREMENT_COLS = Set.of(
        "dungeons_completed", "bosses_defeated", "monsters_killed",
        "death_count", "rewards_earned", "traps_triggered",
        "puzzles_solved", "secrets_found", "waves_completed"
    );

    private static final Set<String> ALLOWED_TOP_COLS = Set.of(
        "dungeons_completed", "bosses_defeated", "monsters_killed",
        "secrets_found", "fastest_run_ms"
    );

    public SqlitePlayerStatsDao(
            @NotNull final DatabaseConnectionFactory factory,
            @NotNull final PluginLogger              logger
    ) {
        super(factory, logger);
    }

    @Override
    @Nullable
    public PlayerStats findByUuid(@NotNull final UUID uuid) throws SQLException {
        final String sql = """
            SELECT uuid, player_name, dungeons_completed, bosses_defeated, monsters_killed,
                   death_count, fastest_run_ms, highest_difficulty, rewards_earned,
                   COALESCE(traps_triggered,0), COALESCE(puzzles_solved,0),
                   COALESCE(secrets_found,0), COALESCE(waves_completed,0)
            FROM ud_player_stats WHERE uuid = ?
            """;
        try (final Connection conn = connection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (final ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapRow(rs);
            }
        }
    }

    @Override
    public void insert(@NotNull final UUID uuid, @NotNull final String playerName) throws SQLException {
        final String sql = """
            INSERT OR IGNORE INTO ud_player_stats
            (uuid, player_name, first_seen, last_seen)
            VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """;
        try (final Connection conn = connection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, playerName);
            ps.executeUpdate();
        }
    }

    @Override
    public void increment(
            @NotNull final UUID   uuid,
            @NotNull final String column,
            final int             amount
    ) throws SQLException {
        validateColumn(column, ALLOWED_INCREMENT_COLS);
        // Column is validated against whitelist — safe to embed directly.
        final String sql = "UPDATE ud_player_stats SET " + column
                + " = " + column + " + ?, last_seen = CURRENT_TIMESTAMP WHERE uuid = ?";
        try (final Connection conn = connection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, amount);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    @Override
    public void updateFastestRun(@NotNull final UUID uuid, final long durationMs) throws SQLException {
        final String sql = """
            UPDATE ud_player_stats
            SET fastest_run_ms = ?,
                last_seen      = CURRENT_TIMESTAMP
            WHERE uuid = ?
              AND (fastest_run_ms IS NULL OR fastest_run_ms = 0 OR fastest_run_ms > ?)
            """;
        try (final Connection conn = connection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, durationMs);
            ps.setString(2, uuid.toString());
            ps.setLong(3, durationMs);
            ps.executeUpdate();
        }
    }

    @Override
    public void updatePlayerName(@NotNull final UUID uuid, @NotNull final String playerName) throws SQLException {
        final String sql = """
            UPDATE ud_player_stats
            SET player_name = ?, last_seen = CURRENT_TIMESTAMP
            WHERE uuid = ?
            """;
        try (final Connection conn = connection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerName);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    @Override
    public void updateHighestDifficulty(@NotNull final UUID uuid, @NotNull final String difficultyId) throws SQLException {
        // Simple overwrite — ordering logic is handled in StatisticsService.
        final String sql = """
            UPDATE ud_player_stats
            SET highest_difficulty = ?, last_seen = CURRENT_TIMESTAMP
            WHERE uuid = ?
            """;
        try (final Connection conn = connection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, difficultyId);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    @Override
    @NotNull
    public java.util.List<TopEntry> topPlayers(@NotNull final String column, final int limit)
            throws SQLException {
        if (!ALLOWED_TOP_COLS.contains(column)) {
            throw new IllegalArgumentException("Illegal leaderboard column: " + column);
        }
        // The column name is interpolated but strictly whitelisted above, so no
        // injection is possible; values still go through prepared parameters.
        final boolean ascending = "fastest_run_ms".equals(column);
        final String sql = "SELECT player_name, COALESCE(" + column + ", 0) AS v FROM ud_player_stats "
                + (ascending ? "WHERE fastest_run_ms > 0 ORDER BY v ASC" : "ORDER BY v DESC")
                + " LIMIT ?";
        final java.util.List<TopEntry> result = new java.util.ArrayList<>();
        try (final Connection conn = connection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, limit));
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(new TopEntry(rs.getString(1), rs.getLong(2)));
            }
        }
        return result;
    }

    // ── Row mapper ────────────────────────────────────────────────────────────

    @NotNull
    private PlayerStats mapRow(@NotNull final ResultSet rs) throws SQLException {
        return new PlayerStats(
            UUID.fromString(rs.getString(1)),
            rs.getString(2),
            rs.getInt(3),
            rs.getInt(4),
            rs.getInt(5),
            rs.getInt(6),
            rs.getLong(7),
            rs.getString(8),
            rs.getInt(9),
            rs.getInt(10),
            rs.getInt(11),
            rs.getInt(12),
            rs.getInt(13)
        );
    }
}
