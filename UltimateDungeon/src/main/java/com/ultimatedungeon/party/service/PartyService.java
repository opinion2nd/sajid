package com.ultimatedungeon.party.service;

import com.ultimatedungeon.config.files.MessagesConfig;
import com.ultimatedungeon.config.files.PartyConfig;
import com.ultimatedungeon.config.files.PartyConfig.LeaderTransferMode;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.events.party.*;
import com.ultimatedungeon.party.model.Party;
import com.ultimatedungeon.party.model.PartyMember;
import com.ultimatedungeon.party.model.PartyState;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Stateless service containing all party mutation logic.
 *
 * <p>Every method is called by {@link com.ultimatedungeon.party.manager.PartyManager}
 * after validation has already passed. This class performs the state change,
 * fires the appropriate Bukkit event, and sends MiniMessage notifications.</p>
 *
 * <p>No scheduling or async work happens here — that belongs to the managers
 * that call this service.</p>
 */
public final class PartyService {

    private final MessagesConfig messages;
    private final PluginLogger   logger;
    private final Server         server;

    public PartyService(
            @NotNull final MessagesConfig messages,
            @NotNull final PluginLogger   logger,
            @NotNull final Server         server
    ) {
        this.messages = messages;
        this.logger   = logger;
        this.server   = server;
    }

    // ── Create / disband ──────────────────────────────────────────────────────

    /**
     * Fires {@link PartyCreateEvent}. If not cancelled, notifies the leader.
     *
     * @return {@code true} if the event was not cancelled and creation should proceed
     */
    public boolean fireCreate(@NotNull final Player leader) {
        final PartyCreateEvent event = new PartyCreateEvent(leader);
        server.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;
        MiniMessageUtil.send(leader, messages.getPrefix() + messages.getPartyCreated());
        logger.debug("PartyCreateEvent fired for: " + leader.getName());
        return true;
    }

    /**
     * Notifies all members of a disband, fires {@link PartyDisbandEvent}.
     */
    public void handleDisband(@NotNull final Party party, @NotNull final String reason) {
        for (final Player member : party.getMembers()) {
            if (member.isOnline()) {
                MiniMessageUtil.send(member, messages.getPrefix() + messages.getPartyDisbanded());
            }
        }
        server.getPluginManager().callEvent(new PartyDisbandEvent(party, reason));
        logger.debug("Party disbanded [" + party.getPartyId() + "] reason=" + reason);
    }

    // ── Invite ────────────────────────────────────────────────────────────────

    /**
     * Sends the invitation notification to both parties.
     */
    public void notifyInvite(@NotNull final Player inviter, @NotNull final Player invitee) {
        MiniMessageUtil.send(inviter,
                messages.getPrefix() + messages.getPartyInvited(),
                Map.of("player", invitee.getName()));
        MiniMessageUtil.send(invitee,
                messages.getPrefix() + messages.getPartyInviteReceived(),
                Map.of("player", inviter.getName()));
        logger.debug(inviter.getName() + " invited " + invitee.getName());
    }

    /**
     * Notifies the inviter that the invitation expired without a response.
     */
    public void notifyInviteExpired(@NotNull final Player inviter, @NotNull final Player invitee) {
        if (inviter.isOnline()) {
            MiniMessageUtil.send(inviter,
                    messages.getPrefix() + messages.getPartyInviteExpired(),
                    Map.of("player", invitee.getName()));
        }
    }

    // ── Join ──────────────────────────────────────────────────────────────────

    /**
     * Adds {@code player} to {@code party}, fires {@link PartyMemberJoinEvent},
     * and broadcasts the join message to all members.
     *
     * @return {@code true} if the event was not cancelled
     */
    public boolean handleJoin(@NotNull final Party party, @NotNull final Player player) {
        final PartyMemberJoinEvent event = new PartyMemberJoinEvent(party, player);
        server.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        party.addMember(player);
        broadcastToParty(party,
                messages.getPrefix() + messages.getPartyJoined(),
                Map.of("player", player.getName()));
        logger.debug(player.getName() + " joined party [" + party.getPartyId() + "]");
        return true;
    }

