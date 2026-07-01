package com.ultimatedungeon.dungeon.instance;

import com.ultimatedungeon.core.PluginScheduler;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Runs the pre-encounter countdown for a wave or boss room. The encounter only
 * starts if players are still inside when the timer reaches zero; if everyone
 * leaves the countdown is cancelled, and re-entering starts a fresh one.
 */
public final class EncounterCountdownManager {

    private final PluginScheduler scheduler;
    private final Map<String, BukkitTask> active = new ConcurrentHashMap<>();

    public EncounterCountdownManager(@NotNull final PluginScheduler scheduler) {
        this.scheduler = scheduler;
    }

    private static String key(final UUID instanceId, final RoomData room) {
        return instanceId + ":" + room.getRoomId();
    }

    public boolean isArming(@NotNull final UUID instanceId, @NotNull final RoomData room) {
        return active.containsKey(key(instanceId, room));
    }

    /**
     * Starts a {@code seconds} countdown for the room. Cancels itself if the room
     * empties; runs {@code onStart} on the main thread if players remain at zero.
     */
    public void arm(@NotNull final UUID instanceId, @NotNull final RoomData room, final int seconds,
                    @NotNull final Supplier<List<Player>> playersInRoom, @NotNull final Runnable onStart) {
        final String k = key(instanceId, room);
        if (active.containsKey(k)) return;
        final int[] remaining = {Math.max(1, seconds)};
        final BukkitTask task = scheduler.runSyncRepeating(() -> {
            final List<Player> players = playersInRoom.get();
            if (players.isEmpty()) {
                cancel(k);
                return;
            }
            if (remaining[0] <= 0) {
                cancel(k);
                onStart.run();
                return;
            }
            for (final Player p : players) {
                MiniMessageUtil.sendTitle(p, "<yellow><bold>" + remaining[0],
                        "<gray>The room seals in " + remaining[0] + "s...", 0, 25, 5);
            }
            remaining[0]--;
        }, 0L, 20L);
        active.put(k, task);
    }

    private void cancel(@NotNull final String k) {
        final BukkitTask t = active.remove(k);
        if (t != null) t.cancel();
    }

    /** Cancels any pending countdowns for an instance (e.g. on teardown). */
    public void cancelInstance(@NotNull final UUID instanceId) {
        final String prefix = instanceId + ":";
        active.keySet().stream().filter(k -> k.startsWith(prefix)).toList()
                .forEach(this::cancel);
    }
}
