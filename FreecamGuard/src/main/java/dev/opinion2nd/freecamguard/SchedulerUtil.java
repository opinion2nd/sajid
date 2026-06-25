package dev.opinion2nd.freecamguard;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Tiny scheduling helper that works on both regular Bukkit/Paper/Purpur and on
 * Folia. On Folia we have to use the per-entity / global region schedulers,
 * which we reach through reflection so this class still compiles against the
 * plain Paper API. Everywhere else we just fall back to the classic
 * {@link org.bukkit.scheduler.BukkitScheduler}.
 */
public final class SchedulerUtil {

    private static final boolean FOLIA = detectFolia();

    private SchedulerUtil() {
    }

    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public static boolean isFolia() {
        return FOLIA;
    }

    /** Run a task that touches a specific entity, after a tick delay. */
    public static void runEntityLater(Plugin plugin, Entity entity, Runnable task, long delayTicks) {
        long delay = Math.max(1L, delayTicks);
        if (FOLIA) {
            try {
                Object scheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
                Method runDelayed = scheduler.getClass()
                        .getMethod("runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class);
                Consumer<Object> wrapped = ignored -> task.run();
                runDelayed.invoke(scheduler, plugin, wrapped, null, delay);
                return;
            } catch (Throwable ignored) {
                // fall through to Bukkit scheduler
            }
        }
        Bukkit.getScheduler().runTaskLater(plugin, task, delay);
    }

    /** Run a non-entity task on the global/main thread as soon as possible. */
    public static void runGlobal(Plugin plugin, Runnable task) {
        if (FOLIA) {
            try {
                Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                scheduler.getClass().getMethod("execute", Plugin.class, Runnable.class)
                        .invoke(scheduler, plugin, task);
                return;
            } catch (Throwable ignored) {
                // fall through to Bukkit scheduler
            }
        }
        Bukkit.getScheduler().runTask(plugin, task);
    }
}
