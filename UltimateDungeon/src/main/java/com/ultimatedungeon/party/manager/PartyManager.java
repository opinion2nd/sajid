package com.ultimatedungeon.party.manager;

import com.ultimatedungeon.api.party.IParty;
import com.ultimatedungeon.api.party.IPartyManager;
import com.ultimatedungeon.config.files.MessagesConfig;
import com.ultimatedungeon.config.files.PartyConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.core.PluginScheduler;
import com.ultimatedungeon.party.model.Party;
import com.ultimatedungeon.party.model.PartyState;
import com.ultimatedungeon.party.service.PartyService;
import com.ultimatedungeon.party.service.PartyValidationService;
import com.ultimatedungeon.party.service.PartyValidationService.ValidationResult;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Central stateful manager for all party operations.
 *
 * <p>This is the single entry-point for all party commands and other systems
 * that need to interact with parties. It owns two sub-managers
 * ({@link InvitationManager}, {@link ReadyCheckManager}) and delegates mutation
 * logic to {@link PartyService} and validation to
 * {@link PartyValidationService}.</p>
 *
 * <h3>State maps</h3>
 * <ul>
 *   <li>{@code parties}   — partyId → Party</li>
 *   <li>{@code playerMap} — playerUuid → partyId (reverse index for O(1) lookup)</li>
 * </ul>
 *
 * <h3>Thread safety</h3>
 * All maps are {@link ConcurrentHashMap}. All public methods are called from
 * the main server thread (commands, listeners). The concurrent maps guard
 * against async read access from periodic tasks.
 */
public final class PartyManager implements IPartyManager {

    private final PartyConfig             partyConfig;
    private final MessagesConfig          messages;
    private final PartyService            partyService;
    private final PartyValidationService  validationService;
    private final InvitationManager       invitationManager;
    private final ReadyCheckManager       readyCheckManager;
    private final PluginLogger            logger;

    /** partyId → Party */
    private final Map<UUID, Party> parties   = new ConcurrentHashMap<>();
    /** playerUuid → partyId */
    private final Map<UUID, UUID>  playerMap = new ConcurrentHashMap<>();

    public PartyManager(
            @NotNull final PartyConfig            partyConfig,
            @NotNull final MessagesConfig         messages,
            @NotNull final PartyService           partyService,
            @NotNull final PartyValidationService validationService,
            @NotNull final InvitationManager      invitationManager,
            @NotNull final ReadyCheckManager      readyCheckManager,
            @NotNull final PluginLogger           logger
    ) {
        this.partyConfig       = partyConfig;
        this.messages          = messages;
        this.partyService      = partyService;
        this.validationService = validationService;
        this.invitationManager = invitationManager;
        this.readyCheckManager = readyCheckManager;
        this.logger            = logger;
    }

    // ── IPartyManager ─────────────────────────────────────────────────────────

    @Override
    @NotNull
    public IParty createParty(@NotNull final Player leader) {
        final Party existing = getPartyForPlayerRaw(leader);
        final ValidationResult result = validationService.canCreate(leader, existing);
        if (result.isFail()) {
            sendFailure(leader, ((ValidationResult.Fail) result).reason());
            throw new IllegalStateException("Party creation blocked: "
                    + ((ValidationResult.Fail) result).reason());
        }
        if (!partyService.fireCreate(leader)) {
            throw new IllegalStateException("PartyCreateEvent was cancelled.");
        }
        final UUID partyId = UUID.randomUUID();
        final Party party  = new Party(partyId, leader, partyConfig.getMaxPartySize());
        parties.put(partyId, party);
        playerMap.put(leader.getUniqueId(), partyId);
        logger.debug("Party created: " + partyId + " leader=" + leader.getName());
        return party;
    }

    @Override
    public void disbandParty(@NotNull final UUID partyId) {
        final Party party = parties.get(partyId);
        if (party == null) return;
        readyCheckManager.cancelSession(party);
        party.getMembers().forEach(m -> playerMap.remove(m.getUniqueId()));
        parties.remove(partyId);
        partyService.handleDisband(party, "manual");
    }

    @Override
    @Nullable
    public IParty getParty(@NotNull final UUID partyId) {
        return parties.get(partyId);
    }

    @Override
    @Nullable
    public IParty getPartyForPlayer(@NotNull final Player player) {
        return getPartyForPlayerRaw(player);
    }

    @Override
    @NotNull
    public Collection<IParty> getAllParties() {
        return Collections.unmodifiableCollection(parties.values());
    }

