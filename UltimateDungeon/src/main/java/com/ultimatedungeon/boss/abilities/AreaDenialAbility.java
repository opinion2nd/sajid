package com.ultimatedungeon.boss.abilities;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Slams the ground, damaging and knocking back everyone in range. */
public final class AreaDenialAbility extends AbstractBossAbility {
    public AreaDenialAbility(@NotNull final String id, final double damage, final long cooldownTicks, final double range) {
        super(id, damage, cooldownTicks, range);
    }
    @Override
    protected void perform(@NotNull final LivingEntity boss) {
        if (boss.getWorld() != null) {
            boss.getWorld().spawnParticle(Particle.EXPLOSION, boss.getLocation(), 6, range / 2, 0.5, range / 2, 0.0);
            boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
        }
        for (final Player p : nearbyPlayers(boss)) {
            if (damage > 0) p.damage(damage, boss);
            p.setVelocity(p.getLocation().toVector().subtract(boss.getLocation().toVector())
                    .normalize().multiply(1.2).setY(0.5));
        }
    }
}
