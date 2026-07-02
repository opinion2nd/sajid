package com.ultimatedungeon.listeners.player;

import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.dungeon.lifecycle.ReviveManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Intercepts otherwise-fatal damage to dungeon players. If the player still has
 * a living teammate, they are downed (spectator, revivable) instead of dying;
 * with no teammates left the damage proceeds and their death fails the run.
 */
public final class DungeonDownListener implements Listener {

    private final DungeonInstanceManager instanceManager;
    private final ReviveManager reviveManager;

    public DungeonDownListener(@NotNull final DungeonInstanceManager instanceManager,
                              @NotNull final ReviveManager reviveManager) {
        this.instanceManager = instanceManager;
        this.reviveManager = reviveManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(@NotNull final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof final Player player)) return;
        if (reviveManager.isDown(player)) { // spectators shouldn't take damage anyway
            event.setCancelled(true);
            return;
        }
        final IDungeonInstance instance = instanceManager.getInstanceForPlayer(player);
        if (instance == null) return;
        // Would this hit drop the player? getFinalDamage accounts for armour/effects.
        if (player.getHealth() - event.getFinalDamage() > 0.0) return;
        if (!reviveManager.hasAliveTeammate(player, instance.getInstanceId())) return;

        // Save them from dying — down instead.
        event.setCancelled(true);
        reviveManager.down(player, instance.getInstanceId());
    }
}