    @Override
    public boolean isInParty(@NotNull final Player player) {
        return playerMap.containsKey(player.getUniqueId());
    }

    // ── Invite / Accept / Deny ────────────────────────────────────────────────

    /**
     * Sends an invitation from {@code inviter} to {@code target}.
     * Performs full validation before creating the invitation.
     */
    public void invitePlayer(@NotNull final Player inviter, @NotNull final Player target) {
        final Party party    = getPartyForPlayerRaw(inviter);
        final Party existing = getPartyForPlayerRaw(target);

        if (party == null) { sendFailure(inviter, "not-in-party"); return; }

        final ValidationResult r = validationService.canInvite(inviter, target, party, existing);
        if (r.isFail()) { sendFailure(inviter, ((ValidationResult.Fail) r).reason()); return; }

        invitationManager.createInvitation(party.getPartyId(), inviter, target);
    }

    /**
     * Accepts a pending invitation for {@code player}.
     */
    public void acceptInvitation(@NotNull final Player player) {
        final var invitation = invitationManager.getPendingInvitation(player);
        if (invitation == null) { sendFailure(player, "no-invite"); return; }

        final Party party    = parties.get(invitation.getPartyId());
        final Party existing = getPartyForPlayerRaw(player);

        if (party == null) {
            invitationManager.removeInvitation(player);
            sendFailure(player, "party-no-longer-exists");
            return;
        }

        final ValidationResult r = validationService.canAccept(player, party, existing);
        if (r.isFail()) { sendFailure(player, ((ValidationResult.Fail) r).reason()); return; }

        invitationManager.removeInvitation(player);
        if (partyService.handleJoin(party, player)) {
            playerMap.put(player.getUniqueId(), party.getPartyId());
        }
    }

    /**
     * Denies a pending invitation for {@code player}.
     */
    public void denyInvitation(@NotNull final Player player) {
        final var invitation = invitationManager.getPendingInvitation(player);
        if (invitation == null) { sendFailure(player, "no-invite"); return; }
        invitationManager.removeInvitation(player);
        MiniMessageUtil.send(player, messages.getPrefix()
                + "<yellow>Invitation declined.");
        if (invitation.getInviter().isOnline()) {
            MiniMessageUtil.send(invitation.getInviter(), messages.getPrefix()
                    + "<red>" + player.getName() + " declined your invitation.");
        }
    }

    // ── Leave / Kick / Transfer / Disband ─────────────────────────────────────

    public void leaveParty(@NotNull final Player player) {
        final Party party = getPartyForPlayerRaw(player);
        final ValidationResult r = validationService.canLeave(player, party);
        if (r.isFail()) { sendFailure(player, ((ValidationResult.Fail) r).reason()); return; }

        playerMap.remove(player.getUniqueId());
        final boolean shouldDisband = partyService.handleLeave(
                party, player,
                com.ultimatedungeon.events.party.PartyMemberLeaveEvent.Reason.VOLUNTARY,
                partyConfig.getLeaderTransferMode()
        );
        if (shouldDisband) disbandParty(party.getPartyId());
    }

    public void kickPlayer(@NotNull final Player leader, @NotNull final Player target) {
        final Party party = getPartyForPlayerRaw(leader);
        if (party == null) { sendFailure(leader, "not-in-party"); return; }

        final ValidationResult r = validationService.canKick(leader, target, party);
        if (r.isFail()) { sendFailure(leader, ((ValidationResult.Fail) r).reason()); return; }

        playerMap.remove(target.getUniqueId());
        partyService.handleKick(party, target);
    }

    public void transferLeadership(@NotNull final Player leader, @NotNull final Player target) {
        final Party party = getPartyForPlayerRaw(leader);
        if (party == null) { sendFailure(leader, "not-in-party"); return; }

        final ValidationResult r = validationService.canTransfer(leader, target, party);
        if (r.isFail()) { sendFailure(leader, ((ValidationResult.Fail) r).reason()); return; }

        partyService.handleTransfer(party, leader, target);
    }

    // ── Ready check ───────────────────────────────────────────────────────────

    /**
     * Starts a ready check for the party the leader belongs to.
     *
     * @param leader       the player initiating the ready check
     * @param onAllReady   callback to invoke when all members are ready
     */
    public void startReadyCheck(
            @NotNull final Player          leader,
            @NotNull final Consumer<Party> onAllReady
    ) {
        final Party party = getPartyForPlayerRaw(leader);
        final ValidationResult r = validationService.canStartReadyCheck(leader, party);
        if (r.isFail()) { sendFailure(leader, ((ValidationResult.Fail) r).reason()); return; }
        readyCheckManager.startReadyCheck(party, onAllReady);
    }

