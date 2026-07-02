package com.ultimatedungeon.config;

import com.ultimatedungeon.UltimateDungeon;
import com.ultimatedungeon.config.files.*;
import com.ultimatedungeon.core.PluginLogger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Central configuration manager for UltimateDungeon.
 *
 * <h3>Startup sequence (per file)</h3>
 * <ol>
 *   <li>If the file does not exist on disk, extract the bundled default from
 *       the plugin jar.</li>
 *   <li>Load the file from disk into a {@link YamlConfiguration}.</li>
 *   <li>Copy any missing keys from the bundled default so new config options
 *       are added automatically on upgrade.</li>
 *   <li>Run {@link ConfigMigrator} to apply version-based structural changes.</li>
 *   <li>Save back to disk if any changes were made.</li>
 *   <li>Wrap in a typed config class and expose via a getter.</li>
 * </ol>
 *
 * <h3>Reload</h3>
 * Calling {@link #reload()} repeats steps 2–6 for every file (no extraction
 * needed after the first run).  Typed wrappers are rebuilt from the fresh data.
 *
 * <h3>Thread safety</h3>
 * Config loading and reloading must happen on the main server thread.
 */
public final class ConfigManager {

    // ── File names ────────────────────────────────────────────────────────────
    private static final String FILE_MAIN        = "config.yml";
    private static final String FILE_MESSAGES    = "messages.yml";
    private static final String FILE_DUNGEON     = "dungeon.yml";
    private static final String FILE_THEMES      = "themes.yml";
    private static final String FILE_BOSSES      = "bosses.yml";
    private static final String FILE_MONSTERS    = "monsters.yml";
    private static final String FILE_TRAPS       = "traps.yml";
    private static final String FILE_REWARDS     = "rewards.yml";
    private static final String FILE_LOOT        = "loot.yml";
    private static final String FILE_GUI         = "gui.yml";
    private static final String FILE_PARTY       = "party.yml";
    private static final String FILE_DIFFICULTY  = "difficulty.yml";
    private static final String FILE_DATABASE    = "database.yml";
    private static final String FILE_PERFORMANCE = "performance.yml";

    private static final List<String> ALL_FILES = List.of(
            FILE_MAIN, FILE_MESSAGES, FILE_DUNGEON, FILE_THEMES,
            FILE_BOSSES, FILE_MONSTERS, FILE_TRAPS, FILE_REWARDS,
            FILE_LOOT, FILE_GUI, FILE_PARTY, FILE_DIFFICULTY,
            FILE_DATABASE, FILE_PERFORMANCE
    );

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final UltimateDungeon plugin;
    private final PluginLogger    logger;
    private final ConfigMigrator  migrator;
    private final ConfigValidator validator;

    // ── Typed config wrappers ─────────────────────────────────────────────────
    private MainConfig        mainConfig;
    private MessagesConfig    messagesConfig;
    private DungeonConfig     dungeonConfig;
    private ThemesConfig      themesConfig;
    private BossesConfig      bossesConfig;
    private MonstersConfig    monstersConfig;
    private TrapsConfig       trapsConfig;
    private RewardsConfig     rewardsConfig;
    private LootConfig        lootConfig;
    private GuiConfig         guiConfig;
    private PartyConfig       partyConfig;
    private DifficultyConfig  difficultyConfig;
    private DatabaseConfig    databaseConfig;
    private PerformanceConfig performanceConfig;

