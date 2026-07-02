package com.ultimatedungeon.listeners.room;

import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.boss.arena.ArenaCountdownManager;
import com.ultimatedungeon.boss.arena.ArenaLockdownManager;
import com.ultimatedungeon.boss.engine.BossEngine;
import com.ultimatedungeon.boss.model.BossDefinition;
import com.ultimatedungeon.dungeon.event.DynamicEventEngine;
import com.ultimatedungeon.dungeon.instance.DungeonInstance;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.monster.engine.WaveManager;
import com.ultimatedungeon.puzzle.engine.PuzzleEngine;
import com.ultimatedungeon.puzzle.puzzles.ColorSequencePuzzle;
import com.ultimatedungeon.rewards.engine.RewardDistributor;
import com.ultimatedungeon.rewards.model.RewardEvent;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomGraph;
import com.ultimatedungeon.room.model.RoomType;
import com.ultimatedungeon.services.DifficultyService;
import com.ultimatedungeon.theme.model.ThemeDefinition;
import com.ultimatedungeon.trap.engine.TrapEngine;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detects when a player first enters a dungeon room and activates it: combat
 * rooms start monster waves, trap rooms place traps, puzzle rooms start a puzzle,
 * and the boss room spawns the boss and seals the arena.
 */
public final class RoomEnterListener implements Listener {

    private static final int TRAPS_PER_ROOM = 4;

    private final DungeonInstanceManager instanceManager;
    private final WaveManager waveManager;
    private final TrapEngine trapEngine;
    private final PuzzleEngine puzzleEngine;
    private final BossEngine bossEngine;
    private final ArenaLockdownManager arenaLockdown;
    private final ArenaCountdownManager arenaCountdown;
    private final DynamicEventEngine dynamicEventEngine;
    private final RewardDistributor rewardDistributor;
    private final DifficultyService difficulty;
    private final com.ultimatedungeon.dungeon.lifecycle.WaveResetManager waveResets;
    private final int waveResetSeconds;
    private final com.ultimatedungeon.dungeon.instance.RoomSealer sealer;
    private final com.ultimatedungeon.dungeon.instance.EncounterCountdownManager countdown;
    private final com.ultimatedungeon.dungeon.lifecycle.DungeonScoreService scoreService;

    /** Seconds a player must stay in a wave/boss room before it seals and starts. */
    private static final int ENCOUNTER_COUNTDOWN = 10;

    private final Map<UUID, String> currentRoom = new ConcurrentHashMap<>();

    public RoomEnterListener(@NotNull final DungeonInstanceManager instanceManager,
                             @NotNull final WaveManager waveManager,
                             @NotNull final TrapEngine trapEngine,
                             @NotNull final PuzzleEngine puzzleEngine,
                             @NotNull final BossEngine bossEngine,
                             @NotNull final ArenaLockdownManager arenaLockdown,
                             @NotNull final ArenaCountdownManager arenaCountdown,
                             @NotNull final DynamicEventEngine dynamicEventEngine,
                             @NotNull final RewardDistributor rewardDistributor,
                             @NotNull final DifficultyService difficulty,
                             @NotNull final com.ultimatedungeon.dungeon.lifecycle.WaveResetManager waveResets,
                             final int waveResetSeconds,
                             @NotNull final com.ultimatedungeon.dungeon.instance.RoomSealer sealer,
                             @NotNull final com.ultimatedungeon.dungeon.instance.EncounterCountdownManager countdown,
                             @NotNull final com.ultimatedungeon.dungeon.lifecycle.DungeonScoreService scoreService) {
        this.instanceManager = instanceManager;
        this.waveManager = waveManager;
        this.trapEngine = trapEngine;
        this.puzzleEngine = puzzleEngine;
        this.bossEngine = bossEngine;
        this.arenaLockdown = arenaLockdown;
        this.arenaCountdown = arenaCountdown;
        this.dynamicEventEngine = dynamicEventEngine;
        this.rewardDistributor = rewardDistributor;
        this.difficulty = difficulty;
        this.waveResets = waveResets;
        this.waveResetSeconds = waveResetSeconds;
        this.sealer = sealer;
        this.countdown = countdown;
        this.scoreService = scoreService;
    }

