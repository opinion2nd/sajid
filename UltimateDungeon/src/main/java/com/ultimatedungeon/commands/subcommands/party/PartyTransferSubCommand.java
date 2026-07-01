package com.ultimatedungeon.commands.subcommands.party;

import com.ultimatedungeon.commands.framework.ISubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/** /party transfer — implemented in Milestone 5. */
public final class PartyTransferSubCommand implements ISubCommand {
    @Override @NotNull public String getName() { return "transfer"; }
    @Override @NotNull public String getPermission() { return "dungeon.party"; }
    @Override public void execute(@NotNull final CommandSender sender, @NotNull final String[] args) {}
    @Override @NotNull public List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String[] args) { return List.of(); }
}