    // ── Leave ─────────────────────────────────────────────────────────────────

    /**
     * Removes {@code player} from {@code party}, fires {@link PartyMemberLeaveEvent},
     * broadcasts departure, and transfers/disbands if the leader left.
     *
     * @param party      the party being left
     * @param player     the player leaving
     * @param reason     the leave reason
     * @param transferMode how to handle leader departure
     * @return {@code true} if the party should now be disbanded (caller disbands)
     */
    public boolean handleLeave(
            @NotNull final Party  party,
            @NotNull final Player player,
            @NotNull final PartyMemberLeaveEvent.Reason reason,
            @NotNull final LeaderTransferMode            transferMode
    ) {
        final boolean wasLeader = party.isLeader(player);
        party.removeMember(player);

        server.getPluginManager().callEvent(
                new PartyMemberLeaveEvent(party, player, reason));

        if (reason != PartyMemberLeaveEvent.Reason.DISBAND) {
            broadcastToParty(party,
                    messages.getPrefix() + messages.getPartyLeft(),
                    Map.of("player", player.getName()));
        }

        // If leader left, apply transfer mode
        if (wasLeader && party.getSize() > 0) {
            return switch (transferMode) {
                case AUTO    -> { autoTransferLeadership(party); yield false; }
                case DISBAND -> true; // caller will disband
                case MANUAL  -> {
                    // Freeze party until someone manually transfers — for now auto-transfer
                    autoTransferLeadership(party);
                    yield false;
                }
            };
        }
        // Disband if empty
        return party.getSize() == 0;
    }

    // ── Kick ──────────────────────────────────────────────────────────────────

    public void handleKick(@NotNull final Party party, @NotNull final Player target) {
        party.removeMember(target);
        server.getPluginManager().callEvent(
                new PartyMemberLeaveEvent(party, target, PartyMemberLeaveEvent.Reason.KICKED));
        if (target.isOnline()) {
            MiniMessageUtil.send(target,
                    messages.getPrefix() + messages.getPartyKicked(),
                    Map.of("player", target.getName()));
        }
        broadcastToParty(party,
                messages.getPrefix() + messages.getPartyKicked(),
                Map.of("player", target.getName()));
        logger.debug(target.getName() + " kicked from party [" + party.getPartyId() + "]");
    }

    // ── Transfer ──────────────────────────────────────────────────────────────

    public void handleTransfer(
            @NotNull final Party  party,
            @NotNull final Player oldLeader,
            @NotNull final Player newLeader
    ) {
        party.transferLeadership(newLeader.getUniqueId());
        server.getPluginManager().callEvent(
                new PartyLeaderChangeEvent(party, oldLeader, newLeader));
        broadcastToParty(party,
                messages.getPrefix() + messages.getPartyLeaderTransferred(),
                Map.of("player", newLeader.getName()));
        logger.debug("Leadership transferred: " + oldLeader.getName()
                + " → " + newLeader.getName());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void autoTransferLeadership(@NotNull final Party party) {
        // Pick the member who joined earliest (first in CopyOnWriteArrayList)
        party.getMembers().stream()
                .findFirst()
                .ifPresent(newLeader -> {
                    final Player oldLeaderProxy = party.getMembers().stream()
                            .filter(p -> p.getUniqueId().equals(party.getLeader().getUniqueId()))
                            .findFirst().orElse(newLeader);
                    handleTransfer(party, oldLeaderProxy, newLeader);
                });
    }

    /**
     * Broadcasts a MiniMessage string with placeholders to every online party member.
     */
    public void broadcastToParty(
            @NotNull final Party              party,
            @NotNull final String             rawMessage,
            @NotNull final Map<String,String> placeholders
    ) {
        for (final Player member : party.getMembers()) {
            if (member.isOnline()) {
                MiniMessageUtil.send(member, rawMessage, placeholders);
            }
        }
    }

    /** Broadcasts with no placeholders. */
    public void broadcastToParty(@NotNull final Party party, @NotNull final String rawMessage) {
        broadcastToParty(party, rawMessage, Map.of());
    }
}
