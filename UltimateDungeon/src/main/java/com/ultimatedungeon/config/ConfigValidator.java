package com.ultimatedungeon.config;

import com.ultimatedungeon.config.files.*;
import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;

/**
 * Validates all loaded configuration values before the plugin proceeds with startup.
 *
 * <p>Validation is intentionally non-fatal for warnings and fatal only for values
 * that would cause crashes or broken gameplay. Each failed check is logged with
 * a clear explanation so server administrators can fix problems without reading
 * source code.</p>
 *
 * <p>Returns {@code false} if any <em>critical</em> validation fails — the
 * caller ({@link ConfigManager}) should abort startup in that case.</p>
 */
public final class ConfigValidator {

    private final PluginLogger logger;

    public ConfigValidator(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    /**
     * Runs all validation checks.
     *
     * @param main        loaded main config
     * @param database    loaded database config
     * @param performance loaded performance config
     * @param party       loaded party config
     * @param dungeon     loaded dungeon config
     * @param difficulty  loaded difficulty config
     * @return {@code true} if all critical checks pass; {@code false} to abort startup
     */
    public boolean validate(
            @NotNull final MainConfig        main,
            @NotNull final DatabaseConfig    database,
            @NotNull final PerformanceConfig performance,
            @NotNull final PartyConfig       party,
            @NotNull final DungeonConfig     dungeon,
            @NotNull final DifficultyConfig  difficulty
    ) {
        boolean critical = true;

        critical &= validateMain(main);
        critical &= validateDatabase(database);
        critical &= validatePerformance(performance);
        critical &= validateParty(party);
        critical &= validateDungeon(dungeon);
        critical &= validateDifficulty(difficulty);

        if (critical) {
            logger.debug("Config validation passed — all checks clean.");
        }
        return critical;
    }

    // ── Per-file validators ───────────────────────────────────────────────────

    private boolean validateMain(@NotNull final MainConfig cfg) {
        boolean ok = true;
        if (cfg.getInstancePrefix().isBlank()) {
            logger.severe("config.yml → instance-prefix must not be blank.");
            ok = false;
        }
        return ok;
    }

    private boolean validateDatabase(@NotNull final DatabaseConfig cfg) {
        boolean ok = true;
        if (cfg.isMysql()) {
            if (cfg.getMysqlHost().isBlank()) {
                logger.severe("database.yml → mysql.host must not be blank.");
                ok = false;
            }
            if (cfg.getMysqlDatabase().isBlank()) {
                logger.severe("database.yml → mysql.database must not be blank.");
                ok = false;
            }
            if (cfg.getPoolMaxSize() < 1) {
                logger.warning("database.yml → pool.maximum-pool-size is < 1; defaulting to 2.");
            }
        }
        if (cfg.isSqlite() && cfg.getSqliteFile().isBlank()) {
            logger.severe("database.yml → sqlite.file must not be blank.");
            ok = false;
        }
        return ok;
    }

    private boolean validatePerformance(@NotNull final PerformanceConfig cfg) {
        boolean ok = true;
        if (cfg.getMaxEntitiesPerDungeon() < 10) {
            logger.warning("performance.yml → max-entities-per-dungeon is very low ("
                    + cfg.getMaxEntitiesPerDungeon() + "). Gameplay may be impaired.");
        }
        if (cfg.getGenerationThreadCount() < 1) {
            logger.severe("performance.yml → generation-thread-count must be at least 1.");
            ok = false;
        }
        if (cfg.getDungeonHeartbeatTicks() < 1) {
            logger.warning("performance.yml → tick-intervals.dungeon-heartbeat must be ≥ 1. Using 20.");
        }
        return ok;
    }

    private boolean validateParty(@NotNull final PartyConfig cfg) {
        boolean ok = true;
        if (cfg.getMaxPartySize() < 1) {
            logger.severe("party.yml → max-party-size must be at least 1.");
            ok = false;
        }
        if (cfg.getMaxPartySize() > 20) {
            logger.warning("party.yml → max-party-size is very high ("
                    + cfg.getMaxPartySize() + "). Consider server capacity.");
        }
        if (cfg.getInviteTimeoutSeconds() < 5) {
            logger.warning("party.yml → invite-timeout is very short (" 
                    + cfg.getInviteTimeoutSeconds() + "s). Players may miss invitations.");
        }
        if (cfg.getReadyCheckDurationSeconds() < 5) {
            logger.warning("party.yml → ready-check-duration is very short ("
                    + cfg.getReadyCheckDurationSeconds() + "s).");
        }
        return ok;
    }

    private boolean validateDungeon(@NotNull final DungeonConfig cfg) {
        boolean ok = true;
        if (cfg.getDungeonSizeMin() < 5) {
            logger.warning("dungeon.yml → generation.dungeon-size.min is very low ("
                    + cfg.getDungeonSizeMin() + "). Dungeons may be trivially short.");
        }
        if (cfg.getDungeonSizeMin() > cfg.getDungeonSizeMax()) {
            logger.severe("dungeon.yml → dungeon-size.min (" + cfg.getDungeonSizeMin()
                    + ") must be ≤ dungeon-size.max (" + cfg.getDungeonSizeMax() + ").");
            ok = false;
        }
        if (cfg.getCorridorLengthMin() > cfg.getCorridorLengthMax()) {
            logger.severe("dungeon.yml → corridor-length.min must be ≤ corridor-length.max.");
            ok = false;
        }
        if (cfg.getMaxConcurrentInstances() < 1) {
            logger.severe("dungeon.yml → max-concurrent-instances must be at least 1.");
            ok = false;
        }
        return ok;
    }

    private boolean validateDifficulty(@NotNull final DifficultyConfig cfg) {
        if (cfg.getPresetIds().isEmpty()) {
            logger.severe("difficulty.yml → no difficulty presets defined. Add at least one.");
            return false;
        }
        boolean ok = true;
        for (final DifficultyConfig.DifficultyPreset preset : cfg.getAllPresets().values()) {
            if (preset.healthMultiplier() <= 0) {
                logger.severe("difficulty.yml → [" + preset.id() + "] health-multiplier must be > 0.");
                ok = false;
            }
            if (preset.damageMultiplier() <= 0) {
                logger.severe("difficulty.yml → [" + preset.id() + "] damage-multiplier must be > 0.");
                ok = false;
            }
        }
        return ok;
    }
}
