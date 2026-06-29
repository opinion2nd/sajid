package com.ultimatedungeon.commands.subcommands.dungeon;

import com.ultimatedungeon.commands.framework.ISubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/** /dungeon admin — implemented in Milestone 5. */
public final class DungeonAdminSubCommand implements ISubCommand {
    @Override @NotNull public String getName() { return "admin"; }
    @Override @NotNull public String getPermission() { return "dungeon.admin"; }
    @Override public void execute(@NotNull final CommandSender sender, @NotNull final String[] args) {}
    @Override @NotNull public List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String[] args) { return List.of(); }
}
