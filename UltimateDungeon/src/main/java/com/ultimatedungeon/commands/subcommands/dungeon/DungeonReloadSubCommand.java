package com.ultimatedungeon.commands.subcommands.dungeon;

import com.ultimatedungeon.commands.framework.ISubCommand;
import com.ultimatedungeon.config.ConfigManager;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** {@code /dungeon reload} — reloads all configuration files. */
public final class DungeonReloadSubCommand implements ISubCommand {

    private final ConfigManager configManager;

    public DungeonReloadSubCommand(@NotNull final ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override @NotNull public String getName() { return "reload"; }
    @Override @NotNull public String getPermission() { return "dungeon.reload"; }

    @Override
    public void execute(@NotNull final CommandSender sender, @NotNull final String[] args) {
        try {
            configManager.reload();
            MiniMessageUtil.send(sender, "<green>UltimateDungeon configuration reloaded.");
        } catch (final Exception e) {
            MiniMessageUtil.send(sender, "<red>Reload failed: " + e.getMessage());
        }
    }

    @Override @NotNull
    public List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String[] args) {
        return List.of();
    }
}
