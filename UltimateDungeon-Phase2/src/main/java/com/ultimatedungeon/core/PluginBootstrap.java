package com.ultimatedungeon.core;

import com.ultimatedungeon.UltimateDungeon;
import com.ultimatedungeon.compat.VersionDetector;
import com.ultimatedungeon.config.ConfigManager;
import com.ultimatedungeon.database.DatabaseManager;
import com.ultimatedungeon.economy.NoOpEconomyProvider;
import com.ultimatedungeon.economy.VaultEconomyProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Orchestrates the plugin startup sequence in strict dependency order.
 *
 * <h3>Phase order</h3>
 * <ol>
 *   <li><strong>Logger</strong> — must be first; everything else logs.</li>
 *   <li><strong>Version detection</strong> — platform detection before any
 *       platform-specific code runs.</li>
 *   <li><strong>Service registry</strong> — DI container ready for all phases.</li>
 *   <li><strong>Configuration</strong> — typed config wrappers loaded and
 *       validated; debug mode applied to logger.</li>
 *   <li><strong>Scheduler</strong> — task scheduler ready for async use.</li>
 *   <li><strong>Database</strong> — pool opened, migrations applied, DAOs wired.</li>
 *   <li><strong>Registries</strong> — boss, monster, trap, puzzle, room, theme,
 *       loot table registries populated (Milestone 2+).</li>
 *   <li><strong>Managers</strong> — dungeon, party, session, cooldown, GUI
 *       managers constructed (Milestone 3+).</li>
 *   <li><strong>Commands</strong> — command executors registered (Milestone 5).</li>
 *   <li><strong>Listeners</strong> — Bukkit event listeners registered (Milestone 3+).</li>
 *   <li><strong>Economy</strong> — Vault soft-depend hooked last; falls back
 *       to no-op if Vault is absent.</li>
 * </ol>
 *
 * <p>Any unchecked exception thrown during startup propagates up to
 * {@link UltimateDungeon#onEnable()}, which disables the plugin cleanly.</p>
 */
public final class PluginBootstrap {

    private final UltimateDungeon plugin;

    // ── Core services ─────────────────────────────────────────────────────────
    private PluginLogger     pluginLogger;
    private ServiceRegistry  serviceRegistry;
    private PluginScheduler  pluginScheduler;
    private ConfigManager    configManager;
    private DatabaseManager  databaseManager;
    private VersionDetector  versionDetector;

    public PluginBootstrap(@NotNull final UltimateDungeon plugin) {
        this.plugin = plugin;
    }

    // ── Startup ───────────────────────────────────────────────────────────────

    /**
     * Executes the full startup sequence. A failure in any phase throws an
     * exception which the caller (plugin entry point) handles by disabling.
     */
    public void start() {
        initLogger();
        initVersionDetection();
        initServiceRegistry();
        initConfiguration();
        initScheduler();
        initDatabase();
        initRegistries();
        initManagers();
        initCommands();
        initListeners();
        initEconomy();

        pluginLogger.info("UltimateDungeon v"
                + plugin.getDescription().getVersion() + " enabled successfully.");
    }

    // ── Shutdown ──────────────────────────────────────────────────────────────

    /**
     * Performs final shutdown bookkeeping after all systems have been torn down
     * by {@link PluginShutdownHandler}.
     */
    public void shutdown() {
        pluginLogger.info("UltimateDungeon shutdown complete.");
    }

    // ── Init phases ───────────────────────────────────────────────────────────

    private void initLogger() {
        pluginLogger = new PluginLogger(plugin);
        pluginLogger.info("Logger initialised.");
    }

    private void initVersionDetection() {
        versionDetector = new VersionDetector();
        pluginLogger.info("Server: " + versionDetector.getVersionString()
                + (versionDetector.isFolia()    ? " [Folia]"    : "")
                + (versionDetector.isPurpur()   ? " [Purpur]"   : "")
                + (versionDetector.isSpigotOnly() ? " [Spigot]" : " [Paper]"));
    }

    private void initServiceRegistry() {
        serviceRegistry = new ServiceRegistry(pluginLogger);
        // Register the logger itself so any system can retrieve it via registry.
        serviceRegistry.register(PluginLogger.class, pluginLogger);
        serviceRegistry.register(VersionDetector.class, versionDetector);
        pluginLogger.info("Service registry initialised.");
    }

    private void initConfiguration() {
        configManager = new ConfigManager(plugin, pluginLogger);
        configManager.load();

        // Apply debug setting from config immediately so subsequent phases log correctly.
        pluginLogger.setDebugMode(configManager.getMainConfig().isDebug());

        serviceRegistry.register(ConfigManager.class, configManager);
        pluginLogger.info("Configuration loaded ("
                + (pluginLogger.isDebugMode() ? "DEBUG mode ON" : "debug mode off") + ").");
    }

    private void initScheduler() {
        pluginScheduler = new PluginScheduler(plugin, pluginLogger);
        serviceRegistry.register(PluginScheduler.class, pluginScheduler);
        pluginLogger.info("Scheduler initialised.");
    }

    private void initDatabase() {
        databaseManager = new DatabaseManager(
                configManager.getDatabaseConfig(),
                pluginLogger,
                plugin.getDataFolder()
        );
        databaseManager.initialise();
        serviceRegistry.register(DatabaseManager.class, databaseManager);
    }

    private void initRegistries() {
        // Boss, Monster, Trap, Puzzle, Room, Theme, Loot registries.
        // Implemented in Milestone 2.
        pluginLogger.debug("Registries phase — awaiting Milestone 2.");
    }

    private void initManagers() {
        // DungeonInstanceManager, PartyManager, PlayerSessionManager, CooldownManager, GuiManager.
        // Implemented in Milestone 3.
        pluginLogger.debug("Managers phase — awaiting Milestone 3.");
    }

    private void initCommands() {
        // DungeonCommand, PartyCommand registration on the server.
        // Implemented in Milestone 5.
        pluginLogger.debug("Commands phase — awaiting Milestone 5.");
    }

    private void initListeners() {
        // GuiClickListener and all gameplay listeners.
        // Implemented in Milestone 3+.
        pluginLogger.debug("Listeners phase — awaiting Milestone 3.");
    }

    private void initEconomy() {
        final VaultEconomyProvider vault = new VaultEconomyProvider(plugin);
        if (vault.isAvailable()) {
            serviceRegistry.register(
                    com.ultimatedungeon.api.economy.IEconomyProvider.class, vault);
            pluginLogger.info("Economy: Vault hooked successfully.");
        } else {
            serviceRegistry.register(
                    com.ultimatedungeon.api.economy.IEconomyProvider.class,
                    new NoOpEconomyProvider());
            pluginLogger.info("Economy: Vault not found — money rewards disabled.");
        }
    }

    // ── Accessors (used by PluginShutdownHandler) ─────────────────────────────

    @NotNull public PluginLogger    getPluginLogger()    { return pluginLogger; }
    @NotNull public ServiceRegistry getServiceRegistry() { return serviceRegistry; }
    @NotNull public PluginScheduler getPluginScheduler() { return pluginScheduler; }
    @NotNull public ConfigManager   getConfigManager()   { return configManager; }
    @NotNull public DatabaseManager getDatabaseManager() { return databaseManager; }
}
