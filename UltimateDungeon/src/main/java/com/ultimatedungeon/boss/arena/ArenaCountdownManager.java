package com.ultimatedungeon.boss.arena;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.core.PluginScheduler;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Runs the pre-fight countdown before a boss encounter, showing a ticking title
 * to the arena players and invoking a callback when it reaches zero.
 */
public final class ArenaCountdownManager {

    private final PluginScheduler scheduler;
    private final PluginLogger logger;

    public ArenaCountdownManager(@NotNull final PluginScheduler scheduler, @NotNull final PluginLogger logger) {
        this.scheduler = scheduler;
        this.logger = logger;
    }

    /** Starts a {@code seconds}-long countdown, then runs {@code onComplete} on the main thread. */
    public void start(final int seconds, @NotNull final Collection<? extends Player> players,
                      @NotNull final Runnable onComplete) {
        final List<Player> snapshot = new ArrayList<>(players);
        final int[] remaining = {Math.max(1, seconds)};
        final BukkitTask[] holder = new BukkitTask[1];
        holder[0] = scheduler.runSyncRepeating(() -> {
            if (remaining[0] <= 0) {
                for (final Player p : snapshot) {
                    MiniMessageUtil.sendTitle(p, "<red><bold>FIGHT!", "", 0, 20, 10);
                }
                if (holder[0] != null) holder[0].cancel();
                onComplete.run();
                return;
            }
            for (final Player p : snapshot) {
                MiniMessageUtil.sendTitle(p, "<yellow>" + remaining[0],
                        "<gray>The encounter begins...", 0, 25, 5);
            }
            remaining[0]--;
        }, 0L, 20L);
    }
}
