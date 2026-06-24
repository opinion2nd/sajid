package dev.opinion2nd.antiespguard.paper.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Folia-aware scheduling helper.
 *
 * <p>On Folia there is no single main thread — work must run on the region
 * thread that owns the relevant entity/chunk. This helper detects Folia once
 * (by probing for {@code Bukkit.getGlobalRegionScheduler}) and routes tasks to
 * the correct scheduler, transparently falling back to the classic Bukkit
 * scheduler on Paper/Spigot/Purpur.</p>
 */
public final class Schedulers {

    private static final boolean FOLIA = detectFolia();

    private Schedulers() {
    }

    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isFolia() {
        return FOLIA;
    }

    /** Run a task on the thread that owns {@code entity} (Folia) or the main thread. */
    public static void runForEntity(Plugin plugin, Entity entity, Runnable task) {
        if (!FOLIA) {
            Bukkit.getScheduler().runTask(plugin, task);
            return;
        }
        try {
            Object scheduler = Entity.class.getMethod("getScheduler").invoke(entity);
            scheduler.getClass()
                    .getMethod("run", Plugin.class, java.util.function.Consumer.class, Runnable.class)
                    .invoke(scheduler, plugin, (java.util.function.Consumer<Object>) ignored -> task.run(), null);
        } catch (Throwable t) {
            // If the entity scheduler is unavailable, fall back to global.
            runGlobal(plugin, task);
        }
    }

    /** Run a task on the global region (Folia) or the main thread. */
    public static void runGlobal(Plugin plugin, Runnable task) {
        if (!FOLIA) {
            Bukkit.getScheduler().runTask(plugin, task);
            return;
        }
        try {
            Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            scheduler.getClass()
                    .getMethod("execute", Plugin.class, Runnable.class)
                    .invoke(scheduler, plugin, task);
        } catch (Throwable t) {
            task.run();
        }
    }

    /** Run a repeating async task; identical on Folia and Paper. */
    public static void runAsyncRepeating(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        if (!FOLIA) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
            return;
        }
        try {
            Object scheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
            long delayMs = delayTicks * 50L;
            long periodMs = periodTicks * 50L;
            scheduler.getClass()
                    .getMethod("runAtFixedRate", Plugin.class, java.util.function.Consumer.class,
                            long.class, long.class, java.util.concurrent.TimeUnit.class)
                    .invoke(scheduler, plugin,
                            (java.util.function.Consumer<Object>) ignored -> task.run(),
                            Math.max(1, delayMs), Math.max(1, periodMs),
                            java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (Throwable t) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
        }
    }
}
