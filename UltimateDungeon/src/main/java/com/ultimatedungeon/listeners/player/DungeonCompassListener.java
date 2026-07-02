package com.ultimatedungeon.listeners.player;

import com.ultimatedungeon.UltimateDungeon;
import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.dungeon.instance.DungeonInstance;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomGraph;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Dungeon Tracker: a compass given on dungeon entry. Right-clicking it
 * points the compass at the nearest unexplored room and tells the player how
 * far away it is and in which direction — so nobody gets lost in large layouts.
 *
 * <p>Outside a dungeon the tracker is inert; its first use there dissolves it,
 * keeping player inventories clean after a run.</p>
 */
public final class DungeonCompassListener implements Listener {

    private static final long USE_COOLDOWN_MS = 1_000L;

    private final DungeonInstanceManager instanceManager;
    private final NamespacedKey trackerKey;
    private final Map<UUID, Long> lastUse = new ConcurrentHashMap<>();

    public DungeonCompassListener(@NotNull final UltimateDungeon plugin,
                                  @NotNull final DungeonInstanceManager instanceManager) {
        this.instanceManager = instanceManager;
        this.trackerKey = new NamespacedKey(plugin, "ud_tracker");
    }

    /** Builds the tracker compass item handed to players on dungeon entry. */
    @NotNull
    public ItemStack createTracker() {
        final ItemStack item = new ItemStack(Material.COMPASS);
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MiniMessageUtil.legacy("<gold><bold>Dungeon Tracker</bold></gold>"));
            meta.setLore(java.util.List.of(
                    MiniMessageUtil.legacy("<gray>Right-click to point toward the"),
                    MiniMessageUtil.legacy("<gray>nearest unexplored room.")));
            meta.getPersistentDataContainer().set(trackerKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onUse(@NotNull final PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        final ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.COMPASS) return;
        final ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(trackerKey, PersistentDataType.BYTE)) return;

        final Player player = event.getPlayer();
        final long now = System.currentTimeMillis();
        final Long last = lastUse.get(player.getUniqueId());
        if (last != null && now - last < USE_COOLDOWN_MS) return;
        lastUse.put(player.getUniqueId(), now);

        final IDungeonInstance raw = instanceManager.getInstanceForPlayer(player);
        if (!(raw instanceof final DungeonInstance instance) || instance.getRoomGraph() == null) {
            // Souvenir from a past run — dissolve it to keep inventories clean.
            player.getInventory().remove(item);
            MiniMessageUtil.send(player, "<gray>The tracker's magic fades away…");
            return;
        }

        final RoomData target = nearestUnexplored(instance.getRoomGraph(), player.getLocation());
        if (target == null) {
            MiniMessageUtil.send(player, "<green>Every room has been explored!");
            return;
        }
        final Location centre = target.getCentre();
        player.setCompassTarget(centre);
        final int distance = (int) Math.round(player.getLocation().distance(centre));
        MiniMessageUtil.send(player, "<gold>Tracker: <yellow>unexplored room <white>" + distance
                + "m " + cardinal(player.getLocation(), centre) + "<yellow> — follow the needle.");
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.6f);
    }

    @Nullable
    private RoomData nearestUnexplored(@NotNull final RoomGraph graph, @NotNull final Location from) {
        RoomData best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (final RoomData room : graph.getRooms()) {
            if (room.isEntered()) continue;
            final double d = room.getCentre().distanceSquared(from);
            if (d < bestDistSq) {
                bestDistSq = d;
                best = room;
            }
        }
        return best;
    }

    @NotNull
    private String cardinal(@NotNull final Location from, @NotNull final Location to) {
        final double dx = to.getX() - from.getX();
        final double dz = to.getZ() - from.getZ();
        if (Math.abs(dx) > Math.abs(dz)) return dx > 0 ? "east" : "west";
        return dz > 0 ? "south" : "north";
    }
}
