package com.ultimatedungeon.boss.abilities;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

/** Warps the battlefield, afflicting nearby players with debilitating effects. */
public final class EnvironmentAbility extends AbstractBossAbility {
    public EnvironmentAbility(@NotNull final String id, final double damage, final long cooldownTicks, final double range) {
        super(id, damage, cooldownTicks, range);
    }
    @Override
    protected void perform(@NotNull final LivingEntity boss) {
        if (boss.getWorld() != null) {
            boss.getWorld().spawnParticle(Particle.SMOKE, boss.getLocation(), 60, range / 2, 1, range / 2, 0.01);
            boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.8f, 1.0f);
        }
        for (final Player p : nearbyPlayers(boss)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 0));
            if (damage > 0) p.damage(damage, boss);
        }
    }
}
