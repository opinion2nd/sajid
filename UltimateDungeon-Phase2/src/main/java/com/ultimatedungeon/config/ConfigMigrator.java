package com.ultimatedungeon.config;

import com.ultimatedungeon.core.PluginLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Applies safe in-place config migrations when the stored {@code config-version}
 * is older than {@link ConfigVersion#CURRENT}.
 *
 * <p>Each migration step is a small, targeted method that adds new keys or
 * renames old ones. Values a server owner has intentionally customised are
 * always preserved — migration never overwrites existing values.</p>
 *
 * <p>After migration the caller must save the file back to disk.</p>
 */
public final class ConfigMigrator {

    private final PluginLogger logger;

    public ConfigMigrator(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    /**
     * Migrates {@code config} from {@code fromVersion} up to
     * {@link ConfigVersion#CURRENT} one step at a time.
     *
     * @param fileName    display name of the file being migrated (for logging)
     * @param config      the loaded but not-yet-saved {@link FileConfiguration}
     * @param fromVersion the version stored in the file
     * @return {@code true} if any migration step was applied (file needs saving)
     */
    public boolean migrate(
            @NotNull final String            fileName,
            @NotNull final FileConfiguration config,
            final int                        fromVersion
    ) {
        if (fromVersion >= ConfigVersion.CURRENT) {
            return false;
        }

        boolean changed = false;
        int     version = fromVersion;

        logger.info("Migrating " + fileName + " from v" + fromVersion
                + " to v" + ConfigVersion.CURRENT + "...");

        // ── v0 → v1 ──────────────────────────────────────────────────────────
        if (version < 1) {
            changed |= migrateV0ToV1(fileName, config);
            version = 1;
        }

        // Future: add v1 → v2 here when the schema changes.

        if (changed) {
            config.set("config-version", ConfigVersion.CURRENT);
            logger.info(fileName + " migrated successfully to v" + ConfigVersion.CURRENT + ".");
        }
        return changed;
    }

    // ── Migration steps ───────────────────────────────────────────────────────

    /**
     * Migration from config-version 0 (pre-release) to version 1 (first stable).
     *
     * <p>This step is a no-op for configs that were already generated at v1 —
     * all keys exist. It handles hand-crafted configs that omit {@code config-version}
     * entirely by setting it.</p>
     */
    private boolean migrateV0ToV1(
            @NotNull final String            fileName,
            @NotNull final FileConfiguration config
    ) {
        boolean changed = false;

        // Ensure config-version key exists.
        if (!config.isSet("config-version")) {
            config.set("config-version", 1);
            changed = true;
            logger.debug(fileName + ": added missing config-version key.");
        }

        return changed;
    }
}
