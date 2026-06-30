package com.ultimatedungeon.party.manager;

import com.ultimatedungeon.config.files.PartyConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.party.model.PartyInvitation;
import com.ultimatedungeon.party.service.PartyService;
import com.ultimatedungeon.util.TimeUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages pending party invitations and their expiry.
 *
 * <p>Invitations are stored keyed by the <em>invitee's</em> UUID so O(1)
 * lookup is possible when a player runs {@code /party accept} or {@code /party deny}.
 * A single player may only hold one pending invitation at a time — a new invite
 * from a different party replaces the previous one.</p>
 *
 * <p>Expiry is checked lazily on retrieval and actively via {@link #purgeExpired()},
 * which is called every tick by {@link com.ultimatedungeon.tasks.InvitationExpiryTask}.</p>
 */
public final class InvitationManager {

    private final PartyConfig  partyConfig;
    private final PartyService partyService;
    private final PluginLogger logger;

    /** inviteeUuid → pending invitation */
    private final Map<UUID, PartyInvitation> pending = new ConcurrentHashMap<>();

    public InvitationManager(
            @NotNull final PartyConfig  partyConfig,
            @NotNull final PartyService partyService,
            @NotNull final PluginLogger logger
    ) {
        this.partyConfig  = partyConfig;
        this.partyService = partyService;
        this.logger       = logger;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Creates and stores a new invitation from {@code inviter} to {@code invitee}.
     * Any prior pending invitation for {@code invitee} is silently replaced.
     *
     * @param partyId the party the invitee is being invited to
     * @param inviter the player sending the invite
     * @param invitee the player receiving the invite
     */
    public void createInvitation(
            @NotNull final UUID   partyId,
            @NotNull final Player inviter,
            @NotNull final Player invitee
    ) {
        final long expiresAt = TimeUtil.now()
                + (partyConfig.getInviteTimeoutSeconds() * 1_000L);
        final PartyInvitation invitation = new PartyInvitation(partyId, inviter, invitee, expiresAt);
        pending.put(invitee.getUniqueId(), invitation);
        partyService.notifyInvite(inviter, invitee);
        logger.debug("Invitation created: " + inviter.getName()
                + " → " + invitee.getName()
                + " (expires in " + partyConfig.getInviteTimeoutSeconds() + "s)");
    }

    /**
     * Returns the pending invitation for {@code player}, or {@code null} if none exists
     * or the invitation has expired.
     */
    @Nullable
    public PartyInvitation getPendingInvitation(@NotNull final Player player) {
        final PartyInvitation invitation = pending.get(player.getUniqueId());
        if (invitation == null) return null;
        if (invitation.isExpired()) {
            removeInvitation(player);
            return null;
        }
        return invitation;
    }

    /**
     * Checks whether {@code player} has a pending (non-expired) invitation.
     */
    public boolean hasPendingInvitation(@NotNull final Player player) {
        return getPendingInvitation(player) != null;
    }

    /**
     * Removes the pending invitation for {@code player} without sending any message.
     * Called after accept, deny, or expiry.
     */
    public void removeInvitation(@NotNull final Player player) {
        pending.remove(player.getUniqueId());
    }

    /**
     * Scans all pending invitations and removes expired ones, notifying inviters.
     * Should be called on a scheduled repeating task.
     */
    public void purgeExpired() {
        pending.entrySet().removeIf(entry -> {
            final PartyInvitation inv = entry.getValue();
            if (!inv.isExpired()) return false;
            partyService.notifyInviteExpired(inv.getInviter(), inv.getInvitee());
            logger.debug("Invitation expired: "
                    + inv.getInviter().getName() + " → " + inv.getInvitee().getName());
            return true;
        });
    }

    /** Returns how many invitations are currently pending. */
    public int getPendingCount() {
        return pending.size();
    }

    /** Clears all pending invitations. Called on plugin shutdown. */
    public void clearAll() {
        pending.clear();
    }
}
