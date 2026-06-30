package com.ultimatedungeon.listeners.arena;

import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.boss.arena.ArenaLockdownManager;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

/** Blocks player-initiated escape teleports during a locked boss encounter. */
public final class ArenaTeleportBlockListener implements Listener {

    private final ArenaLockdownManager lockdown;
    private final DungeonInstanceManager instanceManager;

    public ArenaTeleportBlockListener(@NotNull final ArenaLockdownManager lockdown,
                                      @NotNull final DungeonInstanceManager instanceManager) {
        this.lockdown = lockdown;
        this.instanceManager = instanceManager;
    }

    @EventHandler
    public void onTeleport(@NotNull final PlayerTeleportEvent event) {
        final IDungeonInstance instance = instanceManager.getInstanceForPlayer(event.getPlayer());
        if (instance == null || !lockdown.isLocked(instance.getInstanceId())) return;
        switch (event.getCause()) {
            case ENDER_PEARL, CHORUS_FRUIT, COMMAND, SPECTATE -> event.setCancelled(true);
            default -> { /* allow plugin teleports (our own knock-back / mechanics) */ }
        }
    }
}
