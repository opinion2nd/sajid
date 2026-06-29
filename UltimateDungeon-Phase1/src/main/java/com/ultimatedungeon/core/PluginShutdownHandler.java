package com.ultimatedungeon.core;

import com.ultimatedungeon.UltimateDungeon;
import org.jetbrains.annotations.NotNull;

/**
 * Orchestrates the plugin shutdown sequence in reverse initialisation order.
 *
 * <p>Responsible for ensuring all active dungeon instances are ended cleanly,
 * all scheduled tasks are cancelled, all database connections are closed,
 * and all managed memory is released before the plugin goes offline.</p>
 */
public final class PluginShutdownHandler {

    private final UltimateDungeon plugin;
    private final PluginBootstrap bootstrap;

    public PluginShutdownHandler(
            @NotNull final UltimateDungeon plugin,
            @NotNull final PluginBootstrap bootstrap
    ) {
        this.plugin = plugin;
        this.bootstrap = bootstrap;
    }

    /**
     * Executes the full shutdown sequence.
     *
     * <p>Exceptions are caught per-phase so a failure in one phase does not
     * prevent subsequent cleanup phases from executing.</p>
     */
    public void shutdown() {
        final PluginLogger logger = bootstrap.getPluginLogger();
        logger.info("Beginning shutdown sequence...");

        runPhase("Tasks",       this::shutdownTasks,        logger);
        runPhase("Listeners",   this::shutdownListeners,    logger);
        runPhase("Dungeons",    this::shutdownDungeons,     logger);
        runPhase("Managers",    this::shutdownManagers,     logger);
        runPhase("Database",    this::shutdownDatabase,     logger);
        runPhase("Registry",    this::shutdownRegistry,     logger);

        bootstrap.shutdown();
    }

    // ── Shutdown phases ───────────────────────────────────────────────────────

    private void shutdownTasks() {
        // Cancel all tracked scheduled tasks.
        // PluginScheduler.cancelAll() — wired in Phase 1 implementation.
    }

    private void shutdownListeners() {
        // Unregister all event listeners.
    }

    private void shutdownDungeons() {
        // Force-end all active dungeon instances and clean their worlds.
    }

    private void shutdownManagers() {
        // Clear all manager state: parties, sessions, cooldowns, GUIs.
    }

    private void shutdownDatabase() {
        // Close all database connections and flush pending writes.
    }

    private void shutdownRegistry() {
        // Clear the service registry.
        bootstrap.getServiceRegistry().clear();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void runPhase(
            @NotNull final String name,
            @NotNull final Runnable phase,
            @NotNull final PluginLogger logger
    ) {
        try {
            phase.run();
            logger.debug("Shutdown phase complete: " + name);
        } catch (final Exception e) {
            logger.severe("Error during shutdown phase [" + name + "]: " + e.getMessage(), e);
        }
    }
}
