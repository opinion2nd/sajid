package com.ultimatedungeon.core;

import com.ultimatedungeon.UltimateDungeon;
import org.jetbrains.annotations.NotNull;

/**
 * Orchestrates the plugin startup sequence in strict dependency order.
 *
 * <p>Each system is initialised exactly once, in the order required by its
 * dependencies. No system may reference another until that other system has
 * been fully initialised by this class.</p>
 *
 * <p>Phase order:
 * <ol>
 *   <li>Logger</li>
 *   <li>Service registry</li>
 *   <li>Configuration</li>
 *   <li>Database</li>
 *   <li>Registries (bosses, monsters, traps, puzzles, rooms, themes, loot)</li>
 *   <li>Managers (dungeon, party, player session, cooldown, GUI)</li>
 *   <li>Commands</li>
 *   <li>Listeners</li>
 *   <li>Scheduler tasks</li>
 *   <li>Economy hook (soft-depend, last)</li>
 * </ol>
 * </p>
 */
public final class PluginBootstrap {

    private final UltimateDungeon plugin;
    private PluginLogger pluginLogger;
    private ServiceRegistry serviceRegistry;

    public PluginBootstrap(@NotNull final UltimateDungeon plugin) {
        this.plugin = plugin;
    }

    // ── Startup ───────────────────────────────────────────────────────────────

    /**
     * Executes the full startup sequence. If any critical phase fails the
     * plugin disables itself cleanly rather than leaving the server in an
     * inconsistent state.
     */
    public void start() {
        try {
            initLogger();
            initServiceRegistry();
            initConfiguration();
            initDatabase();
            initRegistries();
            initManagers();
            initCommands();
            initListeners();
            initTasks();
            initEconomy();

            pluginLogger.info("UltimateDungeon enabled successfully.");
        } catch (final Exception e) {
            if (pluginLogger != null) {
                pluginLogger.severe("Critical startup failure — disabling plugin.", e);
            } else {
                plugin.getLogger().severe("Critical startup failure — disabling plugin: " + e.getMessage());
            }
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    // ── Shutdown ──────────────────────────────────────────────────────────────

    /**
     * Shuts down all systems in reverse initialisation order.
     * Called by {@link com.ultimatedungeon.core.PluginShutdownHandler}.
     */
    public void shutdown() {
        // Implemented in Milestone 1 — Phase 1 skeleton only.
        if (pluginLogger != null) {
            pluginLogger.info("UltimateDungeon shutdown complete.");
        }
    }

    // ── Private init phases ───────────────────────────────────────────────────

    private void initLogger() {
        pluginLogger = new PluginLogger(plugin);
        pluginLogger.info("Logger initialised.");
    }

    private void initServiceRegistry() {
        serviceRegistry = new ServiceRegistry(plugin, pluginLogger);
        pluginLogger.info("Service registry initialised.");
    }

    private void initConfiguration() {
        // Delegated to ConfigManager in Phase 1 implementation.
        pluginLogger.info("Configuration phase — pending implementation.");
    }

    private void initDatabase() {
        // Delegated to DatabaseManager in Phase 1 implementation.
        pluginLogger.info("Database phase — pending implementation.");
    }

    private void initRegistries() {
        // Boss, Monster, Trap, Puzzle, Room, Theme, Loot registries.
        pluginLogger.info("Registries phase — pending implementation.");
    }

    private void initManagers() {
        // DungeonInstanceManager, PartyManager, PlayerSessionManager, etc.
        pluginLogger.info("Managers phase — pending implementation.");
    }

    private void initCommands() {
        // DungeonCommand, PartyCommand registration.
        pluginLogger.info("Commands phase — pending implementation.");
    }

    private void initListeners() {
        // All Bukkit event listeners registration.
        pluginLogger.info("Listeners phase — pending implementation.");
    }

    private void initTasks() {
        // All repeating and delayed scheduled tasks.
        pluginLogger.info("Tasks phase — pending implementation.");
    }

    private void initEconomy() {
        // Vault soft-dependency hook.
        pluginLogger.info("Economy phase — pending implementation.");
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    @NotNull
    public PluginLogger getPluginLogger() {
        return pluginLogger;
    }

    @NotNull
    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }
}
