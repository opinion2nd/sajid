package com.ultimatedungeon.rewards.providers;

import com.ultimatedungeon.api.reward.IReward;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** Dispatches configured console commands, substituting %player% with the name. */
public final class CommandRewardProvider implements IReward {
    private final List<String> commands;
    public CommandRewardProvider(@NotNull final List<String> commands) { this.commands = commands; }
    @Override @NotNull public String getRewardType() { return "CUSTOM_COMMAND"; }
    @Override public void deliver(@NotNull final Player player) {
        for (final String command : commands) {
            if (command == null || command.isBlank()) continue;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    command.replace("%player%", player.getName()));
        }
    }
}
