package com.ultimatedungeon.trap.traps;

import com.ultimatedungeon.trap.model.TrapDefinition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.TNTPrimed;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A configurable TNT trap that detonates <em>real</em> primed TNT.
 *
 * <p>Because the dungeon's protection listener strips block damage from every
 * explosion in a dungeon world, this TNT hurts and flings players without ever
 * cratering the arena. Delegating to genuine {@link TNTPrimed} entities means the
 * fuse, gravity (falling TNT) and sequential detonation (chain TNT) all come from
 * vanilla physics — no scheduler required.</p>
 *
 * <p>A single implementation covers every flavour from {@code traps.yml}; the
 * behaviour (pressure plate, tripwire, delayed, falling, chain, fake-chest bait,
 * corridor ambush) is entirely driven by the definition's tuning fields:</p>
 * <ul>
 *   <li>{@code fuse-ticks} — delay before the first blast</li>
 *   <li>{@code tnt-count} — how many charges spawn</li>
 *   <li>{@code tnt-spread} — horizontal scatter radius of the charges</li>
 *   <li>{@code tnt-height} — spawn height above the plate (falling TNT)</li>
 *   <li>{@code chain-fuse-step} — extra fuse ticks per charge (ripple detonation)</li>
 *   <li>{@code tnt-power} — blast yield, scaled by difficulty</li>
 *   <li>{@code bait-block} — optional lure block placed when armed (fake chest)</li>
 * </ul>
 */
public final class TntTrap extends AbstractTrap {

    private static final int MIN_FUSE = 5;
    private static final float MIN_POWER = 1.0f;
    private static final float MAX_POWER = 4.0f;

    public TntTrap(@NotNull final TrapDefinition definition) { super(definition); }

    @Override
    public void place(@NotNull final Location location) {
        super.place(location);
        // Fake-chest / lure variants drop a bait block so the ambush reads as loot.
        final String bait = definition.getBaitBlock();
        if (bait != null && this.location != null && this.location.getWorld() != null) {
            final Material mat = Material.matchMaterial(bait);
            if (mat != null && mat.isBlock()) {
                this.location.getBlock().setType(mat, false);
            }
        }
    }

    @Override
    protected void onActivate() {
        if (location == null || location.getWorld() == null) return;

        // Telegraph: a hiss and smoke so an alert player gets a heartbeat to react.
        particle(Particle.SMOKE, 12);
        sound(Sound.ENTITY_TNT_PRIMED, 1.0f, 1.0f);

        final int count = Math.max(1, definition.getTntCount());
        final int baseFuse = Math.max(MIN_FUSE, definition.getFuseTicks());
        final int step = Math.max(0, definition.getChainFuseStep());
        final float power = scaledPower();
        final double spread = Math.max(0.0, definition.getTntSpread());
        final double height = Math.max(0.0, definition.getTntHeight());

        for (int i = 0; i < count; i++) {
            final double ox = spread > 0 ? ThreadLocalRandom.current().nextDouble(-spread, spread) : 0.0;
            final double oz = spread > 0 ? ThreadLocalRandom.current().nextDouble(-spread, spread) : 0.0;
            final Location spawn = location.clone().add(ox + 0.5, height + 0.5, oz + 0.5);
            final TNTPrimed tnt = location.getWorld().spawn(spawn, TNTPrimed.class);
            tnt.setFuseTicks(baseFuse + step * i);
            tnt.setYield(power);
            tnt.setIsIncendiary(false);
        }
    }

    /** Blast yield scaled by difficulty (relative to the trap's base damage), clamped. */
    private float scaledPower() {
        final double ref = definition.getBaseDamage();
        final double factor = ref > 0 ? scaledDamage / ref : 1.0;
        final double power = definition.getTntPower() * Math.max(0.5, factor);
        return (float) Math.max(MIN_POWER, Math.min(MAX_POWER, power));
    }
}