    @EventHandler
    public void onMove(@NotNull final PlayerMoveEvent event) {
        final Location to = event.getTo();
        if (to == null || !movedBlock(event)) return;
        final Player player = event.getPlayer();

        final IDungeonInstance raw = instanceManager.getInstanceForPlayer(player);
        if (!(raw instanceof final DungeonInstance instance)) return;
        final RoomGraph graph = instance.getRoomGraph();
        if (graph == null) return;

        final RoomData room = roomAt(graph, to);
        if (room == null) return;

        // Parkour completion is checked on every move (not just on room entry) so
        // reaching the finish pad mid-course is always caught.
        if (room.getType() == RoomType.PARKOUR && !room.isCleared()) {
            checkParkourFinish(room, player);
        }

        final String last = currentRoom.get(player.getUniqueId());
        if (room.getRoomId().equals(last)) return;
        currentRoom.put(player.getUniqueId(), room.getRoomId());

        if (room.isEntered()) {
            if (room.isCleared() && isWaveRoom(room.getType())) {
                MiniMessageUtil.send(player, "<green>✔ This room is already cleared.");
            }
            return;
        }
        // Wave and boss rooms run a 10s countdown first — the encounter only
        // begins (and the room seals) if players are still inside at zero.
        if (isEncounterRoom(room.getType())) {
            if (!countdown.isArming(instance.getInstanceId(), room)) {
                countdown.arm(instance.getInstanceId(), room, ENCOUNTER_COUNTDOWN,
                        () -> playersInRoom(room), () -> beginEncounter(instance, room));
            }
            return;
        }
        room.setEntered();
        activate(instance, room);
    }

    private boolean isWaveRoom(@NotNull final RoomType type) {
        return type == RoomType.COMBAT || type == RoomType.ELITE_COMBAT
                || type == RoomType.MINI_BOSS;
    }

    private boolean isEncounterRoom(@NotNull final RoomType type) {
        return isWaveRoom(type) || type == RoomType.BOSS;
    }

    /** Activates non-sealing rooms (event, trap, puzzle, secret) immediately on entry. */
    private void activate(@NotNull final DungeonInstance instance, @NotNull final RoomData room) {
        final UUID id = instance.getInstanceId();
        final String difficultyId = instance.getContext().getRequest().getDifficultyId();
        final int level = difficulty.level(difficultyId);

        switch (room.getType()) {
            case EVENT -> {
                final List<Player> inRoom = playersInRoom(room);
                final boolean fired = dynamicEventEngine.trigger(id, room, inRoom, level);
                if (!fired) {
                    waveManager.start(id, room, level, () -> onWaveRoomCleared(id, room));
                }
            }
            case SECRET -> {
                scoreService.recordSecret(id);
                discoverSecret(room);
            }
            case TRAP -> trapEngine.placeInRoom(id, room, TRAPS_PER_ROOM, difficultyId);
            case PUZZLE -> puzzleEngine.startPuzzle(id, new ColorSequencePuzzle(), room::setCleared);
            default -> { /* spawn, treasure, merchant, reward — no auto-activation */ }
        }
    }

