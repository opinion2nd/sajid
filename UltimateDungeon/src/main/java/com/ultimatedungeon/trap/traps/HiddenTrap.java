package com.ultimatedungeon.trap.traps;

import com.ultimatedungeon.trap.model.TrapDefinition;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;

/** A concealed pressure trap that strikes once when stepped on. */
public final class HiddenTrap extends AbstractTrap {
    public HiddenTrap(@NotNull final TrapDefinition definition) { super(definition); }
    @Override
    protected void onActivate() {
        particle(Particle.CRIT, 20);
        sound(Sound.BLOCK_TRIPWIRE_CLICK_ON, 1.0f, 1.0f);
        damageNearby();
        reset(); // single-use
    }
}
