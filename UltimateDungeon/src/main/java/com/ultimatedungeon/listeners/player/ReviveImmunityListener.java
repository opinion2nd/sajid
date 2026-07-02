package com.ultimatedungeon.listeners.player;

import com.ultimatedungeon.services.ReviveManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Enforces the post-revive grace period ({@code revive.immunity-seconds} in
 * party.yml): a freshly revived player takes no damage from any source —
 * mobs, players, fire, fall, traps — and hostile mobs stop targeting them.
 */
public final class ReviveImmunityListener implements Listener {

    private final ReviveManager reviveManager;

    public ReviveImmunityListener(@NotNull final ReviveManager reviveManager) {
        this.reviveManager = reviveManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(@NotNull final EntityDamageEvent event) {
        if (event.getEntity() instanceof final Player player
                && reviveManager.isImmune(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(@NotNull final EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof final Player player
                && reviveManager.isImmune(player)) {
            event.setCancelled(true);
        }
    }
}
