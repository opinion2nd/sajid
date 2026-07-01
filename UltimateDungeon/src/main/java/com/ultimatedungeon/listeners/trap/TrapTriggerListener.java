package com.ultimatedungeon.listeners.trap;

import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.trap.engine.TrapEngine;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

/** Fires movement-triggered traps when a player walks within range. */
public final class TrapTriggerListener implements Listener {

    private final TrapEngine trapEngine;
    private final DungeonInstanceManager instanceManager;

    public TrapTriggerListener(@NotNull final TrapEngine trapEngine,
                               @NotNull final DungeonInstanceManager instanceManager) {
        this.trapEngine = trapEngine;
        this.instanceManager = instanceManager;
    }

    @EventHandler
    public void onMove(@NotNull final PlayerMoveEvent event) {
        final Location from = event.getFrom();
        final Location to = event.getTo();
        if (to == null || (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())) {
            return;
        }
        final IDungeonInstance instance = instanceManager.getInstanceForPlayer(event.getPlayer());
        if (instance != null) {
            trapEngine.onPlayerMove(instance.getInstanceId(), event.getPlayer());
        }
    }
}
