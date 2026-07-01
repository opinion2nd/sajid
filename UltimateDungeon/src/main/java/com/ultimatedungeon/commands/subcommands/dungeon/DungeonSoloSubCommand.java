package com.ultimatedungeon.commands.subcommands.dungeon;

import com.ultimatedungeon.api.theme.ITheme;
import com.ultimatedungeon.commands.framework.ISubCommand;
import com.ultimatedungeon.config.files.DifficultyConfig;
import com.ultimatedungeon.services.DungeonLaunchService;
import com.ultimatedungeon.theme.registry.ThemeRegistry;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/** {@code /dungeon solo [theme] [difficulty]} — starts a solo dungeon. */
public final class DungeonSoloSubCommand implements ISubCommand {

    private final DungeonLaunchService launchService;
    private final ThemeRegistry themeRegistry;
    private final DifficultyConfig difficultyConfig;

    public DungeonSoloSubCommand(@NotNull final DungeonLaunchService launchService,
                                 @NotNull final ThemeRegistry themeRegistry,
                                 @NotNull final DifficultyConfig difficultyConfig) {
        this.launchService = launchService;
        this.themeRegistry = themeRegistry;
        this.difficultyConfig = difficultyConfig;
    }

    @Override @NotNull public String getName() { return "solo"; }
    @Override @NotNull public String getPermission() { return "dungeon.use"; }

    @Override
    public void execute(@NotNull final CommandSender sender, @NotNull final String[] args) {
        if (!(sender instanceof final Player player)) {
            MiniMessageUtil.send(sender, "<red>Only players can start a dungeon.");
            return;
        }
        final String theme = args.length > 0 ? args[0] : defaultTheme();
        final String difficulty = args.length > 1 ? args[1] : defaultDifficulty();
        launchService.launchSolo(player, theme, difficulty);
    }

    @Override
    @NotNull
    public List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String[] args) {
        if (args.length == 1) {
            final List<String> out = new ArrayList<>();
            for (final ITheme t : themeRegistry.getAllThemes()) out.add(t.getThemeId());
            return out;
        }
        if (args.length == 2) return new ArrayList<>(difficultyConfig.getPresetIds());
        return List.of();
    }

    @NotNull
    private String defaultTheme() {
        return themeRegistry.getAllThemes().stream().findFirst().map(ITheme::getThemeId).orElse("ancient_ruins");
    }

    @NotNull
    private String defaultDifficulty() {
        return difficultyConfig.getPresetIds().stream().findFirst().orElse("normal");
    }
}
