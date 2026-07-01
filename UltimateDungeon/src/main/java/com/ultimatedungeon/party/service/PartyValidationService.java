package com.ultimatedungeon.party.service;

import com.ultimatedungeon.config.files.PartyConfig;
import com.ultimatedungeon.managers.PlayerSessionManager;
import com.ultimatedungeon.party.model.Party;
import com.ultimatedungeon.party.model.PartyState;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Stateless validation service for party operations.
 *
 * <p>Every method returns a {@link ValidationResult} — a sealed type carrying
 * either success or a descriptive failure reason. Callers translate the reason
 * to the appropriate player-facing message from {@code messages.yml}.</p>
 */
public final class PartyValidationService {

    private final PartyConfig          partyConfig;
    private final PlayerSessionManager sessionManager;

    public PartyValidationService(
            @NotNull final PartyConfig          partyConfig,
            @NotNull final PlayerSessionManager sessionManager
    ) {
        this.partyConfig    = partyConfig;
        this.sessionManager = sessionManager;
    }

    // ── Validation results ────────────────────────────────────────────────────

    public sealed interface ValidationResult permits ValidationResult.Ok, ValidationResult.Fail {
        record Ok()                          implements ValidationResult {}
        record Fail(@NotNull String reason)  implements ValidationResult {}

        static ValidationResult ok()                        { return new Ok(); }
        static ValidationResult fail(@NotNull String reason){ return new Fail(reason); }
        default boolean isOk()   { return this instanceof Ok;   }
        default boolean isFail() { return this instanceof Fail; }
    }

    // ── Create ────────────────────────────────────────────────────────────────

    /**
     * Checks whether {@code player} may create a new party.
     *
     * @param existingParty the party this player already belongs to, or {@code null}
     */
    @NotNull
    public ValidationResult canCreate(@NotNull final Player player, final Party existingParty) {
        if (existingParty != null) return ValidationResult.fail("already-in-party");
        return ValidationResult.ok();
    }

    // ── Invite ────────────────────────────────────────────────────────────────

    /**
     * Checks whether {@code leader} may invite {@code target} to {@code party}.
     */
    @NotNull
    public ValidationResult canInvite(
            @NotNull final Player leader,
            @NotNull final Player target,
            @NotNull final Party  party,
            final Party           targetExistingParty
    ) {
        if (!party.isLeader(leader))         return ValidationResult.fail("not-leader");
        if (party.isFull())                  return ValidationResult.fail("party-full");
        if (leader.equals(target))           return ValidationResult.fail("cannot-invite-self");
        if (targetExistingParty != null)     return ValidationResult.fail("target-already-in-party");
        if (!target.isOnline())              return ValidationResult.fail("target-not-online");
        if (party.getState() == PartyState.IN_DUNGEON && !partyConfig.isLateJoin())
                                             return ValidationResult.fail("dungeon-in-progress");
        return ValidationResult.ok();
    }

    // ── Accept ────────────────────────────────────────────────────────────────

    /**
     * Checks whether {@code player} may accept an invitation to {@code party}.
     */
    @NotNull
    public ValidationResult canAccept(
            @NotNull final Player player,
            @NotNull final Party  party,
            final Party           playerExistingParty
    ) {
        if (playerExistingParty != null) return ValidationResult.fail("already-in-party");
        if (party.isFull())              return ValidationResult.fail("party-full");
        if (party.getState() == PartyState.IN_DUNGEON && !partyConfig.isLateJoin())
                                         return ValidationResult.fail("dungeon-in-progress");
        return ValidationResult.ok();
    }

    // ── Kick ──────────────────────────────────────────────────────────────────

    @NotNull
    public ValidationResult canKick(
            @NotNull final Player leader,
            @NotNull final Player target,
            @NotNull final Party  party
    ) {
        if (!party.isLeader(leader)) return ValidationResult.fail("not-leader");
        if (!party.isMember(target)) return ValidationResult.fail("target-not-in-party");
        if (leader.equals(target))   return ValidationResult.fail("cannot-kick-self");
        return ValidationResult.ok();
    }

    // ── Transfer ──────────────────────────────────────────────────────────────

    @NotNull
    public ValidationResult canTransfer(
            @NotNull final Player leader,
            @NotNull final Player target,
            @NotNull final Party  party
    ) {
        if (!party.isLeader(leader)) return ValidationResult.fail("not-leader");
        if (!party.isMember(target)) return ValidationResult.fail("target-not-in-party");
        if (leader.equals(target))   return ValidationResult.fail("already-leader");
        return ValidationResult.ok();
    }

    // ── Leave ─────────────────────────────────────────────────────────────────

    @NotNull
    public ValidationResult canLeave(@NotNull final Player player, final Party party) {
        if (party == null)             return ValidationResult.fail("not-in-party");
        if (!party.isMember(player))   return ValidationResult.fail("not-in-party");
        return ValidationResult.ok();
    }

    // ── Ready check ───────────────────────────────────────────────────────────

    @NotNull
    public ValidationResult canStartReadyCheck(
            @NotNull final Player leader,
            final Party           party
    ) {
        if (party == null)               return ValidationResult.fail("not-in-party");
        if (!party.isLeader(leader))     return ValidationResult.fail("not-leader");
        if (party.getSize() < 1)         return ValidationResult.fail("party-empty");
        if (party.getState() == PartyState.READY_CHECK)
                                         return ValidationResult.fail("ready-check-already-active");
        if (party.getState() == PartyState.IN_DUNGEON)
                                         return ValidationResult.fail("already-in-dungeon");
        // Validate all members are online
        for (final org.bukkit.entity.Player m : party.getMembers()) {
            if (!m.isOnline()) return ValidationResult.fail("member-offline");
        }
        return ValidationResult.ok();
    }
}
