package com.ultimatedungeon.database.impl.mysql;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.database.DatabaseConnectionFactory;
import com.ultimatedungeon.database.impl.sqlite.SqliteDungeonRecordDao;
import org.jetbrains.annotations.NotNull;

/**
 * MySQL implementation of {@link com.ultimatedungeon.database.dao.IDungeonRecordDao}.
 *
 * <p>The ANSI SQL used for these tables is compatible with both SQLite and MySQL.
 * This class extends the SQLite implementation and overrides nothing — the sole
 * purpose is the separate class name so the engine-selection logic in
 * {@link com.ultimatedungeon.database.DatabaseManager} remains clean.</p>
 *
 * <p>Any MySQL-specific optimisations (batch inserts, ON DUPLICATE KEY, etc.)
 * will be added here in future milestones when high-throughput paths are profiled.</p>
 */
public final class MysqlDungeonRecordDao extends SqliteDungeonRecordDao {

    public MysqlDungeonRecordDao(
            @NotNull final DatabaseConnectionFactory factory,
            @NotNull final PluginLogger              logger
    ) {
        super(factory, logger);
    }
}
