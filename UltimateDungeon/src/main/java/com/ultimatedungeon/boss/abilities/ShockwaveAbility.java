package com.ultimatedungeon.boss.abilities;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * A shockwave that yanks nearby players toward the boss and damages them —
 * distinct from the AoE blast (which knocks players away). No minions are
 * summoned; bosses fight alone.
 */
public final class ShockwaveAbility extends AbstractBossAbility {
    public ShockwaveAbility(@NotNull final String id, final double damage,
                            final long cooldownTicks, final double range) {
        super(id, damage, cooldownTicks, range);
    }

    @Override
    protected void perform(@NotNull final LivingEntity boss) {
        if (boss.getWorld() != null) {
            boss.getWorld().spawnParticle(Particle.SONIC_BOOM, boss.getLocation().add(0, 1, 0), 1);
            boss.getWorld().spawnParticle(Particle.CRIT, boss.getLocation(), 30, range / 2, 0.5, range / 2, 0.1);
            boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.0f);
        }
        for (final Player p : nearbyPlayers(boss)) {
            final Vector pull = boss.getLocation().toVector().subtract(p.getLocation().toVector());
            if (pull.lengthSquared() > 1.0E-4) p.setVelocity(pull.normalize().multiply(0.9).setY(0.2));
            if (damage > 0) p.damage(damage, boss);
        }
    }
}
