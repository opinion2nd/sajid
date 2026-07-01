package com.ultimatedungeon.trap.traps;

import com.ultimatedungeon.trap.model.TrapDefinition;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

/** Detonates a non-destructive blast, damaging and flinging nearby players. */
public final class ExplosiveTrap extends AbstractTrap {
    public ExplosiveTrap(@NotNull final TrapDefinition definition) { super(definition); }
    @Override
    protected void onActivate() {
        if (location == null || location.getWorld() == null) return;
        particle(Particle.EXPLOSION, 5);
        sound(Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        // Cosmetic + scripted damage only; never break dungeon blocks.
        location.getWorld().createExplosion(location, 0.0f, false, false);
        damageNearby();
    }
}
