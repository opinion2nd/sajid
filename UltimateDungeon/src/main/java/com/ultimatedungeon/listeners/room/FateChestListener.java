package com.ultimatedungeon.listeners.room;

import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.boss.arena.ArenaLockdownManager;
import com.ultimatedungeon.dungeon.instance.DungeonInstance;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.monster.engine.WaveManager;
import com.ultimatedungeon.rewards.engine.RewardRoomService;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomGraph;
import com.ultimatedungeon.room.model.RoomType;
import com.ultimatedungeon.util.MiniMessageUtil;
import com.ultimatedungeon.util.RandomUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Chest of Fate — the room type that replaced puzzles.
 *
 * <p>The room seals shut and presents three chests. A player opens ONE and
 * fate decides: <b>fortune</b> (rare loot for everyone inside), an
 * <b>ambush</b> (a monster wave must be cleared to unlock), or a
 * <b>curse</b> (poison and darkness — but the doors open). All three chests
 * vanish the moment one is touched, so choose wisely.</p>
 */
public final class FateChestListener implements Listener {

    private final DungeonInstanceManager instanceManager;
    private final ArenaLockdownManager arenaLockdown;
    private final WaveManager waveManager;
    private final RewardRoomService rewardRoomService;

    /** Rooms whose fate has already been decided. */
    private final Set<String> resolved = ConcurrentHashMap.newKeySet();

    public FateChestListener(@NotNull final DungeonInstanceManager instanceManager,
                             @NotNull final ArenaLockdownManager arenaLockdown,
                             @NotNull final WaveManager waveManager,
                             @NotNull final RewardRoomService rewardRoomService) {
        this.instanceManager = instanceManager;
        this.arenaLockdown = arenaLockdown;
        this.waveManager = waveManager;
        this.rewardRoomService = rewardRoomService;
    }

    @EventHandler
    public void onChestOpen(@NotNull final PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        final Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.CHEST) return;

        final Player player = event.getPlayer();
        final IDungeonInstance raw = instanceManager.getInstanceForPlayer(player);
        if (!(raw instanceof final DungeonInstance instance)) return;
        final RoomGraph graph = instance.getRoomGraph();
        if (graph == null) return;

        final RoomData room = roomAt(graph, block.getLocation());
        if (room == null || room.getType() != RoomType.PUZZLE) return;

        event.setCancelled(true); // never open the vanilla inventory
        if (room.isCleared() || !resolved.add(room.getRoomId())) return;

        removeAllChests(room);
        resolveFate(instance.getInstanceId(), instance, room, player);
    }

    // ── Fate outcomes ─────────────────────────────────────────────────────────

    private void resolveFate(@NotNull final UUID instanceId, @NotNull final DungeonInstance instance,
                             @NotNull final RoomData room, @NotNull final Player opener) {
        final List<Player> inRoom = playersInRoom(room);
        final double roll = RandomUtil.random();
        final World world = room.getCentre().getWorld();

        if (roll < 0.40) {
            // ── Fortune: rare loot for everyone inside ──────────────────────
            if (world != null) {
                world.spawnParticle(Particle.TOTEM_OF_UNDYING, opener.getLocation().add(0, 1, 0),
                        60, 0.8, 1.0, 0.8, 0.15);
            }
            for (final Player p : inRoom) {
                MiniMessageUtil.send(p, "<green><bold>FORTUNE!</bold></green> "
                        + "<gray>The chest overflows with riches.");
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            }
            rewardRoomService.grant(inRoom, "secret_room_loot");
            finish(instanceId, room);
        } else if (roll < 0.75) {
            // ── Ambush: clear the wave to unlock the doors ──────────────────
            for (final Player p : inRoom) {
                MiniMessageUtil.send(p, "<red><bold>AMBUSH!</bold></red> "
                        + "<gray>It was a trap — fight your way out!");
                p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1.0f, 0.6f);
            }
            final String level = instance.getContext().getRequest().getDifficultyId();
            waveManager.startForLevel(instanceId, room, level, () -> finish(instanceId, room));
        } else {
            // ── Curse: pain, but the doors open ─────────────────────────────
            if (world != null) {
                world.spawnParticle(Particle.SQUID_INK, room.getCentre().add(0, 1.5, 0),
                        50, 2.0, 1.0, 2.0, 0.05);
            }
            for (final Player p : inRoom) {
                MiniMessageUtil.send(p, "<dark_purple><bold>CURSED!</bold></dark_purple> "
                        + "<gray>Greed has its price...");
                p.playSound(p.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 1.0f, 0.6f);
                p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 160, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 160, 0));
            }
            finish(instanceId, room);
        }
    }

    private void finish(@NotNull final UUID instanceId, @NotNull final RoomData room) {
        room.setCleared();
        arenaLockdown.unlock(instanceId, room.getRoomId());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** All three chests vanish the moment one is chosen. */
    private void removeAllChests(@NotNull final RoomData room) {
        final Location origin = room.getOrigin();
        final World world = origin.getWorld();
        if (world == null) return;
        for (int x = 0; x < room.getWidth(); x++) {
            for (int y = 0; y < room.getHeight(); y++) {
                for (int z = 0; z < room.getDepth(); z++) {
                    final Block b = world.getBlockAt(origin.getBlockX() + x,
                            origin.getBlockY() + y, origin.getBlockZ() + z);
                    if (b.getType() == Material.CHEST) b.setType(Material.AIR, false);
                }
            }
        }
    }

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

    private RoomData roomAt(@NotNull final RoomGraph graph, @NotNull final Location loc) {
        for (final RoomData room : graph.getRooms()) {
            if (room.contains(loc)) return room;
        }
        return null;
    }
}
