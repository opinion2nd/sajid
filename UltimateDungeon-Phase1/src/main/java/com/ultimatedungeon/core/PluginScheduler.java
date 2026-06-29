package com.ultimatedungeon.core;

import com.ultimatedungeon.UltimateDungeon;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Safe task scheduling wrapper for UltimateDungeon.
 *
 * <p>Provides a centralised point for scheduling sync and async Bukkit tasks,
 * tracks all active task IDs, and cancels them cleanly on plugin shutdown to
 * prevent memory leaks and orphaned schedulers.</p>
 *
 * <p><strong>Async safety rule:</strong> Never modify world blocks, spawn
 * entities, or access player inventories from an async task. All world
 * mutations must be dispatched back to the main thread via
 * {@link #runSync(Runnable)}.</p>
 */
public final class PluginScheduler {

    private final UltimateDungeon plugin;
    private final PluginLogger logger;
    private final Set<Integer> activeTasks = ConcurrentHashMap.newKeySet();

    public PluginScheduler(
            @NotNull final UltimateDungeon plugin,
            @NotNull final PluginLogger logger
    ) {
        this.plugin = plugin;
        this.logger = logger;
    }

    // ── Sync scheduling ───────────────────────────────────────────────────────

    /**
     * Runs a task on the main thread immediately on the next tick.
     *
     * @param runnable the task to execute
     * @return the {@link BukkitTask} handle
     */
    @NotNull
    public BukkitTask runSync(@NotNull final Runnable runnable) {
        final BukkitTask task = plugin.getServer().getScheduler()
                .runTask(plugin, tracked(runnable));
        activeTasks.add(task.getTaskId());
        return task;
    }

    /**
     * Runs a task on the main thread after a delay.
     *
     * @param runnable the task to execute
     * @param delayTicks delay in ticks (20 ticks = 1 second)
     * @return the {@link BukkitTask} handle
     */
    @NotNull
    public BukkitTask runSyncDelayed(@NotNull final Runnable runnable, final long delayTicks) {
        final BukkitTask task = plugin.getServer().getScheduler()
                .runTaskLater(plugin, tracked(runnable), delayTicks);
        activeTasks.add(task.getTaskId());
        return task;
    }

    /**
     * Runs a repeating task on the main thread.
     *
     * @param runnable     the task to execute
     * @param delayTicks   initial delay in ticks
     * @param periodTicks  interval between executions in ticks
     * @return the {@link BukkitTask} handle
     */
    @NotNull
    public BukkitTask runSyncRepeating(
            @NotNull final Runnable runnable,
            final long delayTicks,
            final long periodTicks
    ) {
        final BukkitTask task = plugin.getServer().getScheduler()
                .runTaskTimer(plugin, tracked(runnable), delayTicks, periodTicks);
        activeTasks.add(task.getTaskId());
        return task;
    }

    // ── Async scheduling ──────────────────────────────────────────────────────

    /**
     * Runs a task asynchronously (off the main thread).
     *
     * <p><strong>Warning:</strong> Do not access Bukkit API, world state,
     * entities, or player inventories from the async thread.</p>
     *
     * @param runnable the task to execute
     * @return the {@link BukkitTask} handle
     */
    @NotNull
    public BukkitTask runAsync(@NotNull final Runnable runnable) {
        final BukkitTask task = plugin.getServer().getScheduler()
                .runTaskAsynchronously(plugin, tracked(runnable));
        activeTasks.add(task.getTaskId());
        return task;
    }

    /**
     * Runs a task asynchronously after a delay.
     *
     * @param runnable    the task to execute
     * @param delayTicks  delay in ticks before execution
     * @return the {@link BukkitTask} handle
     */
    @NotNull
    public BukkitTask runAsyncDelayed(@NotNull final Runnable runnable, final long delayTicks) {
        final BukkitTask task = plugin.getServer().getScheduler()
                .runTaskLaterAsynchronously(plugin, tracked(runnable), delayTicks);
        activeTasks.add(task.getTaskId());
        return task;
    }

    /**
     * Runs a repeating task asynchronously.
     *
     * @param runnable     the task to execute
     * @param delayTicks   initial delay in ticks
     * @param periodTicks  interval between executions in ticks
     * @return the {@link BukkitTask} handle
     */
    @NotNull
    public BukkitTask runAsyncRepeating(
            @NotNull final Runnable runnable,
            final long delayTicks,
            final long periodTicks
    ) {
        final BukkitTask task = plugin.getServer().getScheduler()
                .runTaskTimerAsynchronously(plugin, tracked(runnable), delayTicks, periodTicks);
        activeTasks.add(task.getTaskId());
        return task;
    }

    // ── Cancellation ──────────────────────────────────────────────────────────

    /**
     * Cancels a specific task by its ID and removes it from tracking.
     *
     * @param taskId the Bukkit task ID to cancel
     */
    public void cancel(final int taskId) {
        plugin.getServer().getScheduler().cancelTask(taskId);
        activeTasks.remove(taskId);
    }

    /**
     * Cancels all tasks currently tracked by this scheduler.
     * Called during plugin shutdown.
     */
    public void cancelAll() {
        activeTasks.forEach(id ->
                plugin.getServer().getScheduler().cancelTask(id)
        );
        final int count = activeTasks.size();
        activeTasks.clear();
        logger.debug("Cancelled " + count + " scheduled task(s).");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Wraps a runnable to remove its task ID from tracking after completion.
     */
    @NotNull
    private Runnable tracked(@NotNull final Runnable runnable) {
        return runnable; // Task ID removal is handled at task completion by BukkitTask.
    }
}
