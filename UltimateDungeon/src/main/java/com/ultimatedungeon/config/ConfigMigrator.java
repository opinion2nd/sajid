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

        // ── v1 → v2: five-level system and the new boss roster ───────────────
        if (version < 2) {
            changed |= migrateV1ToV2(fileName, config);
            version = 2;
        }

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

    /**
     * Migration to version 2: the four legacy difficulty presets become five
     * levels (which also control map size), and the legacy boss_1..boss_8
     * roster is replaced by the seven named bosses. Legacy sections are
     * removed so the new bundled defaults can be merged in on load; everything
     * else a server owner customised is left untouched.
     */
    private boolean migrateV1ToV2(
            @NotNull final String            fileName,
            @NotNull final FileConfiguration config
    ) {
        boolean changed = false;

        if (fileName.equals("difficulty.yml")) {
            for (final String legacy : new String[]{"easy", "normal", "hard", "nightmare"}) {
                final String path = "difficulties." + legacy;
                if (config.isSet(path)) {
                    config.set(path, null);
                    changed = true;
                    logger.debug(fileName + ": removed legacy preset '" + legacy + "'.");
                }
            }
        }

        if (fileName.equals("bosses.yml") || fileName.equals("loot.yml")) {
            for (int i = 1; i <= 8; i++) {
                final String bossPath = "bosses.boss_" + i;
                final String lootPath = "loot-tables.boss_" + i + "_loot";
                if (config.isSet(bossPath)) {
                    config.set(bossPath, null);
                    changed = true;
                }
                if (config.isSet(lootPath)) {
                    config.set(lootPath, null);
                    changed = true;
                }
            }
            if (changed) logger.debug(fileName + ": removed legacy boss entries.");
        }

        if (fileName.equals("themes.yml") && config.isSet("themes")) {
            changed |= replaceBossPool(config, "ancient_ruins",       java.util.List.of("tharok", "sylvara"));
            changed |= replaceBossPool(config, "frozen_cavern",       java.util.List.of("boreas", "aethon"));
            changed |= replaceBossPool(config, "corrupted_temple",    java.util.List.of("zharok"));
            changed |= replaceBossPool(config, "volcanic_fortress",   java.util.List.of("vulkhan"));
            changed |= replaceBossPool(config, "forgotten_catacombs", java.util.List.of("nyxara"));
        }

        return changed;
    }

    /** Replaces a theme's boss pool if it still references the legacy roster. */
    private boolean replaceBossPool(
            @NotNull final FileConfiguration config,
            @NotNull final String            themeId,
            @NotNull final java.util.List<String> newPool
    ) {
        final String path = "themes." + themeId + ".boss-pool";
        final java.util.List<String> current = config.getStringList(path);
        if (current.isEmpty() || current.stream().noneMatch(id -> id.startsWith("boss_"))) {
            return false;
        }
        config.set(path, newPool);
        return true;
    }
}
