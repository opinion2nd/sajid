package com.ultimatedungeon.trap.traps;

import com.ultimatedungeon.trap.model.TrapDefinition;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Drops a crushing block onto each nearby player from the ceiling. */
public final class FallingBlockTrap extends AbstractTrap {
    public FallingBlockTrap(@NotNull final TrapDefinition definition) { super(definition); }
    @Override
    protected void onActivate() {
        if (location == null || location.getWorld() == null) return;
        sound(Sound.BLOCK_ANVIL_LAND, 1.0f, 0.7f);
        final double r = Math.max(1.0, definition.getTriggerRadius());
        for (final var e : location.getWorld().getNearbyEntities(location, r, r, r)) {
            if (!(e instanceof final Player player)) continue;
            location.getWorld().spawnFallingBlock(
                    player.getLocation().clone().add(0, 5, 0), Material.ANVIL.createBlockData());
        }
        damageNearby();
    }
}
