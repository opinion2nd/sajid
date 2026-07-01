package com.ultimatedungeon.monster.abilities;

import com.ultimatedungeon.monster.model.MonsterDefinition;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A monster ability built from a {@link MonsterDefinition.AbilitySpec}.
 *
 * <p>The concrete effect is chosen from the spec's shape so any configured
 * ability does something sensible without a per-id hard-code:
 * <ul>
 *   <li>long range + damage → a ranged bolt at the nearest player;</li>
 *   <li>damage, short/no range → a melee area strike with knockback;</li>
 *   <li>no damage → a support burst (self-haste + telegraph particles).</li>
 * </ul>
 */
public final class ConfiguredMonsterAbility extends AbstractMonsterAbility {

    private final double damage;
    private final double range;

    public ConfiguredMonsterAbility(@NotNull final MonsterDefinition.AbilitySpec spec) {
        super(spec.id(), spec.cooldownTicks());
        this.damage = spec.damage();
        this.range = spec.range();
    }

    @Override
    protected void perform(@NotNull final LivingEntity monster) {
        final World world = monster.getWorld();
        if (world == null) return;
        if (range >= 8.0 && damage > 0) {
            rangedBolt(monster, world);
        } else if (damage > 0) {
            meleeStrike(monster, world);
        } else {
            support(monster, world);
        }
    }

    private void rangedBolt(@NotNull final LivingEntity monster, @NotNull final World world) {
        final Player target = nearest(monster, range);
        if (target == null) return;
        world.spawnParticle(Particle.CRIT, monster.getEyeLocation(), 12, 0.2, 0.2, 0.2, 0.1);
        world.playSound(monster.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.2f);
        target.getWorld().spawnParticle(Particle.CRIT, target.getEyeLocation(), 8, 0.2, 0.2, 0.2, 0.0);
        target.damage(damage, monster);
    }

    private void meleeStrike(@NotNull final LivingEntity monster, @NotNull final World world) {
        final double r = Math.max(2.5, range);
        world.spawnParticle(Particle.CRIT, monster.getLocation().add(0, 1, 0), 15, r / 3, 0.4, r / 3, 0.05);
        world.playSound(monster.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.9f);
        for (final Player p : monster.getWorld().getPlayers()) {
            if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;
            if (p.getLocation().distanceSquared(monster.getLocation()) > r * r) continue;
            p.damage(damage, monster);
            final Vector kb = p.getLocation().toVector().subtract(monster.getLocation().toVector());
            if (kb.lengthSquared() > 1.0E-4) p.setVelocity(kb.normalize().multiply(0.4).setY(0.25));
        }
    }

    private void support(@NotNull final LivingEntity monster, @NotNull final World world) {
        world.spawnParticle(Particle.ANGRY_VILLAGER, monster.getLocation().add(0, 1.5, 0), 6, 0.3, 0.3, 0.3, 0.0);
        world.playSound(monster.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 0.8f, 0.8f);
        monster.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 0, false, false));
    }

    @Nullable
    private Player nearest(@NotNull final LivingEntity monster, final double maxRange) {
        Player best = null;
        double bestDist = maxRange * maxRange;
        for (final Player p : monster.getWorld().getPlayers()) {
            if (p.getGameMode() == GameMode.SPECTATOR) continue;
            final double d = p.getLocation().distanceSquared(monster.getLocation());
            if (d <= bestDist) { bestDist = d; best = p; }
        }
        return best;
    }
}
