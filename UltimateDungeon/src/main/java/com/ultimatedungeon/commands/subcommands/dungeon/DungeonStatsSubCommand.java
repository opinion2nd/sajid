package com.ultimatedungeon.commands.subcommands.dungeon;

import com.ultimatedungeon.commands.framework.ISubCommand;
import com.ultimatedungeon.services.StatisticsService;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** {@code /dungeon stats} — shows the player's personal dungeon statistics. */
public final class DungeonStatsSubCommand implements ISubCommand {

    private final StatisticsService statistics;

    public DungeonStatsSubCommand(@NotNull final StatisticsService statistics) {
        this.statistics = statistics;
    }

    @Override @NotNull public String getName() { return "stats"; }
    @Override @NotNull public String getPermission() { return "dungeon.stats"; }

    @Override
    public void execute(@NotNull final CommandSender sender, @NotNull final String[] args) {
        if (!(sender instanceof final Player player)) return;
        statistics.loadStats(player.getUniqueId()).thenAccept(stats -> {
            if (stats == null) {
                MiniMessageUtil.send(player, "<gray>No statistics recorded yet.");
                return;
            }
            MiniMessageUtil.send(player, "<gold><bold>Your Dungeon Stats");
            MiniMessageUtil.send(player, "<gray>Dungeons completed: <white>" + stats.dungeonsCompleted());
            MiniMessageUtil.send(player, "<gray>Bosses defeated: <white>" + stats.bossesDefeated());
            MiniMessageUtil.send(player, "<gray>Monsters killed: <white>" + stats.monstersKilled());
            MiniMessageUtil.send(player, "<gray>Deaths: <white>" + stats.deathCount());
            MiniMessageUtil.send(player, "<gray>Rewards earned: <white>" + stats.rewardsEarned());
        });
    }

    @Override @NotNull
    public List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String[] args) {
        return List.of();
    }
}
