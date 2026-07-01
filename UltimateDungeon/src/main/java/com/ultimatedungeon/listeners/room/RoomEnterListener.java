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
import com.ultimatedungeon.theme.model.ThemeDefinition;
import com.ultimatedungeon.trap.engine.TrapEngine;
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
import java.util.concurrent.ThreadLocalRandom;

/**
 * Detects when a player first enters a dungeon room and activates it: combat
 * rooms start monster waves, trap rooms place traps, puzzle rooms start a puzzle,
 * and the boss room spawns the boss and seals the arena.
 */
public final class RoomEnterListener implements Listener {

    private static final int WAVE_COUNT = 2;
    private static final int PER_WAVE = 4;
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

    private final Map<UUID, String> currentRoom = new ConcurrentHashMap<>();

    public RoomEnterListener(@NotNull final DungeonInstanceManager instanceManager,
                             @NotNull final WaveManager waveManager,
                             @NotNull final TrapEngine trapEngine,
                             @NotNull final PuzzleEngine puzzleEngine,
                             @NotNull final BossEngine bossEngine,
                             @NotNull final ArenaLockdownManager arenaLockdown,
                             @NotNull final ArenaCountdownManager arenaCountdown,
                             @NotNull final DynamicEventEngine dynamicEventEngine,
                             @NotNull final RewardDistributor rewardDistributor) {
        this.instanceManager = instanceManager;
        this.waveManager = waveManager;
        this.trapEngine = trapEngine;
        this.puzzleEngine = puzzleEngine;
        this.bossEngine = bossEngine;
        this.arenaLockdown = arenaLockdown;
        this.arenaCountdown = arenaCountdown;
        this.dynamicEventEngine = dynamicEventEngine;
        this.rewardDistributor = rewardDistributor;
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

        final String last = currentRoom.get(player.getUniqueId());
        if (room.getRoomId().equals(last)) return;
        currentRoom.put(player.getUniqueId(), room.getRoomId());

        if (room.isEntered()) return; // already activated by someone
        room.setEntered();
        activate(instance, room);
    }

    private void activate(@NotNull final DungeonInstance instance, @NotNull final RoomData room) {
        final UUID id = instance.getInstanceId();
        final String difficulty = instance.getContext().getRequest().getDifficultyId();
        final ThemeDefinition theme = instance.getTheme();
        final List<String> monsters = theme != null ? theme.getMonsterPool() : List.of();

        switch (room.getType()) {
            case COMBAT, ELITE_COMBAT, MINI_BOSS -> {
                if (!monsters.isEmpty()) {
                    waveManager.start(id, room, monsters, WAVE_COUNT, PER_WAVE, difficulty, room::setCleared);
                }
            }
            case EVENT -> {
                final List<Player> inRoom = playersInRoom(room);
                final boolean fired = dynamicEventEngine.trigger(id, room, inRoom, monsters, difficulty);
                if (!fired && !monsters.isEmpty()) {
                    waveManager.start(id, room, monsters, WAVE_COUNT, PER_WAVE, difficulty, room::setCleared);
                }
            }
            case SECRET -> discoverSecret(room);
            case TRAP -> trapEngine.placeInRoom(id, room, TRAPS_PER_ROOM, difficulty);
            case PUZZLE -> puzzleEngine.startPuzzle(id, new ColorSequencePuzzle(), room::setCleared);
            case BOSS -> {
                final List<String> bosses = theme != null ? theme.getBossPool() : List.of();
                if (bosses.isEmpty()) return;
                final String bossId = bosses.get(ThreadLocalRandom.current().nextInt(bosses.size()));
                final BossDefinition def = bossEngine.getDefinition(bossId);
                final int seconds = def != null ? def.getCountdownSeconds() : 10;
                final var world = room.getCentre().getWorld();
                if (world == null) return;
                // Pre-fight countdown, then spawn the boss and seal the arena.
                arenaCountdown.start(seconds, world.getPlayers(), () -> {
                    if (world.getPlayers().stream().noneMatch(p -> room.contains(p.getLocation()))) {
                        return; // countdown safety: everyone left, do not spawn
                    }
                    arenaLockdown.lock(id, room);
                    bossEngine.spawnBoss(id, bossId, room.getCentre(), difficulty, world.getPlayers());
                });
            }
            default -> { /* spawn, treasure, merchant, parkour, reward — no auto-activation */ }
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
