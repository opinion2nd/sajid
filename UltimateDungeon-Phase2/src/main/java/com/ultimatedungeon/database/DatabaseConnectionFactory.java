package com.ultimatedungeon.database;

import com.ultimatedungeon.config.files.DatabaseConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Creates and manages the database connection pool.
 *
 * <h3>SQLite</h3>
 * A minimal HikariCP pool (pool size = 1, as SQLite allows only one writer)
 * wrapping the SQLite JDBC driver. The database file is created inside the
 * plugin data folder automatically on first connection.
 *
 * <h3>MySQL</h3>
 * A full HikariCP pool configured with the settings from {@code database.yml}.
 * Connection health is verified on checkout; stale connections are recycled.
 *
 * <h3>Lifecycle</h3>
 * {@link #open()} must be called before any query. {@link #close()} is called
 * during plugin shutdown — it drains the pool and releases all JDBC resources.
 */
public final class DatabaseConnectionFactory {

    private final DatabaseConfig config;
    private final PluginLogger   logger;
    private final File           dataFolder;

    private HikariDataSource dataSource;

    public DatabaseConnectionFactory(
            @NotNull final DatabaseConfig config,
            @NotNull final PluginLogger   logger,
            @NotNull final File           dataFolder
    ) {
        this.config     = config;
        this.logger     = logger;
        this.dataFolder = dataFolder;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Opens the connection pool. Throws if the pool cannot be established.
     *
     * @throws RuntimeException if HikariCP fails to start
     */
    public void open() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.warning("DatabaseConnectionFactory.open() called but pool is already open.");
            return;
        }

        if (config.isSqlite()) {
            openSqlite();
        } else {
            openMysql();
        }

        logger.info("Database pool opened (" + config.getEngine().name() + ").");
    }

    /**
     * Closes the pool and releases all held JDBC resources.
     * Safe to call even if the pool was never opened.
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database pool closed.");
        }
    }

    // ── Connection vending ────────────────────────────────────────────────────

    /**
     * Returns a {@link Connection} from the pool.
     *
     * <p>Callers <strong>must</strong> close the connection in a finally block
     * (or try-with-resources) to return it to the pool.</p>
     *
     * @return an active database connection
     * @throws SQLException if a connection cannot be obtained
     * @throws IllegalStateException if the pool has not been opened
     */
    @NotNull
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new IllegalStateException(
                "Database pool is not open. Call open() before requesting connections."
            );
        }
        return dataSource.getConnection();
    }

    /** Returns {@code true} if the pool is open and healthy. */
    public boolean isOpen() {
        return dataSource != null && !dataSource.isClosed();
    }

    // ── Private — pool initialisation ─────────────────────────────────────────

    private void openSqlite() {
        final String jdbcUrl = config.buildJdbcUrl(dataFolder);
        logger.debug("SQLite JDBC URL: " + jdbcUrl);

        final HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(jdbcUrl);
        hc.setDriverClassName("org.sqlite.JDBC");

        // SQLite supports only one concurrent writer; a pool size > 1 causes
        // lock contention. Keep at 1 write connection + WAL mode for reads.
        hc.setMaximumPoolSize(1);
        hc.setMinimumIdle(1);
        hc.setConnectionTimeout(10_000L);
        hc.setIdleTimeout(0);           // never expire the single connection
        hc.setMaxLifetime(0);           // no max lifetime for SQLite
        hc.setPoolName("UltimateDungeon-SQLite");

        // Enable WAL journal mode for better concurrent read performance.
        hc.addDataSourceProperty("journal_mode", "WAL");
        hc.addDataSourceProperty("busy_timeout", "5000");
        hc.addDataSourceProperty("synchronous", "NORMAL");

        hc.setConnectionTestQuery("SELECT 1");

        dataSource = new HikariDataSource(hc);
    }

    private void openMysql() {
        final String jdbcUrl = config.buildJdbcUrl(dataFolder);
        logger.debug("MySQL JDBC URL: " + jdbcUrl);

        final HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(jdbcUrl);
        hc.setUsername(config.getMysqlUsername());
        hc.setPassword(config.getMysqlPassword());
        hc.setDriverClassName("com.mysql.cj.jdbc.Driver");

        hc.setMaximumPoolSize(config.getPoolMaxSize());
        hc.setMinimumIdle(config.getPoolMinIdle());
        hc.setConnectionTimeout(config.getPoolConnectionTimeout());
        hc.setIdleTimeout(config.getPoolIdleTimeout());
        hc.setMaxLifetime(config.getPoolMaxLifetime());
        hc.setPoolName("UltimateDungeon-MySQL");

        // Health check — fail fast on checkout rather than returning a broken connection.
        hc.setConnectionTestQuery("SELECT 1");
        hc.addDataSourceProperty("cachePrepStmts",          "true");
        hc.addDataSourceProperty("prepStmtCacheSize",       "250");
        hc.addDataSourceProperty("prepStmtCacheSqlLimit",   "2048");
        hc.addDataSourceProperty("useServerPrepStmts",      "true");
        hc.addDataSourceProperty("rewriteBatchedStatements","true");

        dataSource = new HikariDataSource(hc);
    }
}
