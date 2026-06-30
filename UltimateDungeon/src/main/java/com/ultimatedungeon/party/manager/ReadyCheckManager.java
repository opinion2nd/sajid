package com.ultimatedungeon.party.manager;

import com.ultimatedungeon.config.files.MessagesConfig;
import com.ultimatedungeon.config.files.PartyConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.core.PluginScheduler;
import com.ultimatedungeon.events.party.PartyReadyEvent;
import com.ultimatedungeon.party.model.Party;
import com.ultimatedungeon.party.model.PartyState;
import com.ultimatedungeon.party.model.ReadyCheckSession;
import com.ultimatedungeon.util.MiniMessageUtil;
import com.ultimatedungeon.util.TimeUtil;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Manages the per-party ready-check lifecycle.
 *
 * <h3>Flow</h3>
 * <ol>
 *   <li>{@link #startReadyCheck} — broadcasts prompt, sets party state to
 *       {@link PartyState#READY_CHECK}, and schedules expiry.</li>
 *   <li>Each member calls {@link #recordResponse} when they click accept or deny.</li>
 *   <li>If all members respond positively the session is resolved immediately via
 *       the {@code onAllReady} callback (the leader doesn't need to wait for the timer).</li>
 *   <li>If the timer fires before all members respond, the session expires, the party
 *       returns to {@link PartyState#FORMING}, and failure is broadcast.</li>
 * </ol>
 */
public final class ReadyCheckManager {

    private final PartyConfig    partyConfig;
    private final MessagesConfig messages;
    private final PluginScheduler scheduler;
    private final PluginLogger   logger;
    private final Server         server;

    /** partyId → active session */
    private final Map<UUID, ReadyCheckSession> activeSessions = new ConcurrentHashMap<>();
    /** partyId → Bukkit task ID for the expiry countdown */
    private final Map<UUID, Integer>           expiryTasks    = new ConcurrentHashMap<>();

    public ReadyCheckManager(
            @NotNull final PartyConfig    partyConfig,
            @NotNull final MessagesConfig messages,
            @NotNull final PluginScheduler scheduler,
            @NotNull final PluginLogger   logger,
            @NotNull final Server         server
    ) {
        this.partyConfig = partyConfig;
        this.messages    = messages;
        this.scheduler   = scheduler;
        this.logger      = logger;
        this.server      = server;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Starts a ready check for the given party.
     *
     * @param party      the party that should confirm readiness
     * @param onAllReady callback fired when every member has accepted — caller
     *                   should launch the dungeon here
     */
    public void startReadyCheck(
            @NotNull final Party            party,
            @NotNull final Consumer<Party>  onAllReady
    ) {
        final UUID partyId = party.getPartyId();
        if (activeSessions.containsKey(partyId)) {
            logger.debug("Ready check already active for party " + partyId);
            return;
        }

        final long expiresAt = TimeUtil.now()
                + (partyConfig.getReadyCheckDurationSeconds() * 1_000L);
        final ReadyCheckSession session = new ReadyCheckSession(partyId, expiresAt);
        activeSessions.put(partyId, session);
        party.setState(PartyState.READY_CHECK);

        // Broadcast ready-check start to all members
        final String timeStr = String.valueOf(partyConfig.getReadyCheckDurationSeconds());
        for (final Player member : party.getMembers()) {
            if (member.isOnline()) {
                MiniMessageUtil.send(member,
                        messages.getPrefix() + messages.getPartyReadyCheckStarted(),
                        Map.of("time", timeStr));
            }
        }

        // Schedule expiry — runs on main thread
        final org.bukkit.scheduler.BukkitTask task = scheduler.runSyncDelayed(
                () -> onExpiry(party, session),
                partyConfig.getReadyCheckDurationTicks()
        );
        expiryTasks.put(partyId, task.getTaskId());

        logger.debug("Ready check started for party " + partyId
                + " (expires in " + partyConfig.getReadyCheckDurationSeconds() + "s)");
    }

    /**
     * Records a player's response to the ready check.
     *
     * @param party      the party this player belongs to
     * @param player     the player responding
     * @param ready      {@code true} = accept, {@code false} = deny
     * @param onAllReady callback used if this response completes the check positively
     */
    public void recordResponse(
            @NotNull final Party           party,
            @NotNull final Player          player,
            final boolean                  ready,
            @NotNull final Consumer<Party> onAllReady
    ) {
        final ReadyCheckSession session = activeSessions.get(party.getPartyId());
        if (session == null || session.isExpired()) return;

        session.recordResponse(player, ready);
        logger.debug(player.getName() + " responded: " + (ready ? "READY" : "NOT READY"));

        if (!ready) {
            // Immediate failure on any denial
            cancelSession(party);
            party.setState(PartyState.FORMING);
            for (final Player m : party.getMembers()) {
                if (m.isOnline()) {
                    MiniMessageUtil.send(m,
                            messages.getPrefix() + messages.getPartyReadyCheckFailed());
                }
            }
            return;
        }

        // Check if all expected members have responded positively
        final long expectedCount = party.getMembers().stream()
                .filter(Player::isOnline).count();
        final long positiveCount = session.getResponses().values().stream()
                .filter(Boolean::booleanValue).count();

        if (positiveCount >= expectedCount) {
            cancelSession(party);
            party.setState(PartyState.FORMING);
            server.getPluginManager().callEvent(new PartyReadyEvent(party));
            for (final Player m : party.getMembers()) {
                if (m.isOnline()) {
                    MiniMessageUtil.send(m,
                            messages.getPrefix() + messages.getPartyReadyCheckPassed());
                }
            }
            onAllReady.accept(party);
        }
    }

    /** Returns {@code true} if a ready check is currently active for the party. */
    public boolean hasActiveSession(@NotNull final UUID partyId) {
        return activeSessions.containsKey(partyId);
    }

    /** Cancels an active ready check without sending failure messages. */
    public void cancelSession(@NotNull final Party party) {
        final UUID partyId = party.getPartyId();
        activeSessions.remove(partyId);
        final Integer taskId = expiryTasks.remove(partyId);
        if (taskId != null) scheduler.cancel(taskId);
    }

    /** Clears all active sessions on shutdown. */
    public void clearAll() {
        expiryTasks.values().forEach(scheduler::cancel);
        expiryTasks.clear();
        activeSessions.clear();
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void onExpiry(@NotNull final Party party, @NotNull final ReadyCheckSession session) {
        if (!activeSessions.containsKey(party.getPartyId())) return; // already resolved
        if (session.allReady()) return; // shouldn't happen but guard

        activeSessions.remove(party.getPartyId());
        expiryTasks.remove(party.getPartyId());
        party.setState(PartyState.FORMING);

        for (final Player m : party.getMembers()) {
            if (m.isOnline()) {
                MiniMessageUtil.send(m, messages.getPrefix() + messages.getPartyReadyCheckFailed());
            }
        }
        logger.debug("Ready check expired for party " + party.getPartyId());
    }
}
