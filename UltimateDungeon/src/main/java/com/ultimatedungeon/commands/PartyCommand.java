package com.ultimatedungeon.commands;

import com.ultimatedungeon.config.files.MessagesConfig;
import com.ultimatedungeon.party.manager.PartyManager;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Executor for the {@code /party} command and all its sub-commands.
 *
 * <p>Sub-commands are delegated to the {@link PartyManager} after permission
 * and player-only checks are applied here. No game logic lives in this class.</p>
 *
 * <h3>Sub-commands</h3>
 * <ul>
 *   <li>{@code /party create}</li>
 *   <li>{@code /party invite <player>}</li>
 *   <li>{@code /party accept}</li>
 *   <li>{@code /party deny}</li>
 *   <li>{@code /party leave}</li>
 *   <li>{@code /party kick <player>}</li>
 *   <li>{@code /party transfer <player>}</li>
 *   <li>{@code /party disband}</li>
 *   <li>{@code /party list}</li>
 *   <li>{@code /party chat <message...>}</li>
 * </ul>
 */
public final class PartyCommand implements CommandExecutor, TabCompleter {

    private static final String PERM_BASE  = "dungeon.party";
    private static final String PERM_ADMIN = "dungeon.admin";

    private final PartyManager   partyManager;
    private final MessagesConfig messages;

    public PartyCommand(
            @NotNull final PartyManager   partyManager,
            @NotNull final MessagesConfig messages
    ) {
        this.partyManager = partyManager;
        this.messages     = messages;
    }

    // ── CommandExecutor ───────────────────────────────────────────────────────

    @Override
    public boolean onCommand(
            @NotNull  final CommandSender sender,
            @NotNull  final Command       command,
            @NotNull  final String        label,
            @NotNull  final String[]      args
    ) {
        if (!(sender instanceof final Player player)) {
            MiniMessageUtil.send(sender, messages.getPrefix() + messages.getPlayerOnly());
            return true;
        }
        if (!player.hasPermission(PERM_BASE)) {
            MiniMessageUtil.send(player, messages.getPrefix() + messages.getNoPermission());
            return true;
        }
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create"   -> handleCreate(player);
            case "invite"   -> handleInvite(player, args);
            case "accept"   -> handleAccept(player);
            case "deny"     -> handleDeny(player);
            case "leave"    -> handleLeave(player);
            case "kick"     -> handleKick(player, args);
            case "transfer" -> handleTransfer(player, args);
            case "disband"  -> handleDisband(player);
            case "list"     -> handleList(player);
            case "chat"     -> handleChat(player, args);
            default         -> MiniMessageUtil.send(player,
                    messages.getPrefix() + messages.getUnknownSubCommand());
        }
        return true;
    }

    // ── Sub-command handlers ──────────────────────────────────────────────────

    private void handleCreate(@NotNull final Player player) {
        try {
            partyManager.createParty(player);
        } catch (final IllegalStateException ignored) {
            // PartyManager already sent the failure message.
        }
    }

    private void handleInvite(@NotNull final Player player, @NotNull final String[] args) {
        if (args.length < 2) { sendUsage(player, "/party invite <player>"); return; }
        final Player target = player.getServer().getPlayer(args[1]);
        if (target == null) {
            MiniMessageUtil.send(player, messages.getPrefix()
                    + "<red>Player <yellow>" + args[1] + "</yellow> is not online.");
            return;
        }
        partyManager.invitePlayer(player, target);
    }

    private void handleAccept(@NotNull final Player player) {
        partyManager.acceptInvitation(player);
    }

    private void handleDeny(@NotNull final Player player) {
        partyManager.denyInvitation(player);
    }

    private void handleLeave(@NotNull final Player player) {
        partyManager.leaveParty(player);
    }

    private void handleKick(@NotNull final Player player, @NotNull final String[] args) {
        if (args.length < 2) { sendUsage(player, "/party kick <player>"); return; }
        final Player target = player.getServer().getPlayer(args[1]);
        if (target == null) {
            MiniMessageUtil.send(player, messages.getPrefix()
                    + "<red>Player <yellow>" + args[1] + "</yellow> is not online.");
            return;
        }
        partyManager.kickPlayer(player, target);
    }

    private void handleTransfer(@NotNull final Player player, @NotNull final String[] args) {
        if (args.length < 2) { sendUsage(player, "/party transfer <player>"); return; }
        final Player target = player.getServer().getPlayer(args[1]);
        if (target == null) {
            MiniMessageUtil.send(player, messages.getPrefix()
                    + "<red>Player <yellow>" + args[1] + "</yellow> is not online.");
            return;
        }
        partyManager.transferLeadership(player, target);
    }

    private void handleDisband(@NotNull final Player player) {
        final var party = partyManager.getPartyForPlayer(player);
        if (party == null) {
            MiniMessageUtil.send(player, messages.getPrefix() + messages.getPartyNotIn());
            return;
        }
        if (!party.isLeader(player)) {
            MiniMessageUtil.send(player, messages.getPrefix() + messages.getPartyNotLeader());
            return;
        }
        partyManager.disbandParty(party.getPartyId());
    }

    private void handleList(@NotNull final Player player) {
        partyManager.listParty(player);
    }

    private void handleChat(@NotNull final Player player, @NotNull final String[] args) {
        if (args.length < 2) { sendUsage(player, "/party chat <message>"); return; }
        final String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        partyManager.sendPartyChat(player, message);
    }

    // ── TabCompleter ──────────────────────────────────────────────────────────

    @Override
    @Nullable
    public List<String> onTabComplete(
            @NotNull  final CommandSender sender,
            @NotNull  final Command       command,
            @NotNull  final String        label,
            @NotNull  final String[]      args
    ) {
        if (!(sender instanceof Player)) return List.of();

        if (args.length == 1) {
            return List.of("create", "invite", "accept", "deny", "leave",
                           "kick", "transfer", "disband", "list", "chat");
        }
        if (args.length == 2) {
            final String sub = args[0].toLowerCase();
            if (sub.equals("invite") || sub.equals("kick") || sub.equals("transfer")) {
                return sender.getServer().getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .toList();
            }
        }
        return List.of();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void sendHelp(@NotNull final Player player) {
        MiniMessageUtil.send(player, messages.getPrefix()
                + "<gold>Party Commands:");
        final String[][] cmds = {
            {"/party create",           "Create a new party"},
            {"/party invite <player>",  "Invite a player"},
            {"/party accept",           "Accept an invitation"},
            {"/party deny",             "Deny an invitation"},
            {"/party leave",            "Leave your party"},
            {"/party kick <player>",    "Kick a member (leader)"},
            {"/party transfer <player>","Transfer leadership (leader)"},
            {"/party disband",          "Disband the party (leader)"},
            {"/party list",             "List party members"},
            {"/party chat <msg>",       "Send a party message"},
        };
        for (final String[] entry : cmds) {
            MiniMessageUtil.send(player,
                    "  <yellow>" + entry[0] + " <gray>— " + entry[1]);
        }
    }

    private void sendUsage(@NotNull final Player player, @NotNull final String usage) {
        MiniMessageUtil.send(player, messages.getPrefix()
                + "<red>Usage: <yellow>" + usage);
    }
}
