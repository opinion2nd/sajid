package com.ultimatedungeon.trap.traps;

import com.ultimatedungeon.trap.model.TrapDefinition;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

/** Releases a toxic cloud applying its configured status effects. */
public final class PoisonAreaTrap extends AbstractTrap {
    public PoisonAreaTrap(@NotNull final TrapDefinition definition) { super(definition); }
    @Override
    protected void onActivate() {
        particle(Particle.SMOKE, 40);
        sound(Sound.ENTITY_SPLASH_POTION_BREAK, 1.0f, 1.0f);
        damageNearby();
    }
}
