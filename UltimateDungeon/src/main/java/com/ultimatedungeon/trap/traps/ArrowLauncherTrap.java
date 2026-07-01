package com.ultimatedungeon.trap.traps;

import com.ultimatedungeon.trap.model.TrapDefinition;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/** Fires arrows at the nearest player from a wall-mounted dispenser. */
public final class ArrowLauncherTrap extends AbstractTrap {
    public ArrowLauncherTrap(@NotNull final TrapDefinition definition) { super(definition); }
    @Override
    protected void onActivate() {
        if (location == null || location.getWorld() == null) return;
        sound(Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);
        Player nearest = null; double best = Double.MAX_VALUE;
        final double r = Math.max(8.0, definition.getTriggerRadius() + 12.0);
        for (final Player p : location.getWorld().getPlayers()) {
            final double d = p.getLocation().distanceSquared(location);
            if (d < best && d <= r * r) { best = d; nearest = p; }
        }
        if (nearest == null) { particle(Particle.CRIT, 5); return; }
        final Vector dir = nearest.getEyeLocation().toVector().subtract(location.toVector());
        if (dir.lengthSquared() < 1.0E-4) return;
        location.getWorld().spawnArrow(location.clone().add(0.5, 1.0, 0.5), dir.normalize(), 2.5f, 4.0f);
    }
}
