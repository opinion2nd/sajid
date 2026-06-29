package com.ultimatedungeon.database;

import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

/**
 * Manages database initialisation, connection provisioning, and shutdown.
 *
 * <p>Supports SQLite (default, zero-config) and MySQL (production, HikariCP pool).
 * The active engine is determined by {@code database.yml → type}.</p>
 *
 * <p>All queries use prepared statements — string concatenation is forbidden.</p>
 */
public final class DatabaseManager {

    private final PluginLogger logger;

    public DatabaseManager(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    public void initialise() {
        // Phase 1: connect, run MigrationRunner, verify tables exist.
        logger.debug("DatabaseManager.initialise() — pending implementation.");
    }

    public void shutdown() {
        // Phase 1: flush pending operations, close connection pool.
        logger.debug("DatabaseManager.shutdown() — pending implementation.");
    }
}
