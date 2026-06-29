package com.ultimatedungeon.config.files;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

/** Typed wrapper for {@code database.yml}. */
public final class DatabaseConfig {

    public enum Engine { SQLITE, MYSQL }

    private final Engine engine;
    private final String sqliteFile;
    private final String mysqlHost;
    private final int    mysqlPort;
    private final String mysqlDatabase;
    private final String mysqlUsername;
    private final String mysqlPassword;
    private final int    poolMaxSize;
    private final int    poolMinIdle;
    private final long   poolConnectionTimeout;
    private final long   poolIdleTimeout;
    private final long   poolMaxLifetime;

    public DatabaseConfig(@NotNull final FileConfiguration cfg) {
        final String typeStr = cfg.getString("type", "sqlite");
        this.engine          = "mysql".equalsIgnoreCase(typeStr) ? Engine.MYSQL : Engine.SQLITE;
        this.sqliteFile      = cfg.getString("sqlite.file", "ultimatedungeon.db");
        this.mysqlHost       = cfg.getString("mysql.host", "localhost");
        this.mysqlPort       = cfg.getInt("mysql.port", 3306);
        this.mysqlDatabase   = cfg.getString("mysql.database", "ultimatedungeon");
        this.mysqlUsername   = cfg.getString("mysql.username", "root");
        this.mysqlPassword   = cfg.getString("mysql.password", "");
        this.poolMaxSize          = cfg.getInt("mysql.pool.maximum-pool-size", 10);
        this.poolMinIdle          = cfg.getInt("mysql.pool.minimum-idle", 2);
        this.poolConnectionTimeout= cfg.getLong("mysql.pool.connection-timeout", 30_000L);
        this.poolIdleTimeout      = cfg.getLong("mysql.pool.idle-timeout", 600_000L);
        this.poolMaxLifetime      = cfg.getLong("mysql.pool.max-lifetime", 1_800_000L);
    }

    @NotNull public Engine getEngine()               { return engine; }
    public boolean         isSqlite()                { return engine == Engine.SQLITE; }
    public boolean         isMysql()                 { return engine == Engine.MYSQL; }
    @NotNull public String getSqliteFile()           { return sqliteFile; }
    @NotNull public String getMysqlHost()            { return mysqlHost; }
    public int             getMysqlPort()            { return mysqlPort; }
    @NotNull public String getMysqlDatabase()        { return mysqlDatabase; }
    @NotNull public String getMysqlUsername()        { return mysqlUsername; }
    @NotNull public String getMysqlPassword()        { return mysqlPassword; }
    public int             getPoolMaxSize()          { return poolMaxSize; }
    public int             getPoolMinIdle()          { return poolMinIdle; }
    public long            getPoolConnectionTimeout(){ return poolConnectionTimeout; }
    public long            getPoolIdleTimeout()      { return poolIdleTimeout; }
    public long            getPoolMaxLifetime()      { return poolMaxLifetime; }

    /** Returns the JDBC URL for the active engine. */
    @NotNull
    public String buildJdbcUrl(@NotNull final java.io.File dataFolder) {
        if (engine == Engine.SQLITE) {
            return "jdbc:sqlite:" + new java.io.File(dataFolder, sqliteFile).getAbsolutePath();
        }
        return "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase
               + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }
}
