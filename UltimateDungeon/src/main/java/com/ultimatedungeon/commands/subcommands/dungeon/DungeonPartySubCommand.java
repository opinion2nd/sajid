package com.ultimatedungeon.commands.subcommands.dungeon;

import com.ultimatedungeon.commands.framework.ISubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/** /dungeon party — implemented in Milestone 5. */
public final class DungeonPartySubCommand implements ISubCommand {
    @Override @NotNull public String getName() { return "party"; }
    @Override @NotNull public String getPermission() { return "dungeon.use"; }
    @Override public void execute(@NotNull final CommandSender sender, @NotNull final String[] args) {}
    @Override @NotNull public List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String[] args) { return List.of(); }
}
