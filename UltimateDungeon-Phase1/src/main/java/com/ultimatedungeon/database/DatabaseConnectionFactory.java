package com.ultimatedungeon.database;

import org.jetbrains.annotations.NotNull;
import java.sql.Connection;
import java.sql.SQLException;

/** Creates and provides database connections for the active engine. */
public final class DatabaseConnectionFactory {

    public DatabaseConnectionFactory() {}

    @NotNull
    public Connection getConnection() throws SQLException {
        throw new UnsupportedOperationException("DatabaseConnectionFactory — pending implementation.");
    }

    public void close() {
        // Release connection pool on shutdown.
    }
}
