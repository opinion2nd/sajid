package com.ultimatedungeon.commands;

import com.ultimatedungeon.commands.framework.AbstractCommand;
import com.ultimatedungeon.commands.framework.CommandPermissionChecker;
import com.ultimatedungeon.commands.subcommands.party.*;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Root handler for the {@code /party} command.
 *
 * <p>Registers all party sub-commands and delegates execution accordingly.</p>
 */
public final class PartyCommand extends AbstractCommand {

    public PartyCommand(@NotNull final CommandPermissionChecker permissionChecker) {
        super(permissionChecker);
        register(new PartyCreateSubCommand());
        register(new PartyInviteSubCommand());
        register(new PartyAcceptSubCommand());
        register(new PartyDenySubCommand());
        register(new PartyLeaveSubCommand());
        register(new PartyKickSubCommand());
        register(new PartyTransferSubCommand());
        register(new PartyDisbandSubCommand());
        register(new PartyListSubCommand());
        register(new PartyChatSubCommand());
    }

    @Override
    protected void sendUsage(@NotNull final CommandSender sender) {
        sender.sendMessage("§6Usage: §e/party [create|invite|accept|deny|leave|kick|transfer|disband|list|chat]");
    }
}
