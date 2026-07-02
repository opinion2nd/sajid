package com.geodash.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * Scheduling + teleport abstraction that works on both classic Bukkit-scheduler
 * servers (Bukkit/Spigot/Paper/Purpur) and regionised Folia servers.
 * Folia APIs are invoked reflectively so the plugin compiles against the
 * plain Bukkit API and still loads everywhere.
 */
public final class CompatScheduler {

    public interface Task {
        void cancel();
    }

    private static final boolean FOLIA = detectFolia();
    private static Method entityGetScheduler;
    private static Method entityRunAtFixedRate;
    private static Method entityRunDelayed;
    private static Method serverGetGlobalScheduler;
    private static Method globalRunAtFixedRate;
    private static Method globalRunDelayed;
    private static Method serverGetAsyncScheduler;
    private static Method asyncRunNow;
    private static Method scheduledTaskCancel;
    private static Method teleportAsync;

    static {
        if (FOLIA) {
            try {
                entityGetScheduler = Entity.class.getMethod("getScheduler");
                Class<?> entitySched = entityGetScheduler.getReturnType();
                entityRunAtFixedRate = entitySched.getMethod("runAtFixedRate",
                        Plugin.class, Consumer.class, Runnable.class, long.class, long.class);
                entityRunDelayed = entitySched.getMethod("runDelayed",
                        Plugin.class, Consumer.class, Runnable.class, long.class);
                serverGetGlobalScheduler = Bukkit.getServer().getClass().getMethod("getGlobalRegionScheduler");
                Class<?> globalSched = serverGetGlobalScheduler.getReturnType();
                globalRunAtFixedRate = globalSched.getMethod("runAtFixedRate",
                        Plugin.class, Consumer.class, long.class, long.class);
                globalRunDelayed = globalSched.getMethod("runDelayed",
                        Plugin.class, Consumer.class, long.class);
                serverGetAsyncScheduler = Bukkit.getServer().getClass().getMethod("getAsyncScheduler");
                Class<?> asyncSched = serverGetAsyncScheduler.getReturnType();
                asyncRunNow = asyncSched.getMethod("runNow", Plugin.class, Consumer.class);
                Class<?> scheduledTask = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
                scheduledTaskCancel = scheduledTask.getMethod("cancel");
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
        try {
            teleportAsync = Entity.class.getMethod("teleportAsync", Location.class);
        } catch (NoSuchMethodException ignored) {
            // plain Bukkit/Spigot - sync teleport is fine there
        }
    }

    private CompatScheduler() {
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

    /** Repeating task pinned to an entity's region (falls back to the main thread scheduler). */
    public static Task runEntityTimer(Plugin plugin, Entity entity, Runnable run, long delay, long period) {
        if (FOLIA) {
            try {
                Object scheduler = entityGetScheduler.invoke(entity);
                Consumer<Object> consumer = t -> run.run();
                Object task = entityRunAtFixedRate.invoke(scheduler, plugin, consumer, null,
                        Math.max(1, delay), Math.max(1, period));
                return foliaTask(task);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Folia entity scheduler failed", e);
            }
        }
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, run, delay, period);
        return task::cancel;
    }

    /** Delayed task pinned to an entity's region. */
    public static Task runEntityLater(Plugin plugin, Entity entity, Runnable run, long delay) {
        if (FOLIA) {
            try {
                Object scheduler = entityGetScheduler.invoke(entity);
                Consumer<Object> consumer = t -> run.run();
                Object task = entityRunDelayed.invoke(scheduler, plugin, consumer, null, Math.max(1, delay));
                return foliaTask(task);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Folia entity scheduler failed", e);
            }
        }
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, run, delay);
        return task::cancel;
    }

    /** Repeating task on the global region / main thread. Do not touch entities or blocks from here on Folia. */
    public static Task runGlobalTimer(Plugin plugin, Runnable run, long delay, long period) {
        if (FOLIA) {
            try {
                Object scheduler = serverGetGlobalScheduler.invoke(Bukkit.getServer());
                Consumer<Object> consumer = t -> run.run();
                Object task = globalRunAtFixedRate.invoke(scheduler, plugin, consumer,
                        Math.max(1, delay), Math.max(1, period));
                return foliaTask(task);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Folia global scheduler failed", e);
            }
        }
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, run, delay, period);
        return task::cancel;
    }

    /** Delayed task on the global region / main thread. */
    public static Task runGlobalLater(Plugin plugin, Runnable run, long delay) {
        if (FOLIA) {
            try {
                Object scheduler = serverGetGlobalScheduler.invoke(Bukkit.getServer());
                Consumer<Object> consumer = t -> run.run();
                Object task = globalRunDelayed.invoke(scheduler, plugin, consumer, Math.max(1, delay));
                return foliaTask(task);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Folia global scheduler failed", e);
            }
        }
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, run, delay);
        return task::cancel;
    }

    public static void runAsync(Plugin plugin, Runnable run) {
        if (FOLIA) {
            try {
                Object scheduler = serverGetAsyncScheduler.invoke(Bukkit.getServer());
                Consumer<Object> consumer = t -> run.run();
                asyncRunNow.invoke(scheduler, plugin, consumer);
                return;
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Folia async scheduler failed", e);
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, run);
    }

    /** Region-safe teleport: async on Paper/Folia, sync elsewhere. */
    public static void teleport(Entity entity, Location target) {
        if (teleportAsync != null) {
            try {
                teleportAsync.invoke(entity, target);
                return;
            } catch (ReflectiveOperationException ignored) {
                // fall through to sync teleport
            }
        }
        entity.teleport(target);
    }

    private static Task foliaTask(Object task) {
        return () -> {
            try {
                scheduledTaskCancel.invoke(task);
            } catch (ReflectiveOperationException ignored) {
            }
        };
    }
}
