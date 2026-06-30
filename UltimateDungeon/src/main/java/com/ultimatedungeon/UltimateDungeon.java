package com.ultimatedungeon;

import com.ultimatedungeon.core.PluginBootstrap;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.core.PluginShutdownHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * UltimateDungeon — Premium procedural dungeon plugin.
 *
 * <p>This class is the plugin entry point. It is intentionally minimal.
 * All startup and shutdown logic is delegated to {@link PluginBootstrap}
 * and {@link PluginShutdownHandler} to keep this class a clean lifecycle hook.</p>
 */
public final class UltimateDungeon extends JavaPlugin {

    private static UltimateDungeon instance;

    private PluginBootstrap bootstrap;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onEnable() {
        instance = this;

        bootstrap = new PluginBootstrap(this);
        bootstrap.start();
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) {
            new PluginShutdownHandler(this, bootstrap).shutdown();
        }
        instance = null;
    }

    // ── Static accessor ───────────────────────────────────────────────────────

    /**
     * Returns the singleton plugin instance.
     *
     * <p>Prefer constructor injection over this method wherever possible.
     * This accessor exists for the rare cases where direct injection is
     * impractical (e.g. static utility helpers).</p>
     *
     * @return the active {@link UltimateDungeon} instance
     */
    @NotNull
    public static UltimateDungeon getInstance() {
        if (instance == null) {
            throw new IllegalStateException("UltimateDungeon is not enabled.");
        }
        return instance;
    }

    /**
     * Convenience accessor for the plugin's {@link PluginLogger}.
     *
     * @return the plugin logger
     */
    @NotNull
    public PluginLogger getPluginLogger() {
        return bootstrap.getPluginLogger();
    }
}
