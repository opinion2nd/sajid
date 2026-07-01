package com.ultimatedungeon.listeners.arena;

import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.boss.arena.ArenaEscapeBlocker;
import com.ultimatedungeon.boss.arena.ArenaLockdownManager;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.room.model.RoomData;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** Keeps players inside a locked boss arena during the encounter. */
public final class ArenaEscapeListener implements Listener {

    private final ArenaLockdownManager lockdown;
    private final ArenaEscapeBlocker blocker;
    private final DungeonInstanceManager instanceManager;

    public ArenaEscapeListener(@NotNull final ArenaLockdownManager lockdown,
                               @NotNull final ArenaEscapeBlocker blocker,
                               @NotNull final DungeonInstanceManager instanceManager) {
        this.lockdown = lockdown;
        this.blocker = blocker;
        this.instanceManager = instanceManager;
    }

    @EventHandler
    public void onMove(@NotNull final PlayerMoveEvent event) {
        final Location to = event.getTo();
        if (to == null) return;
        final IDungeonInstance instance = instanceManager.getInstanceForPlayer(event.getPlayer());
        if (instance == null) return;
        final UUID id = instance.getInstanceId();
        if (!lockdown.isLocked(id)) return;
        final RoomData arena = lockdown.getArena(id);
        if (arena != null && blocker.isOutside(arena, to)) {
            blocker.pushBack(event.getPlayer(), arena);
        }
    }
}
