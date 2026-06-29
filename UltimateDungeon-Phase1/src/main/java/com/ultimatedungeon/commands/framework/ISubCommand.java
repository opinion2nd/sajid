package com.ultimatedungeon.commands.framework;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/** Contract for all sub-command implementations. */
public interface ISubCommand {
    @NotNull String getName();
    @NotNull String getPermission();
    void execute(@NotNull CommandSender sender, @NotNull String[] args);
    @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args);
}
