package com.ultimatedungeon.trap.traps;

import com.ultimatedungeon.trap.model.TrapDefinition;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

/** Spikes erupt from the floor, damaging and lightly launching anyone above. */
public final class SpikeTrap extends AbstractTrap {
    public SpikeTrap(@NotNull final TrapDefinition definition) { super(definition); }
    @Override
    protected void onActivate() {
        particle(Particle.CRIT, 30);
        sound(Sound.ENTITY_PLAYER_HURT, 1.0f, 0.8f);
        damageNearby();
    }
}
