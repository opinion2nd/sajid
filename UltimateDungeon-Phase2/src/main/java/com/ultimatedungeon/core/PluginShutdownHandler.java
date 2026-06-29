package com.ultimatedungeon.core;

import com.ultimatedungeon.UltimateDungeon;
import com.ultimatedungeon.database.DatabaseManager;
import org.jetbrains.annotations.NotNull;

/**
 * Orchestrates plugin shutdown in reverse initialisation order.
 *
 * <p>Every phase is wrapped in its own try-catch so a failure in one phase
 * never prevents subsequent phases from running. This guarantees that the
 * database pool and scheduler are always closed even if a gameplay system
 * throws during teardown.</p>
 *
 * <h3>Shutdown order (reverse of startup)</h3>
 * <ol>
 *   <li>Tasks — cancel all scheduler tasks immediately</li>
 *   <li>Listeners — unregister Bukkit listeners</li>
 *   <li>Dungeons — force-end all active instances, clean worlds</li>
 *   <li>Managers — clear party, session, cooldown, and GUI state</li>
 *   <li>Database — flush writes, close HikariCP pool</li>
 *   <li>Registry — release all service references</li>
 * </ol>
 */
public final class PluginShutdownHandler {

    private final UltimateDungeon plugin;
    private final PluginBootstrap bootstrap;

    public PluginShutdownHandler(
            @NotNull final UltimateDungeon plugin,
            @NotNull final PluginBootstrap bootstrap
    ) {
        this.plugin    = plugin;
        this.bootstrap = bootstrap;
    }

    /**
     * Executes the full shutdown sequence. Exceptions in individual phases are
     * logged and swallowed to ensure all phases complete.
     */
    public void shutdown() {
        final PluginLogger logger = bootstrap.getPluginLogger();
        logger.info("Beginning shutdown sequence...");

        runPhase("Tasks",     this::shutdownTasks,     logger);
        runPhase("Listeners", this::shutdownListeners, logger);
        runPhase("Dungeons",  this::shutdownDungeons,  logger);
        runPhase("Managers",  this::shutdownManagers,  logger);
        runPhase("Database",  this::shutdownDatabase,  logger);
        runPhase("Registry",  this::shutdownRegistry,  logger);

        bootstrap.shutdown();
    }

    // ── Shutdown phases ───────────────────────────────────────────────────────

    private void shutdownTasks() {
        // Cancel every task tracked by the scheduler.
        // Guards against NullPointerException if startup aborted early.
        try {
            bootstrap.getPluginScheduler().cancelAll();
        } catch (final IllegalStateException ignored) {
            // Scheduler was never initialised (startup failed very early).
        }
    }

    private void shutdownListeners() {
        // Unregister all Bukkit event handlers registered by this plugin.
        // Paper/Bukkit automatically calls HandlerList.unregisterAll(plugin)
        // when the plugin is disabled, but doing it explicitly here ensures
        // no events fire during the remaining shutdown phases.
        plugin.getServer().getScheduler().cancelTasks(plugin);
        org.bukkit.event.HandlerList.unregisterAll(plugin);
    }

    private void shutdownDungeons() {
        // Force-end all active dungeon instances and clean their worlds.
        // Implemented in Milestone 3.
        bootstrap.getPluginLogger().debug("Dungeon shutdown — awaiting Milestone 3.");
    }

    private void shutdownManagers() {
        // Clear all stateful managers: party, session, cooldown, GUI.
        // Implemented in Milestone 3+.
        bootstrap.getPluginLogger().debug("Manager shutdown — awaiting Milestone 3.");
    }

    private void shutdownDatabase() {
        try {
            bootstrap.getDatabaseManager().shutdown();
        } catch (final IllegalStateException ignored) {
            // DatabaseManager was never initialised.
        }
    }

    private void shutdownRegistry() {
        try {
            bootstrap.getServiceRegistry().clear();
        } catch (final IllegalStateException ignored) {
            // Registry was never initialised.
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void runPhase(
            @NotNull final String     name,
            @NotNull final Runnable   phase,
            @NotNull final PluginLogger logger
    ) {
        try {
            phase.run();
            logger.debug("Shutdown phase complete: " + name);
        } catch (final Exception e) {
            logger.severe("Error in shutdown phase [" + name + "]: " + e.getMessage(), e);
        }
    }
}