    public ConfigManager(
            @NotNull final UltimateDungeon plugin,
            @NotNull final PluginLogger    logger
    ) {
        this.plugin    = plugin;
        this.logger    = logger;
        this.migrator  = new ConfigMigrator(logger);
        this.validator = new ConfigValidator(logger);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Extracts defaults, loads, migrates, validates, and caches all config files.
     *
     * @throws IllegalStateException if any critical validation fails
     */
    public void load() {
        logger.info("Loading configuration files...");

        for (final String fileName : ALL_FILES) {
            extractDefault(fileName);
        }

        runMigrations();
        loadAllFiles();
        runValidation();

        logger.info("All " + ALL_FILES.size() + " configuration files loaded successfully.");
    }

    /**
     * Reloads all configuration files from disk.
     * Typed wrappers are rebuilt so callers always see fresh values.
     */
    public void reload() {
        logger.info("Reloading configuration files...");
        runMigrations();
        loadAllFiles();
        runValidation();
        logger.info("Configuration reloaded successfully.");
    }

    // ── Typed config accessors ────────────────────────────────────────────────

    @NotNull public MainConfig        getMainConfig()        { return mainConfig; }
    @NotNull public MessagesConfig    getMessagesConfig()    { return messagesConfig; }
    @NotNull public DungeonConfig     getDungeonConfig()     { return dungeonConfig; }
    @NotNull public ThemesConfig      getThemesConfig()      { return themesConfig; }
    @NotNull public BossesConfig      getBossesConfig()      { return bossesConfig; }
    @NotNull public MonstersConfig    getMonstersConfig()    { return monstersConfig; }
    @NotNull public TrapsConfig       getTrapsConfig()       { return trapsConfig; }
    @NotNull public RewardsConfig     getRewardsConfig()     { return rewardsConfig; }
    @NotNull public LootConfig        getLootConfig()        { return lootConfig; }
    @NotNull public GuiConfig         getGuiConfig()         { return guiConfig; }
    @NotNull public PartyConfig       getPartyConfig()       { return partyConfig; }
    @NotNull public DifficultyConfig  getDifficultyConfig()  { return difficultyConfig; }
    @NotNull public DatabaseConfig    getDatabaseConfig()    { return databaseConfig; }
    @NotNull public PerformanceConfig getPerformanceConfig() { return performanceConfig; }

    // ── Private — loading ─────────────────────────────────────────────────────

    private void loadAllFiles() {
        final FileConfiguration mainRaw        = load(FILE_MAIN);
        final FileConfiguration messagesRaw    = load(FILE_MESSAGES);
        final FileConfiguration dungeonRaw     = load(FILE_DUNGEON);
        final FileConfiguration themesRaw      = load(FILE_THEMES);
        final FileConfiguration bossesRaw      = load(FILE_BOSSES);
        final FileConfiguration monstersRaw    = load(FILE_MONSTERS);
        final FileConfiguration trapsRaw       = load(FILE_TRAPS);
        final FileConfiguration rewardsRaw     = load(FILE_REWARDS);
        final FileConfiguration lootRaw        = load(FILE_LOOT);
        final FileConfiguration guiRaw         = load(FILE_GUI);
        final FileConfiguration partyRaw       = load(FILE_PARTY);
        final FileConfiguration difficultyRaw  = load(FILE_DIFFICULTY);
        final FileConfiguration databaseRaw    = load(FILE_DATABASE);
        final FileConfiguration performanceRaw = load(FILE_PERFORMANCE);

        mainConfig        = new MainConfig(mainRaw);
        messagesConfig    = new MessagesConfig(messagesRaw);
        dungeonConfig     = new DungeonConfig(dungeonRaw);
        themesConfig      = new ThemesConfig(themesRaw);
        bossesConfig      = new BossesConfig(bossesRaw);
        monstersConfig    = new MonstersConfig(monstersRaw);
        trapsConfig       = new TrapsConfig(trapsRaw);
        rewardsConfig     = new RewardsConfig(rewardsRaw);
        lootConfig        = new LootConfig(lootRaw);
        guiConfig         = new GuiConfig(guiRaw);
        partyConfig       = new PartyConfig(partyRaw);
        difficultyConfig  = new DifficultyConfig(difficultyRaw);
        databaseConfig    = new DatabaseConfig(databaseRaw);
        performanceConfig = new PerformanceConfig(performanceRaw);
    }

    /**
     * Loads a YAML file from the plugin data folder and copies in any keys that
     * exist in the bundled default but are absent from the on-disk copy.
     */
    @NotNull
    private FileConfiguration load(@NotNull final String fileName) {
        final File file = new File(plugin.getDataFolder(), fileName);

        // Ensure file exists (should have been extracted by extractDefault already).
        if (!file.exists()) {
            logger.warning("Config file not found after extraction: " + fileName
                    + ". Creating empty defaults.");
            extractDefault(fileName);
        }

        final YamlConfiguration disk    = YamlConfiguration.loadConfiguration(file);
        final YamlConfiguration bundled = loadBundled(fileName);

        // Add any keys present in the bundled default but missing from disk.
        boolean updated = false;
        for (final String key : bundled.getKeys(true)) {
            if (!disk.isSet(key)) {
                disk.set(key, bundled.get(key));
                updated = true;
                logger.debug(fileName + ": added missing key '" + key + "' from defaults.");
            }
        }

        if (updated) {
            saveFile(file, disk);
        }

        logger.debug("Loaded: " + fileName);
        return disk;
    }

    /**
     * Loads the bundled (in-jar) default for {@code fileName} as a
     * {@link YamlConfiguration}.
     */
    @NotNull
    private YamlConfiguration loadBundled(@NotNull final String fileName) {
        final InputStream stream = plugin.getResource(fileName);
        if (stream == null) {
            logger.warning("No bundled default found for: " + fileName);
            return new YamlConfiguration();
        }
        try (final InputStreamReader reader =
                     new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (final IOException e) {
            logger.severe("Failed to read bundled default for " + fileName, e);
            return new YamlConfiguration();
        }
    }

    // ── Private — extraction ──────────────────────────────────────────────────

    /**
     * Extracts the bundled default config from the jar to the plugin data folder
     * if the file does not already exist on disk. Preserves any existing file.
     */
    private void extractDefault(@NotNull final String fileName) {
        final File target = new File(plugin.getDataFolder(), fileName);
        if (target.exists()) {
            return;
        }
        try {
            plugin.saveResource(fileName, false);
            logger.debug("Extracted default config: " + fileName);
        } catch (final IllegalArgumentException e) {
            // Resource does not exist in the jar — log and continue.
            logger.warning("No bundled resource found for: " + fileName
                    + ". Skipping extraction.");
        }
    }

    // ── Private — migration ───────────────────────────────────────────────────

    private void runMigrations() {
        // Migrations run BEFORE typed wrappers are built so the same boot
        // already sees the migrated data.
        for (final String fileName : ALL_FILES) {
            migrateFile(fileName);
        }
    }

    private void migrateFile(@NotNull final String fileName) {
        final File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            return;
        }
        final YamlConfiguration disk = YamlConfiguration.loadConfiguration(file);
        final int storedVersion = disk.getInt("config-version", 0);
        if (storedVersion >= ConfigVersion.CURRENT) {
            return;
        }
        final boolean changed = migrator.migrate(fileName, disk, storedVersion);
        if (changed) {
            disk.set("config-version", ConfigVersion.CURRENT);
            saveFile(file, disk);
        }
    }

    // ── Private — validation ──────────────────────────────────────────────────

    private void runValidation() {
        final boolean passed = validator.validate(
                mainConfig, databaseConfig, performanceConfig,
                partyConfig, dungeonConfig, difficultyConfig
        );
        if (!passed) {
            throw new IllegalStateException(
                "One or more critical configuration errors were found. "
                + "Please fix the errors logged above and restart the server."
            );
        }
    }

    // ── Private — save ────────────────────────────────────────────────────────

    private void saveFile(@NotNull final File file, @NotNull final YamlConfiguration config) {
        try {
            config.save(file);
        } catch (final IOException e) {
            logger.severe("Failed to save config file: " + file.getName(), e);
        }
    }
}
