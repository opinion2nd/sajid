package com.geodash.reward;

import com.geodash.GeoDashPlugin;
import com.geodash.level.Level;
import com.geodash.util.CompatScheduler;
import com.geodash.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class RewardManager {

    private final GeoDashPlugin plugin;

    public RewardManager(GeoDashPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean firstTimeOnly() {
        return plugin.getConfig().getBoolean("rewards.first-time-only", true);
    }

    public void completion(Player player, Level level, long timeMs, int attempts) {
        dispatch("rewards.completion", player, level, timeMs, attempts);
    }

    public void raceWin(Player player, Level level, long timeMs, int attempts) {
        dispatch("rewards.race-win", player, level, timeMs, attempts);
    }

    private void dispatch(String path, Player player, Level level, long timeMs, int attempts) {
        String broadcast = plugin.getConfig().getString(path + ".broadcast", "");
        if (broadcast != null && !broadcast.isEmpty()) {
            Bukkit.broadcastMessage(Msg.color(fill(broadcast, player, level, timeMs, attempts)));
        }
        List<String> commands = plugin.getConfig().getStringList(path + ".commands");
        if (commands.isEmpty()) {
            return;
        }
        // Console commands must run on the global region on Folia
        CompatScheduler.runGlobalLater(plugin, () -> {
            for (String command : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        fill(command, player, level, timeMs, attempts));
            }
        }, 1);
    }

    private String fill(String template, Player player, Level level, long timeMs, int attempts) {
        return template
                .replace("%player%", player.getName())
                .replace("%level%", level.getName())
                .replace("%stars%", String.valueOf(level.getStars()))
                .replace("%time%", Msg.time(timeMs))
                .replace("%attempts%", String.valueOf(attempts));
    }
}
