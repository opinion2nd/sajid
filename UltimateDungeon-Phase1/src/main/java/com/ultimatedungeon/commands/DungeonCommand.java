package com.ultimatedungeon.commands;

import com.ultimatedungeon.commands.framework.AbstractCommand;
import com.ultimatedungeon.commands.framework.CommandPermissionChecker;
import com.ultimatedungeon.commands.subcommands.dungeon.*;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Root handler for the {@code /dungeon} command.
 *
 * <p>Registers all dungeon sub-commands and delegates execution to the
 * appropriate {@link com.ultimatedungeon.commands.framework.ISubCommand}.</p>
 */
public final class DungeonCommand extends AbstractCommand {

    public DungeonCommand(@NotNull final CommandPermissionChecker permissionChecker) {
        super(permissionChecker);
        register(new DungeonSoloSubCommand());
        register(new DungeonPartySubCommand());
        register(new DungeonLeaveSubCommand());
        register(new DungeonStatsSubCommand());
        register(new DungeonReloadSubCommand());
        register(new DungeonAdminSubCommand());
    }

    @Override
    protected void sendUsage(@NotNull final CommandSender sender) {
        sender.sendMessage("§6Usage: §e/dungeon [solo|party|leave|stats|reload|admin]");
    }
}
