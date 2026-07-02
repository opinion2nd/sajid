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
        if (room == null) {
            // In a corridor — forget the room so re-entering it re-triggers
            // its countdown/activation.
            currentRoom.remove(player.getUniqueId());
            return;
        }

        final String last = currentRoom.get(player.getUniqueId());
        if (room.getRoomId().equals(last)) {
            checkParkourProgress(instance, room, player);
            return;
        }
        currentRoom.put(player.getUniqueId(), room.getRoomId());

        if (room.isEntered()) return; // already activated by someone
        activate(instance, room);
    }

    /** Wave/boss rooms roll their fate once and remember it. */
    private final Map<String, Boolean> waveRoll = new ConcurrentHashMap<>();
    /** Where each player entered a parkour room (goal = the far side). */
    private final Map<String, Location> parkourEntry = new ConcurrentHashMap<>();

    private void activate(@NotNull final DungeonInstance instance, @NotNull final RoomData room) {
        final UUID id = instance.getInstanceId();
        final String difficulty = instance.getContext().getRequest().getDifficultyId();
        final ThemeDefinition theme = instance.getTheme();
        final List<String> monsters = theme != null ? theme.getMonsterPool() : List.of();

        switch (room.getType()) {
            case COMBAT, ELITE_COMBAT, MINI_BOSS -> {
                // waves.yml decides ONCE whether this room hosts waves; the
                // percentage grows with the level, so not every room fights.
                final boolean hasWaves = waveRoll.computeIfAbsent(room.getRoomId(),
                        k -> waveManager.shouldRoomHaveWaves(difficulty));
                if (!hasWaves) {
                    room.setEntered();
                    room.setCleared();
                    return;
                }
                // 10s pre-fight countdown, boss-style: leaving the room cancels
                // it, re-entering restarts it. On zero the room seals shut.
                startSealedEncounter(id, room, waveManager.waveCountdownSeconds(difficulty), () ->
                        waveManager.startForLevel(id, room, difficulty, () -> {
                            room.setCleared();
                            arenaLockdown.unlock(id, room.getRoomId());
                        }));
            }
            case EVENT -> {
                room.setEntered();
                final List<Player> inRoom = playersInRoom(room);
                final boolean fired = dynamicEventEngine.trigger(id, room, inRoom, monsters, difficulty);
                if (!fired) {
                    arenaLockdown.lock(id, room);
                    waveManager.startForLevel(id, room, difficulty, () -> {
                        room.setCleared();
                        arenaLockdown.unlock(id, room.getRoomId());
                    });
                }
            }
            case SECRET -> {
                room.setEntered();
                discoverSecret(room);
            }
            case TRAP -> {
                room.setEntered();
                trapEngine.placeInRoom(id, room, TRAPS_PER_ROOM, difficulty);
            }
            case PUZZLE -> {
                // Chest of Fate: the room locks shut, three chests await.
                // Opening ONE decides your fate — fortune, ambush or curse.
                room.setEntered();
                arenaLockdown.lock(id, room);
                playersInRoom(room).forEach(p -> {
                    MiniMessageUtil.send(p, "<gold><bold>Chest of Fate!</bold></gold> "
                            + "<gray>Open ONE chest — fortune, ambush... or a curse.");
                    p.playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 0.6f);
                });
            }
            case PARKOUR -> {
                // Parkour rooms lock shut until a player reaches the far side.
                room.setEntered();
                arenaLockdown.lock(id, room);
                final List<Player> inRoom = playersInRoom(room);
                if (!inRoom.isEmpty()) {
                    parkourEntry.put(room.getRoomId(), inRoom.get(0).getLocation().clone());
                }
                inRoom.forEach(p -> MiniMessageUtil.send(p,
                        "<aqua>Reach the far side of the parkour to unlock the doors!"));
            }
            case BOSS -> {
                final List<String> bosses = theme != null ? theme.getBossPool() : List.of();
                final String firstId = bosses.isEmpty() ? null
                        : bosses.get(ThreadLocalRandom.current().nextInt(bosses.size()));
                final BossDefinition def = firstId != null ? bossEngine.getDefinition(firstId) : null;
                final int seconds = def != null ? def.getCountdownSeconds() : 10;
                final var world = room.getCentre().getWorld();
                if (world == null) return;
                // Pre-fight countdown (cancels if everyone leaves, restarts on
                // re-entry), then seal the arena and spawn ONE random boss —
                // each boss room in the run gets a DIFFERENT boss.
                startSealedEncounter(id, room, seconds, () -> {
                    bossEngine.spawnRandomBoss(id, bosses, room.getCentre(), difficulty,
                            playersNearRoom(room), room.getRoomId());
                    // Boss rooms only host normal waves when the config allows it.
                    if (waveManager.bossRoomWavesEnabled(difficulty)) {
                        waveManager.startForLevel(id, room, difficulty, room::setCleared);
                    }
                });
            }
            default -> {
                // spawn, treasure, merchant, reward — no auto-activation
                room.setEntered();
            }
        }
    }

    /**
     * Boss-style room entry: a cancellable countdown bound to the room. If
     * every player steps out, the countdown silently stops and the room can be
     * re-entered to start it again. When it hits zero the room's doorways seal
     * with bedrock and the encounter begins.
     */
    private void startSealedEncounter(@NotNull final UUID instanceId, @NotNull final RoomData room,
                                      final int seconds, @NotNull final Runnable begin) {
        arenaCountdown.startCancellable(room.getRoomId(), seconds,
                () -> playersInRoom(room), () -> {
                    room.setEntered();
                    arenaLockdown.lock(instanceId, room);
                    begin.run();
                });
    }

    /** Completes a parkour room when a player crosses to the far side. */
    private void checkParkourProgress(@NotNull final DungeonInstance instance,
                                      @NotNull final RoomData room, @NotNull final Player player) {
        if (room.getType() != com.ultimatedungeon.room.model.RoomType.PARKOUR
                || !room.isEntered() || room.isCleared()) return;
        final Location entry = parkourEntry.get(room.getRoomId());
        if (entry == null) return;
        final double dx = Math.abs(player.getLocation().getX() - entry.getX());
        final double dz = Math.abs(player.getLocation().getZ() - entry.getZ());
        final double span = Math.max(room.getWidth(), room.getDepth()) * 0.7;
        if (Math.max(dx, dz) >= span) {
            room.setCleared();
            arenaLockdown.unlock(instance.getInstanceId(), room.getRoomId());
            parkourEntry.remove(room.getRoomId());
            playersInRoom(room).forEach(p -> {
                MiniMessageUtil.send(p, "<green>Parkour complete — doors unlocked!");
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
            });
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

    /** Radius around a room considered part of the same encounter. */
    private static final double NEAR_ROOM_RADIUS_SQ = 48.0 * 48.0;

    /** @return the players inside or just outside {@code room} (same instance area). */
    @NotNull
    private List<Player> playersNearRoom(@NotNull final RoomData room) {
        final List<Player> result = new ArrayList<>();
        final Location centre = room.getCentre();
        if (centre.getWorld() == null) return result;
        for (final Player p : centre.getWorld().getPlayers()) {
            if (p.getLocation().distanceSquared(centre) <= NEAR_ROOM_RADIUS_SQ) result.add(p);
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
