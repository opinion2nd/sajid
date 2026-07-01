package com.ultimatedungeon.trap.traps;

import com.ultimatedungeon.trap.model.TrapDefinition;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

/** A swinging obstacle that knocks players back forcefully. */
public final class MovingObstacleTrap extends AbstractTrap {
    public MovingObstacleTrap(@NotNull final TrapDefinition definition) { super(definition); }
    @Override
    protected void onActivate() {
        particle(Particle.CRIT, 10);
        sound(Sound.BLOCK_PISTON_EXTEND, 1.0f, 0.6f);
        damageNearby();
    }
}
