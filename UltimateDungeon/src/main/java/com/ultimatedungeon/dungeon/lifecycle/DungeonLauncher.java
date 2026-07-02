package com.ultimatedungeon.dungeon.lifecycle;

import com.ultimatedungeon.api.dungeon.DungeonGenerationRequest;
import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.config.files.MessagesConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.dungeon.generation.GenerationPipeline;
import com.ultimatedungeon.dungeon.instance.DungeonCleanupService;
import com.ultimatedungeon.dungeon.instance.DungeonInstance;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.managers.PlayerSessionManager;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.services.NotificationService;
import com.ultimatedungeon.services.PlayerTeleportService;
import com.ultimatedungeon.services.StatisticsService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Orchestrates the full dungeon launch and teardown pipeline.
 *
 * <p>Launch flow: build a request → run the generation pipeline → on success,
 * remember each player's origin, open a session, teleport them to the spawn
 * room and announce the start. Completion and failure both return every player
 * home, close sessions, run cleanup, and notify.</p>
 */
public final class DungeonLauncher {

    private final GenerationPipeline     pipeline;
    private final DungeonInstanceManager instanceManager;
    private final PlayerSessionManager   sessionManager;
    private final PlayerTeleportService  teleportService;
    private final NotificationService    notifications;
    private final StatisticsService      statistics;
    private final DungeonCleanupService  cleanupService;
    private final com.ultimatedungeon.dungeon.world.DungeonWorldManager worldManager;
    private final MessagesConfig         messages;
    private final PluginLogger           logger;

    /** Origin locations to return players to when their run ends. */
    private final Map<UUID, Location> returnLocations = new ConcurrentHashMap<>();
    /** Players associated with each active instance. */
    private final Map<UUID, Set<UUID>> instancePlayers = new ConcurrentHashMap<>();
    /** Database record id per instance for completion stats. */
    private final Map<UUID, Long> instanceRecordId = new ConcurrentHashMap<>();

    /** Optional hook invoked on completion (instance, winners) — wired to rewards. */
    private BiConsumer<DungeonInstance, List<Player>> onComplete;
    private BiConsumer<IDungeonInstance, List<Player>> onStart;
    private BiConsumer<Runnable, Long> delayedRunner;

    /** Ticks players get to celebrate in the cleared dungeon before teardown. */
    private static final long VICTORY_LAP_TICKS = 160L;

    public DungeonLauncher(@NotNull final GenerationPipeline pipeline,
                           @NotNull final DungeonInstanceManager instanceManager,
                           @NotNull final PlayerSessionManager sessionManager,
                           @NotNull final PlayerTeleportService teleportService,
                           @NotNull final NotificationService notifications,
                           @NotNull final StatisticsService statistics,
                           @NotNull final DungeonCleanupService cleanupService,
                           @NotNull final com.ultimatedungeon.dungeon.world.DungeonWorldManager worldManager,
                           @NotNull final MessagesConfig messages,
                           @NotNull final PluginLogger logger) {
        this.pipeline = pipeline;
        this.instanceManager = instanceManager;
        this.sessionManager = sessionManager;
        this.teleportService = teleportService;
        this.notifications = notifications;
        this.statistics = statistics;
        this.cleanupService = cleanupService;
        this.worldManager = worldManager;
        this.messages = messages;
        this.logger = logger;
    }

    public void setCompletionHook(@NotNull final BiConsumer<DungeonInstance, List<Player>> hook) {
        this.onComplete = hook;
    }

    /** Fired after players are teleported into a freshly generated dungeon. */
    public void setStartHook(@NotNull final BiConsumer<IDungeonInstance, List<Player>> hook) {
        this.onStart = hook;
    }

    /**
     * Supplies a main-thread delayed executor (runnable, delayTicks). When set,
     * completion waits {@link #VICTORY_LAP_TICKS} before tearing the dungeon
     * down, giving players a victory lap for the rank screen and celebration.
     */
    public void setDelayedRunner(@NotNull final BiConsumer<Runnable, Long> runner) {
        this.delayedRunner = runner;
    }

    // ── Launch ──────────────────────────────────────────────────────────────

    /**
     * Launches a dungeon for the given players.
     *
     * @return {@code false} if the generation pipeline rejected the request
     *         (e.g. server at capacity)
     */
    public boolean launch(@NotNull final DungeonGenerationRequest request,
                          @NotNull final List<Player> players) {
        for (final Player p : players) notifications.chat(p, messages.getDungeonGenerating());

        return pipeline.launch(request, players,
                instance -> onGenerated(instance, players, request),
                error -> players.forEach(p ->
                        notifications.chat(p, messages.getDungeonFailed())));
    }

