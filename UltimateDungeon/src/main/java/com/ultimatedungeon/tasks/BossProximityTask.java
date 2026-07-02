package com.ultimatedungeon.tasks;

import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.dungeon.instance.DungeonInstance;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.room.model.RoomGraph;
import com.ultimatedungeon.room.model.RoomType;
import com.ultimatedungeon.services.NotificationService;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Atmospheric cue near boss rooms: players approaching an unentered boss room
 * hear a low heartbeat and get an action-bar warning, so the boss arena feels
 * dangerous before the door — and is findable without breaking exploration.
 */
public final class BossProximityTask extends BukkitRunnable {

    /** Blocks from the boss-room centre at which the heartbeat starts. */
    private static final double SENSE_RANGE = 22.0;
    /** Minimum ms between cues for the same player, so it stays a pulse. */
    private static final long CUE_INTERVAL_MS = 4_000L;

    private final DungeonInstanceManager instances;
    private final NotificationService notifications;
    private final Map<UUID, Long> lastCue = new ConcurrentHashMap<>();

    public BossProximityTask(@NotNull final DungeonInstanceManager instances,
                             @NotNull final NotificationService notifications) {
        this.instances = instances;
        this.notifications = notifications;
    }

    @Override
    public void run() {
        for (final IDungeonInstance raw : instances.getActiveInstances()) {
            if (!(raw instanceof final DungeonInstance instance)) continue;
            final RoomGraph graph = instance.getRoomGraph();
            if (graph == null) continue;
            for (final RoomData room : graph.getRoomsOfType(RoomType.BOSS)) {
                if (room.isEntered()) continue;
                cueNearbyPlayers(room);
            }
        }
        // Drop stale cooldown entries so the map never outlives its players.
        final long cutoff = System.currentTimeMillis() - CUE_INTERVAL_MS * 4;
        lastCue.values().removeIf(t -> t < cutoff);
    }

    private void cueNearbyPlayers(@NotNull final RoomData room) {
        final Location centre = room.getCentre();
        if (centre.getWorld() == null) return;
        final double rangeSq = SENSE_RANGE * SENSE_RANGE;
        final long now = System.currentTimeMillis();
        for (final Player p : centre.getWorld().getPlayers()) {
            if (p.getLocation().distanceSquared(centre) > rangeSq) continue;
            if (room.contains(p.getLocation())) continue; // already at the door
            final Long last = lastCue.get(p.getUniqueId());
            if (last != null && now - last < CUE_INTERVAL_MS) continue;
            lastCue.put(p.getUniqueId(), now);
            p.playSound(p.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 0.9f, 0.7f);
            notifications.actionBar(p, "<dark_red>❤ <red>Something powerful stirs nearby…");
        }
    }
}