    /**
     * Records a ready-check response from {@code player}.
     */
    public void respondToReadyCheck(
            @NotNull final Player          player,
            final boolean                  ready,
            @NotNull final Consumer<Party> onAllReady
    ) {
        final Party party = getPartyForPlayerRaw(player);
        if (party == null || party.getState() != PartyState.READY_CHECK) return;
        readyCheckManager.recordResponse(party, player, ready, onAllReady);
    }

    // ── Party chat ────────────────────────────────────────────────────────────

    /**
     * Sends a party-chat message from {@code sender} to all online party members.
     */
    public void sendPartyChat(@NotNull final Player sender, @NotNull final String message) {
        final Party party = getPartyForPlayerRaw(sender);
        if (party == null) { sendFailure(sender, "not-in-party"); return; }
        final String formatted = messages.getPrefix()
                + "<gray>[Party] <white>" + sender.getName() + ": " + message;
        partyService.broadcastToParty(party, formatted);
    }

    // ── Disconnect handling ───────────────────────────────────────────────────

    /**
     * Called when a player disconnects. If they are in a party and it was in
     * FORMING state, removes them. If IN_DUNGEON, the party continues without them
     * (reconnect window handled by PlayerSessionManager).
     */
    public void handleDisconnect(@NotNull final Player player) {
        final Party party = getPartyForPlayerRaw(player);
        if (party == null) return;

        if (party.getState() != PartyState.IN_DUNGEON) {
            playerMap.remove(player.getUniqueId());
            final boolean shouldDisband = partyService.handleLeave(
                    party, player,
                    com.ultimatedungeon.events.party.PartyMemberLeaveEvent.Reason.DISCONNECT,
                    partyConfig.getLeaderTransferMode()
            );
            if (shouldDisband) disbandParty(party.getPartyId());
        }
        // In-dungeon disconnect: leave the party record intact for reconnect
    }

    // ── Listing ───────────────────────────────────────────────────────────────

    /**
     * Sends a formatted party member list to {@code player}.
     */
    public void listParty(@NotNull final Player player) {
        final Party party = getPartyForPlayerRaw(player);
        if (party == null) { sendFailure(player, "not-in-party"); return; }

        MiniMessageUtil.send(player, messages.getPrefix()
                + "<gold>Party members <gray>(" + party.getSize() + "/" + partyConfig.getMaxPartySize() + "):");
        for (final Player m : party.getMembers()) {
            final boolean isLeader = party.isLeader(m);
            final String status    = m.isOnline() ? "<green>●" : "<red>●";
            final String crown     = isLeader ? " <gold>★" : "";
            MiniMessageUtil.send(player,
                    "  " + status + " <white>" + m.getName() + crown);
        }
    }

    // ── Shutdown ──────────────────────────────────────────────────────────────

    /** Disbands all parties and clears all state. Called on plugin shutdown. */
    public void shutdown() {
        invitationManager.clearAll();
        readyCheckManager.clearAll();
        new HashSet<>(parties.keySet()).forEach(this::disbandParty);
        parties.clear();
        playerMap.clear();
        logger.debug("PartyManager shut down — all parties cleared.");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    @Nullable
    private Party getPartyForPlayerRaw(@NotNull final Player player) {
        final UUID partyId = playerMap.get(player.getUniqueId());
        return partyId != null ? parties.get(partyId) : null;
    }

    /** Maps a failure reason key to the appropriate message and sends it to the player. */
    private void sendFailure(@NotNull final Player player, @NotNull final String reason) {
        final String msg = switch (reason) {
            case "already-in-party"       -> messages.getPartyAlreadyIn();
            case "not-in-party"           -> messages.getPartyNotIn();
            case "not-leader"             -> messages.getPartyNotLeader();
            case "party-full"             -> messages.getPartyFull();
            case "no-invite"              -> messages.getPartyNoInvite();
            case "already-in-dungeon"     -> messages.getDungeonAlreadyIn();
            case "ready-check-already-active" -> messages.getPartyReadyCheckStarted();
            default -> "<red>Action not permitted: " + reason;
        };
        MiniMessageUtil.send(player, messages.getPrefix() + msg);
    }
}