    private void onGenerated(@NotNull final IDungeonInstance instance,
                             @NotNull final List<Player> players,
                             @NotNull final DungeonGenerationRequest request) {
        final Location spawn = resolveSpawn(instance);
        final Set<UUID> ids = new LinkedHashSet<>();

        if (instance instanceof DungeonInstance di) {
            di.setActive();
        }

        for (final Player p : players) {
            if (!p.isOnline()) continue;
            returnLocations.put(p.getUniqueId(), p.getLocation().clone());
            sessionManager.createSession(p, instance.getInstanceId());
            instanceManager.associatePlayer(p, instance.getInstanceId());
            ids.add(p.getUniqueId());
            if (spawn != null) teleportService.teleport(p, spawn);
            notifications.chat(p, messages.getDungeonStarting());
            notifications.title(p, "<gold>Dungeon", "<gray>" + request.getThemeId());
        }
        instancePlayers.put(instance.getInstanceId(), ids);

        if (onStart != null) {
            try {
                onStart.accept(instance, players);
            } catch (final Exception e) {
                logger.severe("Start hook failed for " + instance.getInstanceId(), e);
            }
        }

        // Record run start (attribute to the requester).
        final Player requester = Bukkit.getPlayer(request.getRequesterId());
        if (requester != null) {
            statistics.ensurePlayer(requester);
            statistics.recordRunStart(request.getRequesterId(), request.getThemeId(),
                    request.getDifficultyId(), players.size(),
                    id -> instanceRecordId.put(instance.getInstanceId(), id));
        }
        logger.info("Dungeon launched: " + instance.getInstanceId()
                + " for " + ids.size() + " player(s).");
    }

    // ── Completion / failure ────────────────────────────────────────────────

    /** Completes a dungeon successfully: rewards hook, stats, return players, cleanup. */
    public void complete(@NotNull final DungeonInstance instance, @Nullable final String bossKilled) {
        final UUID id = instance.getInstanceId();
        final List<Player> players = onlinePlayers(id);
        final long duration = instance.getContext().getElapsedMs();
        final var request = instance.getContext().getRequest();

        if (onComplete != null) {
            try {
                onComplete.accept(instance, players);
            } catch (final Exception e) {
                logger.severe("Reward hook failed for " + id, e);
            }
        }

        final Long recordId = instanceRecordId.get(id);
        statistics.recordCompletion(request.getRequesterId(),
                recordId != null ? recordId : -1L, duration, bossKilled, request.getDifficultyId());

        for (final Player p : players) {
            notifications.chat(p, messages.getDungeonCompleted());
            notifications.title(p, "<green>Victory!", "<gray>Dungeon complete");
        }
        instance.end();
        if (delayedRunner != null) {
            // Victory lap: leave everyone in the cleared dungeon briefly so the
            // rank screen and celebration play out, then tear down with a FRESH
            // player list (anyone who logged off or left is skipped).
            delayedRunner.accept(() -> teardown(instance, onlinePlayers(id)), VICTORY_LAP_TICKS);
        } else {
            teardown(instance, players);
        }
    }

    /** Fails a dungeon: notify, return players, cleanup. */
    public void fail(@NotNull final DungeonInstance instance) {
        final List<Player> players = onlinePlayers(instance.getInstanceId());
        for (final Player p : players) {
            notifications.chat(p, messages.getDungeonFailed());
            notifications.title(p, "<red>Defeat", "<gray>The dungeon claimed you");
        }
        instance.fail();
        teardown(instance, players);
    }

    /** Removes a single player from their dungeon (e.g. /dungeon leave). */
    public void leave(@NotNull final Player player) {
        final var inst = instanceManager.getInstanceForPlayer(player);
        final UUID instanceId = inst != null ? inst.getInstanceId() : null;

        sendHome(player);
        sessionManager.removeSession(player);
        instanceManager.disassociatePlayer(player);
        for (final Set<UUID> set : instancePlayers.values()) set.remove(player.getUniqueId());

        // If that was the last player, tear the whole instance down so the boss
        // bar, monsters and isolated world do not linger.
        if (instanceId != null) {
            final Set<UUID> remaining = instancePlayers.get(instanceId);
            if ((remaining == null || remaining.isEmpty())
                    && inst instanceof final DungeonInstance di) {
                teardown(di, List.of());
            }
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private void teardown(@NotNull final DungeonInstance instance, @NotNull final List<Player> players) {
        final UUID id = instance.getInstanceId();
        for (final Player p : players) {
            sendHome(p);
            sessionManager.removeSession(p);
            instanceManager.disassociatePlayer(p);
        }
        cleanupService.cleanup(instance);
        instanceManager.removeInstance(id);
        instancePlayers.remove(id);
        instanceRecordId.remove(id);
        // Tear down this instance's isolated world so nothing is left behind.
        worldManager.destroyInstanceWorld(id);
    }

    private void sendHome(@NotNull final Player player) {
        final Location home = returnLocations.remove(player.getUniqueId());
        teleportService.teleport(player,
                home != null ? home : player.getServer().getWorlds().get(0).getSpawnLocation());
    }

    @NotNull
    private List<Player> onlinePlayers(@NotNull final UUID instanceId) {
        final List<Player> list = new ArrayList<>();
        final Set<UUID> ids = instancePlayers.getOrDefault(instanceId, Set.of());
        for (final UUID uuid : ids) {
            final Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) list.add(p);
        }
        return list;
    }

    @Nullable
    private Location resolveSpawn(@NotNull final IDungeonInstance instance) {
        if (!(instance instanceof DungeonInstance di) || di.getRoomGraph() == null) return null;
        final RoomData spawnRoom = di.getRoomGraph().getSpawnRoom();
        return spawnRoom != null ? spawnRoom.getCentre() : null;
    }

    /** Returns the players currently inside the given instance. */
    @NotNull
    public List<Player> getPlayers(@NotNull final UUID instanceId) {
        return onlinePlayers(instanceId);
    }
}
