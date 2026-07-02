package com.geodash;

import com.geodash.command.GdCommand;
import com.geodash.fx.EffectsManager;
import com.geodash.game.GameManager;
import com.geodash.game.RaceManager;
import com.geodash.level.LevelManager;
import com.geodash.listener.GameListener;
import com.geodash.reward.RewardManager;
import com.geodash.stats.StatsManager;
import com.geodash.util.CompatScheduler;
import com.geodash.util.Msg;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class GeoDashPlugin extends JavaPlugin {

    private LevelManager levels;
    private StatsManager stats;
    private EffectsManager effects;
    private RewardManager rewards;
    private GameManager game;
    private RaceManager races;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Msg.setPrefix(getConfig().getString("prefix", "&8[&bGeo&3Dash&8] &7"));

        levels = new LevelManager(this);
        levels.load();
        stats = new StatsManager(this);
        stats.load();
        effects = new EffectsManager(this);
        rewards = new RewardManager(this);
        game = new GameManager(this);
        races = new RaceManager(this);

        getServer().getPluginManager().registerEvents(new GameListener(this), this);

        GdCommand executor = new GdCommand(this);
        PluginCommand command = getCommand("gd");
        command.setExecutor(executor);
        command.setTabCompleter(executor);

        getLogger().info("GeoDash enabled" + (CompatScheduler.isFolia() ? " (Folia mode)" : "")
                + " - " + levels.all().size() + " level(s) ready. Jump!");
    }

    @Override
    public void onDisable() {
        if (game != null) {
            game.leaveAll();
        }
        if (levels != null) {
            levels.save();
        }
        if (stats != null) {
            stats.shutdown();
        }
    }

    public void reloadEverything() {
        reloadConfig();
        Msg.setPrefix(getConfig().getString("prefix", "&8[&bGeo&3Dash&8] &7"));
        levels.load();
        effects.reload();
        game.reload();
    }

    public LevelManager getLevels() {
        return levels;
    }

    public StatsManager getStats() {
        return stats;
    }

    public EffectsManager getEffects() {
        return effects;
    }

    public RewardManager getRewards() {
        return rewards;
    }

    public GameManager getGame() {
        return game;
    }

    public RaceManager getRaces() {
        return races;
    }
}
