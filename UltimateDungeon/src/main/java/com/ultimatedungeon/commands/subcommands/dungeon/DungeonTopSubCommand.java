package com.ultimatedungeon.commands.subcommands.dungeon;

import com.ultimatedungeon.commands.framework.ISubCommand;
import com.ultimatedungeon.services.StatisticsService;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * {@code /dungeon top [clears|bosses|kills|secrets|time]} — server leaderboards.
 * Defaults to fastest clears by count; {@code time} ranks by fastest run.
 */
public final class DungeonTopSubCommand implements ISubCommand {

    private static final int TOP_LIMIT = 10;
    private static final Map<String, String> BOARDS = Map.of(
            "clears",  "dungeons_completed",
            "bosses",  "bosses_defeated",
            "kills",   "monsters_killed",
            "secrets", "secrets_found",
            "time",    "fastest_run_ms");

    private final StatisticsService statistics;

    public DungeonTopSubCommand(@NotNull final StatisticsService statistics) {
        this.statistics = statistics;
    }

    @Override @NotNull public String getName() { return "top"; }
    @Override @NotNull public String getPermission() { return "dungeon.stats"; }

    @Override
    public void execute(@NotNull final CommandSender sender, @NotNull final String[] args) {
        if (!(sender instanceof final Player player)) return;
        final String board = args.length > 0 ? args[0].toLowerCase() : "clears";
        final String column = BOARDS.get(board);
        if (column == null) {
            MiniMessageUtil.send(player, "<red>Unknown leaderboard. <gray>Try: "
                    + String.join(", ", BOARDS.keySet()));
            return;
        }
        statistics.loadTop(column, TOP_LIMIT).thenAccept(entries -> {
            MiniMessageUtil.send(player, "<gold><bold>Top " + TOP_LIMIT + " — " + board);
            if (entries.isEmpty()) {
                MiniMessageUtil.send(player, "<gray>No entries yet. Go clear a dungeon!");
                return;
            }
            int place = 1;
            for (final var entry : entries) {
                final String value = "time".equals(board)
                        ? formatTime(entry.value()) : String.valueOf(entry.value());
                MiniMessageUtil.send(player, medal(place) + " <yellow>" + place + ". <white>"
                        + entry.playerName() + " <gray>— <green>" + value);
                place++;
            }
        });
    }

    @NotNull
    private String medal(final int place) {
        return switch (place) {
            case 1 -> "<gold>①";
            case 2 -> "<gray>②";
            case 3 -> "<#CD7F32>③";
            default -> "<dark_gray>•";
        };
    }

    @NotNull
    private String formatTime(final long ms) {
        final long totalSeconds = ms / 1000;
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    @Override @NotNull
    public List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String[] args) {
        if (args.length == 1) {
            return BOARDS.keySet().stream()
                    .filter(k -> k.startsWith(args[0].toLowerCase())).sorted().toList();
        }
        return List.of();
    }
}
