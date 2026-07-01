package com.ultimatedungeon.tasks;

import org.bukkit.scheduler.BukkitRunnable;

/**
 * Ready-check expiry is handled internally by {@link com.ultimatedungeon.party.manager.ReadyCheckManager}
 * via a per-session {@link com.ultimatedungeon.core.PluginScheduler#runSyncDelayed} task.
 *
 * <p>This class is intentionally left as a no-op stub — it exists so the project
 * package structure remains complete. If a global poll-based approach is preferred
 * in the future, this task can be activated instead.</p>
 */
public final class ReadyCheckExpiryTask extends BukkitRunnable {

    @Override
    public void run() {
        // No-op: expiry handled per-session in ReadyCheckManager.
    }
}
