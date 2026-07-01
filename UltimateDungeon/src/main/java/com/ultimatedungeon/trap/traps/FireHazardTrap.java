package com.ultimatedungeon.trap.traps;

import com.ultimatedungeon.trap.model.TrapDefinition;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** A jet of flame ignites everyone in range. */
public final class FireHazardTrap extends AbstractTrap {
    public FireHazardTrap(@NotNull final TrapDefinition definition) { super(definition); }
    @Override
    protected void onActivate() {
        if (location == null || location.getWorld() == null) return;
        particle(Particle.FLAME, 40);
        sound(Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.0f);
        final double r = Math.max(1.0, definition.getTriggerRadius());
        for (final var e : location.getWorld().getNearbyEntities(location, r, r, r)) {
            if (e instanceof final Player player
                    && player.getGameMode() != org.bukkit.GameMode.CREATIVE
                    && player.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
                player.setFireTicks(80);
            }
        }
        damageNearby();
    }
}
