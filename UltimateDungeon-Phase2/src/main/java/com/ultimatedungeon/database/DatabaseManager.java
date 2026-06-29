package com.ultimatedungeon.database;

import com.ultimatedungeon.config.files.DatabaseConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.database.dao.*;
import com.ultimatedungeon.database.impl.mysql.*;
import com.ultimatedungeon.database.impl.sqlite.*;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Top-level database manager for UltimateDungeon.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Owns the {@link DatabaseConnectionFactory} (HikariCP pool).</li>
 *   <li>Runs {@link MigrationRunner} on startup to keep the schema current.</li>
 *   <li>Constructs and exposes the correct DAO implementations for the active
 *       engine (SQLite vs MySQL).</li>
 *   <li>Closes the pool cleanly on shutdown.</li>
 * </ul>
 *
 * <h3>Thread safety</h3>
 * {@link #initialise()} and {@link #shutdown()} are called from the main thread.
 * DAO methods are called from async tasks and may run concurrently — HikariCP
 * handles connection multiplexing.
 *
 * <h3>Prepared statements</h3>
 * All DAO implementations use prepared statements exclusively.
 * String concatenation in SQL is forbidden throughout the codebase.
 */
public final class DatabaseManager {

    private final DatabaseConfig             config;
    private final PluginLogger               logger;
    private final DatabaseConnectionFactory  connectionFactory;

    // ── DAO instances (one per table) ─────────────────────────────────────────
    private IPlayerStatsDao   playerStatsDao;
    private IDungeonRecordDao dungeonRecordDao;
    private IRewardDao        rewardDao;
    private IPartyDao         partyDao;

    public DatabaseManager(
            @NotNull final DatabaseConfig config,
            @NotNull final PluginLogger   logger,
            @NotNull final java.io.File   dataFolder
    ) {
        this.config            = config;
        this.logger            = logger;
        this.connectionFactory = new DatabaseConnectionFactory(config, logger, dataFolder);
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Opens the connection pool, runs schema migrations, and wires DAO instances.
     *
     * @throws RuntimeException if the pool cannot open or migrations fail
     */
    public void initialise() {
        logger.info("Initialising database (" + config.getEngine().name() + ")...");

        connectionFactory.open();

        runMigrations();
        wireDAOs();

        logger.info("Database initialised successfully.");
    }

    /**
     * Drains and closes the connection pool. Safe to call even if
     * {@link #initialise()} was never called.
     */
    public void shutdown() {
        connectionFactory.close();
        logger.info("Database shut down.");
    }

    // ── DAO accessors ─────────────────────────────────────────────────────────

    @NotNull
    public IPlayerStatsDao getPlayerStatsDao() {
        assertInitialised();
        return playerStatsDao;
    }

    @NotNull
    public IDungeonRecordDao getDungeonRecordDao() {
        assertInitialised();
        return dungeonRecordDao;
    }

    @NotNull
    public IRewardDao getRewardDao() {
        assertInitialised();
        return rewardDao;
    }

    @NotNull
    public IPartyDao getPartyDao() {
        assertInitialised();
        return partyDao;
    }

    // ── Connection pass-through ───────────────────────────────────────────────

    /**
     * Provides a raw JDBC connection for advanced use cases.
     * The caller is responsible for closing it (use try-with-resources).
     *
     * @return an active connection from the pool
     * @throws SQLException if a connection cannot be obtained
     */
    @NotNull
    public Connection getConnection() throws SQLException {
        return connectionFactory.getConnection();
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void runMigrations() {
        final MigrationRunner runner = new MigrationRunner(connectionFactory, logger);
        try {
            runner.run();
        } catch (final SQLException e) {
            throw new RuntimeException(
                "Database migration failed. Check the logs above and correct "
                + "the schema before restarting.", e
            );
        }
    }

    private void wireDAOs() {
        if (config.isSqlite()) {
            playerStatsDao   = new SqlitePlayerStatsDao(connectionFactory, logger);
            dungeonRecordDao = new SqliteDungeonRecordDao(connectionFactory, logger);
            rewardDao        = new SqliteRewardDao(connectionFactory, logger);
            partyDao         = new SqlitePartyDao(connectionFactory, logger);
        } else {
            playerStatsDao   = new MysqlPlayerStatsDao(connectionFactory, logger);
            dungeonRecordDao = new MysqlDungeonRecordDao(connectionFactory, logger);
            rewardDao        = new MysqlRewardDao(connectionFactory, logger);
            partyDao         = new MysqlPartyDao(connectionFactory, logger);
        }
        logger.debug("DAOs wired for engine: " + config.getEngine().name());
    }

    private void assertInitialised() {
        if (playerStatsDao == null) {
            throw new IllegalStateException(
                "DatabaseManager has not been initialised. Call initialise() first."
            );
        }
    }
}
