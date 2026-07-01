package com.ultimatedungeon.listeners.arena;

import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.boss.arena.ArenaLockdownManager;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.jetbrains.annotations.NotNull;

/** Blocks portal use during a locked boss encounter. */
public final class ArenaPortalBlockListener implements Listener {

    private final ArenaLockdownManager lockdown;
    private final DungeonInstanceManager instanceManager;

    public ArenaPortalBlockListener(@NotNull final ArenaLockdownManager lockdown,
                                    @NotNull final DungeonInstanceManager instanceManager) {
        this.lockdown = lockdown;
        this.instanceManager = instanceManager;
    }

    @EventHandler
    public void onPortal(@NotNull final PlayerPortalEvent event) {
        final IDungeonInstance instance = instanceManager.getInstanceForPlayer(event.getPlayer());
        if (instance != null && lockdown.isLocked(instance.getInstanceId())) {
            event.setCancelled(true);
        }
    }
}
