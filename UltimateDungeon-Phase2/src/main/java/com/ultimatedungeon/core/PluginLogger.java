package com.ultimatedungeon.core;

import com.ultimatedungeon.UltimateDungeon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Structured logger for UltimateDungeon.
 *
 * <p>Four levels: INFO (always), DEBUG (debug mode only), WARNING (always),
 * SEVERE (always + optional stacktrace). Debug messages are completely
 * suppressed when debug mode is off — zero cost on production servers.</p>
 *
 * <p>Debug mode is read from {@code config.yml → debug} on startup and may
 * be toggled live via {@code /dungeon admin debug}.</p>
 */
public final class PluginLogger {

    private final Logger logger;
    private volatile boolean debugMode;

    public PluginLogger(@NotNull final UltimateDungeon plugin) {
        this.logger   = plugin.getLogger();
        this.debugMode = false;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Logs at INFO level. Always visible. */
    public void info(@NotNull final String message) {
        logger.info(message);
    }

    /**
     * Logs at DEBUG level. Suppressed unless {@link #setDebugMode(boolean)} is
     * {@code true}. Callers may use string concatenation freely — the guard
     * check prevents any method call overhead when debug is off.
     */
    public void debug(@NotNull final String message) {
        if (debugMode) {
            logger.info("[DEBUG] " + message);
        }
    }

    /** Logs at WARNING level. Always visible. */
    public void warning(@NotNull final String message) {
        logger.warning(message);
    }

    /** Logs at SEVERE level with an attached throwable. Always visible. */
    public void severe(@NotNull final String message, @Nullable final Throwable throwable) {
        if (throwable != null) {
            logger.log(Level.SEVERE, message, throwable);
        } else {
            logger.severe(message);
        }
    }

    /** Logs at SEVERE level without a throwable. Always visible. */
    public void severe(@NotNull final String message) {
        severe(message, null);
    }

    // ── Debug mode ────────────────────────────────────────────────────────────

    /**
     * Enables or disables debug logging at runtime.
     *
     * @param enabled {@code true} to activate verbose debug output
     */
    public void setDebugMode(final boolean enabled) {
        this.debugMode = enabled;
        info("Debug mode " + (enabled ? "ENABLED" : "DISABLED") + ".");
    }

    /** Returns {@code true} if debug mode is currently active. */
    public boolean isDebugMode() {
        return debugMode;
    }
}
