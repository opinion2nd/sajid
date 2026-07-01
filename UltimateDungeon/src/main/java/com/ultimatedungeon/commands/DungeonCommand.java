package com.ultimatedungeon.commands;

import com.ultimatedungeon.commands.framework.AbstractCommand;
import com.ultimatedungeon.commands.framework.CommandPermissionChecker;
import com.ultimatedungeon.commands.subcommands.dungeon.*;
import com.ultimatedungeon.config.ConfigManager;
import com.ultimatedungeon.config.files.MessagesConfig;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.dungeon.lifecycle.DungeonLauncher;
import com.ultimatedungeon.gui.framework.GuiServices;
import com.ultimatedungeon.gui.screens.MainMenuGui;
import com.ultimatedungeon.party.manager.PartyManager;
import com.ultimatedungeon.services.DungeonLaunchService;
import com.ultimatedungeon.services.StatisticsService;
import com.ultimatedungeon.theme.registry.ThemeRegistry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Root handler for the {@code /dungeon} command, wiring each sub-command to the
 * services it needs.
 */
public final class DungeonCommand extends AbstractCommand {

    private final GuiServices guiServices;

    public DungeonCommand(@NotNull final CommandPermissionChecker permissionChecker,
                          @NotNull final DungeonLaunchService launchService,
                          @NotNull final DungeonLauncher launcher,
                          @NotNull final DungeonInstanceManager instanceManager,
                          @NotNull final StatisticsService statistics,
                          @NotNull final PartyManager partyManager,
                          @NotNull final ThemeRegistry themeRegistry,
                          @NotNull final ConfigManager configManager,
                          @NotNull final GuiServices guiServices) {
        super(permissionChecker);
        this.guiServices = guiServices;
        final MessagesConfig messages = configManager.getMessagesConfig();
        register(new DungeonSoloSubCommand(launchService, themeRegistry, configManager.getDifficultyConfig()));
        register(new DungeonPartySubCommand(launchService, partyManager, themeRegistry, configManager.getDifficultyConfig()));
        register(new DungeonLeaveSubCommand(launcher, instanceManager, messages));
        register(new DungeonStatsSubCommand(statistics));
        register(new DungeonReloadSubCommand(configManager));
        register(new DungeonAdminSubCommand(instanceManager));
    }

    @Override
    protected void sendUsage(@NotNull final CommandSender sender) {
        if (sender instanceof final Player player) {
            new MainMenuGui(player, guiServices).open();
            return;
        }
        sender.sendMessage("§6Usage: §e/dungeon [solo|party|leave|stats|reload|admin]");
    }
}
