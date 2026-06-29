package com.ultimatedungeon.commands.subcommands.party;

import com.ultimatedungeon.commands.framework.ISubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/** /party leave — implemented in Milestone 5. */
public final class PartyLeaveSubCommand implements ISubCommand {
    @Override @NotNull public String getName() { return "leave"; }
    @Override @NotNull public String getPermission() { return "dungeon.party"; }
    @Override public void execute(@NotNull final CommandSender sender, @NotNull final String[] args) {}
    @Override @NotNull public List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String[] args) { return List.of(); }
}
