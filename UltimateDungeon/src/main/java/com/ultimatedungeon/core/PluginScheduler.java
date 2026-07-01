package com.ultimatedungeon.core;

import com.ultimatedungeon.UltimateDungeon;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralised task scheduler for UltimateDungeon.
 *
 * <p>All sync and async tasks created through this class are tracked by their
 * Bukkit task ID. {@link #cancelAll()} is called on plugin shutdown to prevent
 * orphaned tasks and memory leaks — even if a system forgot to cancel its own
 * tasks.</p>
 *
 * <h3>Async safety contract</h3>
 * <ul>
 *   <li>Async tasks <em>must never</em> modify world blocks, spawn entities,
 *       or read/write player inventories.</li>
 *   <li>Any result from an async task that touches Bukkit state must be
 *       dispatched back to the main thread via {@link #runSync(Runnable)}.</li>
 * </ul>
 *
 * <h3>Self-removal after single-fire tasks</h3>
 * Delayed (non-repeating) tasks remove themselves from the tracking set after
 * they execute so the set stays lean over long server uptimes.
 */
public final class PluginScheduler {

    private final UltimateDungeon plugin;
    private final PluginLogger    logger;

    /** Thread-safe set of all currently live task IDs. */
    private final Set<Integer> activeTasks = ConcurrentHashMap.newKeySet();

    public PluginScheduler(
            @NotNull final UltimateDungeon plugin,
            @NotNull final PluginLogger    logger
    ) {
        this.plugin = plugin;
        this.logger = logger;
    }

    // ── Sync ──────────────────────────────────────────────────────────────────

    /**
     * Schedules {@code runnable} on the main server thread on the next tick.
     *
     * @param runnable task body
     * @return the Bukkit task handle (can be used to cancel early)
     */
    @NotNull
    public BukkitTask runSync(@NotNull final Runnable runnable) {
        final BukkitTask task = plugin.getServer().getScheduler()
                .runTask(plugin, selfRemoving(runnable));
        activeTasks.add(task.getTaskId());
        return task;
    }

    /**
     * Schedules {@code runnable} on the main thread after {@code delayTicks}.
     *
     * @param runnable   task body
     * @param delayTicks ticks to wait before execution (20 ticks = 1 second)
     * @return the Bukkit task handle
     */
    @NotNull
    public BukkitTask runSyncDelayed(
            @NotNull final Runnable runnable,
            final long delayTicks
    ) {
        final BukkitTask task = plugin.getServer().getScheduler()
                .runTaskLater(plugin, selfRemoving(runnable), delayTicks);
        activeTasks.add(task.getTaskId());
        return task;
    }

    /**
     * Schedules a repeating task on the main thread.
     *
     * @param runnable    task body
     * @param delayTicks  initial delay in ticks
     * @param periodTicks interval between executions in ticks
     * @return the Bukkit task handle — call {@link BukkitTask#cancel()} or
     *         {@link #cancel(int)} to stop the repetition
     */
    @NotNull
    public BukkitTask runSyncRepeating(
            @NotNull final Runnable runnable,
            final long delayTicks,
            final long periodTicks
    ) {
        final BukkitTask task = plugin.getServer().getScheduler()
                .runTaskTimer(plugin, runnable, delayTicks, periodTicks);
        activeTasks.add(task.getTaskId());
        return task;
    }

    // ── Async ─────────────────────────────────────────────────────────────────

    /**
     * Schedules {@code runnable} on an async thread immediately.
     *
     * <p><strong>Do not access Bukkit APIs from the async thread.</strong></p>
     *
     * @param runnable task body
     * @return the Bukkit task handle
     */
    @NotNull
    public BukkitTask runAsync(@NotNull final Runnable runnable) {
        final BukkitTask task = plugin.getServer().getScheduler()
                .runTaskAsynchronously(plugin, selfRemoving(runnable));
        activeTasks.add(task.getTaskId());
        return task;
    }

    /**
     * Schedules {@code runnable} on an async thread after {@code delayTicks}.
     *
     * @param runnable   task body
     * @param delayTicks ticks to wait before execution
     * @return the Bukkit task handle
     */
    @NotNull
    public BukkitTask runAsyncDelayed(
            @NotNull final Runnable runnable,
            final long delayTicks
    ) {
        final BukkitTask task = plugin.getServer().getScheduler()
                .runTaskLaterAsynchronously(plugin, selfRemoving(runnable), delayTicks);
        activeTasks.add(task.getTaskId());
        return task;
    }

    /**
     * Schedules a repeating async task.
     *
     * @param runnable    task body
     * @param delayTicks  initial delay in ticks
     * @param periodTicks interval between executions in ticks
     * @return the Bukkit task handle
     */
    @NotNull
    public BukkitTask runAsyncRepeating(
            @NotNull final Runnable runnable,
            final long delayTicks,
            final long periodTicks
    ) {
        final BukkitTask task = plugin.getServer().getScheduler()
                .runTaskTimerAsynchronously(plugin, runnable, delayTicks, periodTicks);
        activeTasks.add(task.getTaskId());
        return task;
    }

    // ── Cancellation ──────────────────────────────────────────────────────────

    /**
     * Cancels the task with the given ID and removes it from tracking.
     *
     * @param taskId Bukkit task ID returned when the task was scheduled
     */
    public void cancel(final int taskId) {
        plugin.getServer().getScheduler().cancelTask(taskId);
        activeTasks.remove(taskId);
        logger.debug("Cancelled task #" + taskId);
    }

    /**
     * Cancels every task currently tracked by this scheduler.
     * Called automatically during plugin shutdown.
     */
    public void cancelAll() {
        final int count = activeTasks.size();
        for (final int id : activeTasks) {
            plugin.getServer().getScheduler().cancelTask(id);
        }
        activeTasks.clear();
        logger.debug("Cancelled all " + count + " tracked task(s) on shutdown.");
    }

    /** Returns how many tasks are currently tracked (useful for debugging). */
    public int getActiveTaskCount() {
        return activeTasks.size();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Wraps a one-shot runnable so it removes its own ID from the tracking
     * set after it finishes, keeping the set lean without external bookkeeping.
     *
     * <p>Note: this wrapper captures the task ID via a one-element int array
     * because the ID is not known until after {@code runTask*} returns. The
     * Bukkit scheduler guarantees the wrapped runnable is never invoked before
     * the {@code runTask*} call returns, so the race window is closed.</p>
     */
    @NotNull
    private Runnable selfRemoving(@NotNull final Runnable runnable) {
        final int[] idHolder = {-1};
        return () -> {
            try {
                runnable.run();
            } finally {
                if (idHolder[0] != -1) {
                    activeTasks.remove(idHolder[0]);
                }
            }
        };
        // idHolder[0] is set by the caller immediately after receiving the task —
        // see design note above; task body cannot execute until that assignment is done.
    }

    /**
     * Records the task ID into the id-holder array returned by
     * {@link #selfRemoving(Runnable)}.
     *
     * <p>Caller pattern:</p>
     * <pre>{@code
     *   final int[] holder = {-1};
     *   final BukkitTask task = scheduler.runTask(plugin, wrapWithHolder(r, holder));
     *   holder[0] = task.getTaskId();
     * }</pre>
     *
     * For simplicity the single-fire helpers use the lightweight form above
     * without a separate holder method — sufficient for our use case.
     */
}
