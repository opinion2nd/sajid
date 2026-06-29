package com.ultimatedungeon.core;

import com.ultimatedungeon.UltimateDungeon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Structured logger wrapper for UltimateDungeon.
 *
 * <p>Provides levelled logging (INFO, DEBUG, WARNING, ERROR/SEVERE) with
 * optional debug mode. Debug messages are suppressed unless debug mode
 * is active, preventing performance degradation on production servers.</p>
 *
 * <p>Debug mode is controlled by {@code config.yml → debug: true/false}
 * and can be toggled at runtime via the {@code /dungeon admin debug} command.</p>
 */
public final class PluginLogger {

    private static final String PREFIX = "[UltimateDungeon] ";

    private final Logger logger;
    private boolean debugMode;

    public PluginLogger(@NotNull final UltimateDungeon plugin) {
        this.logger = plugin.getLogger();
        this.debugMode = false;
    }

    // ── Log methods ───────────────────────────────────────────────────────────

    /**
     * Logs an informational message. Always visible.
     *
     * @param message the message to log
     */
    public void info(@NotNull final String message) {
        logger.info(message);
    }

    /**
     * Logs a debug message. Suppressed unless debug mode is active.
     *
     * @param message the debug message
     */
    public void debug(@NotNull final String message) {
        if (debugMode) {
            logger.info("[DEBUG] " + message);
        }
    }

    /**
     * Logs a warning. Always visible.
     *
     * @param message the warning message
     */
    public void warning(@NotNull final String message) {
        logger.warning(message);
    }

    /**
     * Logs a severe error with an optional throwable. Always visible.
     *
     * @param message   the error message
     * @param throwable the exception, or null if none
     */
    public void severe(@NotNull final String message, @Nullable final Throwable throwable) {
        if (throwable != null) {
            logger.log(Level.SEVERE, message, throwable);
        } else {
            logger.severe(message);
        }
    }

    /**
     * Logs a severe error without a throwable. Always visible.
     *
     * @param message the error message
     */
    public void severe(@NotNull final String message) {
        severe(message, null);
    }

    // ── Debug mode ────────────────────────────────────────────────────────────

    /**
     * Enables or disables debug mode at runtime.
     *
     * @param enabled true to enable debug logging
     */
    public void setDebugMode(final boolean enabled) {
        this.debugMode = enabled;
        info("Debug mode " + (enabled ? "enabled" : "disabled") + ".");
    }

    /**
     * Returns whether debug mode is currently active.
     *
     * @return true if debug mode is on
     */
    public boolean isDebugMode() {
        return debugMode;
    }
}