    /** Seals the room with bedrock and starts its wave or boss fight. */
    private void beginEncounter(@NotNull final DungeonInstance instance, @NotNull final RoomData room) {
        if (room.isEntered()) return;
        room.setEntered();
        final UUID id = instance.getInstanceId();
        final String difficultyId = instance.getContext().getRequest().getDifficultyId();
        final int level = difficulty.level(difficultyId);
        final ThemeDefinition theme = instance.getTheme();

        if (instance.getRoomGraph() != null) sealer.seal(id, room, instance.getRoomGraph());
        for (final Player p : playersInRoom(room)) {
            MiniMessageUtil.send(p, "<red><bold>The room seals!</bold> <gray>Clear it to open the exits.");
            p.playSound(p.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1.0f, 0.6f);
        }

        if (room.getType() == RoomType.BOSS) {
            final List<String> bosses = theme != null ? theme.getBossPool() : List.of();
            if (bosses.isEmpty()) return;
            arenaLockdown.lock(id, room);
            // Exactly ONE boss per boss room — random, and never the same boss
            // twice in one dungeon (each boss room gets a distinct boss).
            final String bossId = bossEngine.pickUnusedBoss(id, bosses);
            if (bossId == null) return;
            bossEngine.spawnBoss(id, bossId, room.getCentre(), difficultyId, playersInRoom(room));
        } else {
            waveManager.start(id, room, level, () -> onWaveRoomCleared(id, room));
        }
    }

    /** Fires when a wave room's final wave is cleared: reward, announce, open the room. */
    private void onWaveRoomCleared(@NotNull final UUID instanceId, @NotNull final RoomData room) {
        room.setCleared();
        sealer.unseal(instanceId, room); // open the exits
        final List<Player> inRoom = playersInRoom(room);
        for (final Player p : inRoom) {
            MiniMessageUtil.send(p, "<green><bold>Wave cleared!</bold> <gray>The exits open.");
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.1f);
        }
        if (!inRoom.isEmpty()) {
            rewardDistributor.distributeAll(inRoom, RewardEvent.WAVE_COMPLETION);
        }
    }

    /**
     * Completes a parkour course when a player stands on its gold finish pad:
     * marks the room cleared, rewards the finisher and plays a flourish.
     */
    private void checkParkourFinish(@NotNull final RoomData room, @NotNull final Player player) {
        final Location below = player.getLocation().clone().subtract(0, 1, 0);
        if (below.getBlock().getType() != com.ultimatedungeon.room.templates.ParkourRoomTemplate.FINISH_PAD) {
            return;
        }
        room.setCleared();
        rewardDistributor.distributeAll(List.of(player), RewardEvent.TREASURE_ROOM);
        MiniMessageUtil.send(player, "<gold><bold>Parkour complete!</bold> <gray>Claim your reward.");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.3f);
        if (player.getWorld() != null) {
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                    player.getLocation().add(0, 1, 0), 24, 0.6, 0.8, 0.6, 0.1);
        }
    }

    /** Grants secret-room loot to everyone inside and plays a discovery flourish. */
    private void discoverSecret(@NotNull final RoomData room) {
        final List<Player> inRoom = playersInRoom(room);
        if (!inRoom.isEmpty()) {
            rewardDistributor.distributeAll(inRoom, RewardEvent.SECRET_ROOM);
            for (final Player p : inRoom) {
                p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.2f);
                if (p.getWorld() != null) {
                    p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                            p.getLocation().add(0, 1, 0), 20, 0.6, 0.8, 0.6, 0.1);
                }
            }
        }
        room.setCleared();
    }

    /** @return the players currently standing inside {@code room}. */
    @NotNull
    private List<Player> playersInRoom(@NotNull final RoomData room) {
        final List<Player> result = new ArrayList<>();
        final Location centre = room.getCentre();
        if (centre.getWorld() == null) return result;
        for (final Player p : centre.getWorld().getPlayers()) {
            if (room.contains(p.getLocation())) result.add(p);
        }
        return result;
    }

    @Nullable
    private RoomData roomAt(@NotNull final RoomGraph graph, @NotNull final Location loc) {
        for (final RoomData room : graph.getRooms()) {
            if (room.contains(loc)) return room;
        }
        return null;
    }

    private boolean movedBlock(@NotNull final PlayerMoveEvent event) {
        final Location from = event.getFrom();
        final Location to = event.getTo();
        return to == null || from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ();
    }
}
