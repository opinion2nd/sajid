package com.ultimatedungeon.database;

import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

/** Applies SQL schema migration scripts in order on startup. */
public final class MigrationRunner {

    private final PluginLogger logger;

    public MigrationRunner(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    public void run() {
        // Phase 1: discover and apply all V*.sql files from database/schema/.
        logger.debug("MigrationRunner.run() — pending implementation.");
    }
}
