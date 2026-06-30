package com.ultimatedungeon.database;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.database.schema.SchemaResources;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Applies SQL migration scripts in order on startup.
 *
 * <h3>Convention</h3>
 * Scripts live at {@code resources/database/schema/V{n}__{description}.sql}
 * where {@code n} is an integer version number. Scripts are applied in
 * ascending {@code n} order. Already-applied versions are tracked in the
 * {@code ud_schema_version} table which this class creates automatically.
 *
 * <h3>SQLite compatibility</h3>
 * SQLite does not support {@code ALTER TABLE ... ADD COLUMN IF NOT EXISTS}.
 * The V2 script uses this syntax for MySQL but is adapted for SQLite by
 * catching {@code duplicate column} errors silently.
 *
 * <h3>Atomicity</h3>
 * Each script is executed inside a single transaction. If any statement fails
 * the transaction is rolled back, the version is not recorded, and the plugin
 * fails to start — preventing a partially-applied schema.
 */
public final class MigrationRunner {

    private static final String VERSION_TABLE = "ud_schema_version";
    private static final Pattern VERSION_PATTERN =
            Pattern.compile("V(\\d+)__.*\\.sql", Pattern.CASE_INSENSITIVE);

    /** Scripts available in the jar, ordered by version number. */
    private static final List<String> SCRIPT_NAMES = List.of(
            "V1__initial_schema.sql",
            "V2__add_statistics.sql"
    );

    private final DatabaseConnectionFactory factory;
    private final PluginLogger              logger;

    public MigrationRunner(
            @NotNull final DatabaseConnectionFactory factory,
            @NotNull final PluginLogger              logger
    ) {
        this.factory = factory;
        this.logger  = logger;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Ensures the schema-version tracking table exists, then applies any
     * migration scripts whose version is higher than the current schema version.
     *
     * @throws SQLException if a migration fails (plugin startup will abort)
     */
    public void run() throws SQLException {
        try (final Connection conn = factory.getConnection()) {
            ensureVersionTableExists(conn);
            final int currentVersion = readCurrentVersion(conn);
            logger.debug("Current schema version: " + currentVersion);

            final List<MigrationScript> pending = collectPending(currentVersion);
            if (pending.isEmpty()) {
                logger.debug("Schema is up to date. No migrations needed.");
                return;
            }

            logger.info("Applying " + pending.size() + " schema migration(s)...");
            for (final MigrationScript script : pending) {
                applyScript(conn, script);
            }
            logger.info("Schema migrations complete. Current version: "
                    + pending.get(pending.size() - 1).version());
        }
    }

    // ── Private — version tracking ────────────────────────────────────────────

    private void ensureVersionTableExists(@NotNull final Connection conn) throws SQLException {
        final String ddl = """
                CREATE TABLE IF NOT EXISTS %s (
                    version     INT           NOT NULL PRIMARY KEY,
                    script_name VARCHAR(255)  NOT NULL,
                    applied_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """.formatted(VERSION_TABLE);
        try (final Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
        }
        logger.debug("Schema version table ensured.");
    }

    private int readCurrentVersion(@NotNull final Connection conn) throws SQLException {
        final String sql = "SELECT MAX(version) FROM " + VERSION_TABLE;
        try (final Statement stmt = conn.createStatement();
             final ResultSet rs   = stmt.executeQuery(sql)) {
            if (rs.next()) {
                final int v = rs.getInt(1);
                return rs.wasNull() ? 0 : v;
            }
        }
        return 0;
    }

    private void recordVersion(
            @NotNull final Connection conn,
            final int                 version,
            @NotNull final String     scriptName
    ) throws SQLException {
        final String sql = "INSERT INTO " + VERSION_TABLE
                + " (version, script_name) VALUES (?, ?)";
        try (final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, version);
            ps.setString(2, scriptName);
            ps.executeUpdate();
        }
    }

    // ── Private — script discovery ────────────────────────────────────────────

    @NotNull
    private List<MigrationScript> collectPending(final int currentVersion) {
        final List<MigrationScript> pending = new ArrayList<>();
        for (final String name : SCRIPT_NAMES) {
            final int version = parseVersion(name);
            if (version > currentVersion) {
                pending.add(new MigrationScript(version, name));
            }
        }
        pending.sort(Comparator.comparingInt(MigrationScript::version));
        return pending;
    }

    private int parseVersion(@NotNull final String scriptName) {
        final Matcher m = VERSION_PATTERN.matcher(scriptName);
        if (m.matches()) {
            return Integer.parseInt(m.group(1));
        }
        throw new IllegalArgumentException(
            "Cannot parse version from script name: " + scriptName
            + ". Expected format: V{n}__{description}.sql"
        );
    }

    // ── Private — script execution ────────────────────────────────────────────

    private void applyScript(
            @NotNull final Connection        conn,
            @NotNull final MigrationScript   script
    ) throws SQLException {
        logger.info("Applying migration: " + script.name());
        final String sql = loadScript(script.name());

        final boolean autoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            executeSql(conn, sql);
            recordVersion(conn, script.version(), script.name());
            conn.commit();
            logger.info("Migration applied: " + script.name());
        } catch (final SQLException e) {
            conn.rollback();
            logger.severe("Migration failed: " + script.name()
                    + ". Schema rolled back.", e);
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    /**
     * Executes a multi-statement SQL script, splitting on {@code ;} boundaries.
     * Empty statements and SQL comments are skipped.
     */
    private void executeSql(
            @NotNull final Connection conn,
            @NotNull final String     sql
    ) throws SQLException {
        // Remove single-line comments (-- ...) before splitting.
        final String stripped = sql.replaceAll("--[^\n]*", "");
        final String[] statements = stripped.split(";");

        try (final Statement stmt = conn.createStatement()) {
            for (final String raw : statements) {
                final String trimmed = raw.strip();
                if (trimmed.isEmpty()) continue;
                try {
                    stmt.execute(trimmed);
                } catch (final SQLException e) {
                    // SQLite does not support IF NOT EXISTS on ADD COLUMN.
                    // Catch "duplicate column" errors from V2 gracefully.
                    if (isDuplicateColumnError(e)) {
                        logger.debug("Column already exists (SQLite compat skip): "
                                + e.getMessage());
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    private boolean isDuplicateColumnError(@NotNull final SQLException e) {
        final String msg = e.getMessage();
        return msg != null && (msg.contains("duplicate column name")
                || msg.contains("already exists")
                || e.getErrorCode() == 1060 /* MySQL duplicate column */);
    }

    @NotNull
    private String loadScript(@NotNull final String scriptName) {
        final String path = SchemaResources.SCHEMA_PATH + scriptName;
        final InputStream stream = MigrationRunner.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException(
                "Migration script not found in jar: " + path
            );
        }
        try (final BufferedReader reader =
                     new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (final IOException e) {
            throw new IllegalStateException(
                "Failed to read migration script: " + scriptName, e
            );
        }
    }

    // ── Value record ──────────────────────────────────────────────────────────

    private record MigrationScript(int version, @NotNull String name) {}
}
