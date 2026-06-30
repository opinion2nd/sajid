package com.ultimatedungeon.trap.traps;

import com.ultimatedungeon.api.trap.ITrap;
import com.ultimatedungeon.trap.model.TrapDefinition;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Shared base for all configurable traps.
 *
 * <p>Holds the trap's definition, placement location, armed state and scaled
 * damage, and provides reusable helpers for damaging nearby players, applying
 * knockback and status effects, and spawning particles/sounds — so each concrete
 * trap only has to describe its own flavour in {@link #onActivate}.</p>
 */
public abstract class AbstractTrap implements ITrap {

    protected final TrapDefinition definition;
    protected double scaledDamage;
    protected Location location;
    private boolean active;
    private long nextReadyAt;

    protected AbstractTrap(@NotNull final TrapDefinition definition) {
        this.definition = definition;
        this.scaledDamage = definition.getBaseDamage();
    }

    public void setScaledDamage(final double scaledDamage) {
        this.scaledDamage = scaledDamage;
    }

    @Override @NotNull public String getTrapId() { return definition.getId(); }

    @NotNull public TrapDefinition getDefinition() { return definition; }

    @Nullable public Location getLocation() { return location; }

    @Override
    public void place(@NotNull final Location location) {
        this.location = location.clone();
        this.active = true;
    }

    @Override public void reset() { this.active = false; }

    @Override public boolean isActive() { return active; }

    /** True if the trap is armed and off its internal cooldown. */
    public boolean canTrigger() {
        return active && location != null && System.currentTimeMillis() >= nextReadyAt;
    }

    @Override
    public void activate() {
        if (!canTrigger()) return;
        onActivate();
        nextReadyAt = System.currentTimeMillis() + definition.getCooldownTicks() * 50L;
    }

    /** Concrete trap effect. */
    protected abstract void onActivate();

    // ── Shared helpers ────────────────────────────────────────────────────────

    protected void damageNearby() {
        if (location == null || location.getWorld() == null) return;
        final double r = Math.max(1.0, definition.getTriggerRadius());
        for (final var entity : location.getWorld().getNearbyEntities(location, r, r, r)) {
            if (!(entity instanceof final Player player)) continue;
            if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR
                    || player.getGameMode() == org.bukkit.GameMode.CREATIVE) continue;
            if (scaledDamage > 0) player.damage(scaledDamage);
            applyKnockback(player);
            applyEffects(player);
        }
    }

    protected void applyKnockback(@NotNull final Player player) {
        if (definition.getKnockback() <= 0 || location == null) return;
        final Vector dir = player.getLocation().toVector().subtract(location.toVector());
        if (dir.lengthSquared() < 1.0E-4) return;
        player.setVelocity(dir.normalize().multiply(definition.getKnockback()).setY(0.4));
    }

    protected void applyEffects(@NotNull final Player player) {
        for (final TrapDefinition.StatusEffectSpec spec : definition.getStatusEffects()) {
            player.addPotionEffect(new PotionEffect(spec.type(), spec.durationTicks(), spec.amplifier()));
        }
    }

    protected void particle(@NotNull final Particle particle, final int count) {
        if (location != null && location.getWorld() != null) {
            location.getWorld().spawnParticle(particle, location.clone().add(0.5, 0.5, 0.5), count, 0.5, 0.5, 0.5, 0.0);
        }
    }

    protected void sound(@NotNull final Sound sound, final float volume, final float pitch) {
        if (location != null && location.getWorld() != null) {
            location.getWorld().playSound(location, sound, volume, pitch);
        }
    }
}
