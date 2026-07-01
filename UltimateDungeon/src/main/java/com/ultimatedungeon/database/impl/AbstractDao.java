package com.ultimatedungeon.database.impl;

import com.ultimatedungeon.database.DatabaseConnectionFactory;
import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

/**
 * Base class shared by all DAO implementations.
 *
 * <p>Provides connection access from the factory and a whitelist-based
 * column name validator so increment operations cannot be exploited via
 * injected column names.</p>
 */
public abstract class AbstractDao {

    protected final DatabaseConnectionFactory factory;
    protected final PluginLogger              logger;

    protected AbstractDao(
            @NotNull final DatabaseConnectionFactory factory,
            @NotNull final PluginLogger              logger
    ) {
        this.factory = factory;
        this.logger  = logger;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Returns a connection from the pool. Caller must close it. */
    @NotNull
    protected Connection connection() throws SQLException {
        return factory.getConnection();
    }

    /**
     * Validates that {@code column} is in the allowed set.
     * Prevents SQL injection via column name parameters.
     *
     * @param column      the column name to validate
     * @param allowedCols whitelist of permitted column names
     * @throws IllegalArgumentException if {@code column} is not in the whitelist
     */
    protected void validateColumn(
            @NotNull final String      column,
            @NotNull final Set<String> allowedCols
    ) {
        if (!allowedCols.contains(column)) {
            throw new IllegalArgumentException(
                "Column '" + column + "' is not in the permitted set: " + allowedCols
            );
        }
    }
}
