package dev.opinion2nd.customboss;

import dev.opinion2nd.customboss.command.BossCommand;
import dev.opinion2nd.customboss.listener.BossListener;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Entry point. Wires up the boss manager, command and listeners and keeps the
 * currently loaded {@link BossSettings} so /boss reload can swap them live.
 */
public final class CustomBossPlugin extends JavaPlugin {

    private NamespacedKey bossKey;
    private NamespacedKey minionKey;
    private BossManager bossManager;
    private BossSettings settings;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.bossKey = new NamespacedKey(this, "custom_boss");
        this.minionKey = new NamespacedKey(this, "boss_minion");
        this.settings = new BossSettings(getConfig(), getLogger());
        this.bossManager = new BossManager(this);

        getServer().getPluginManager().registerEvents(new BossListener(this, bossManager), this);

        PluginCommand command = getCommand("boss");
        if (command != null) {
            BossCommand handler = new BossCommand(this, bossManager);
            command.setExecutor(handler);
            command.setTabCompleter(handler);
        }

        getLogger().info("CustomBoss enabled.");
    }

    @Override
    public void onDisable() {
        if (bossManager != null) {
            bossManager.removeAll();
        }
    }

    /** Re-reads config.yml from disk and rebuilds the cached settings. */
    public void reloadSettings() {
        reloadConfig();
        this.settings = new BossSettings(getConfig(), getLogger());
    }

    public NamespacedKey getBossKey() {
        return bossKey;
    }

    public NamespacedKey getMinionKey() {
        return minionKey;
    }

    public BossManager getBossManager() {
        return bossManager;
    }

    public BossSettings getSettings() {
        return settings;
    }
}
