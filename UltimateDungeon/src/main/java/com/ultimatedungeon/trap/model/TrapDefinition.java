package com.ultimatedungeon.trap.model;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Immutable trap definition parsed from a {@code traps.yml} section.
 *
 * <p>Captures damage, cooldown, trigger type/radius, activation delay,
 * knockback and any status effects so trap behaviour is fully configurable.</p>
 */
public final class TrapDefinition {

    /** A configurable status effect applied to victims. */
    public record StatusEffectSpec(@NotNull PotionEffectType type, int durationTicks, int amplifier) {}

    private final String id;
    private final String displayName;
    private final TrapTriggerType triggerType;
    private final double baseDamage;
    private final long cooldownTicks;
    private final double triggerRadius;
    private final long activationDelayTicks;
    private final double knockback;
    private final List<StatusEffectSpec> statusEffects;
    // ── TNT-family tuning (ignored by non-TNT traps) ──────────────────────────
    private final int fuseTicks;
    private final int tntCount;
    private final double tntSpread;
    private final double tntHeight;
    private final int chainFuseStep;
    private final double tntPower;
    private final String baitBlock;

    private TrapDefinition(final String id, final String displayName, final TrapTriggerType triggerType,
                           final double baseDamage, final long cooldownTicks, final double triggerRadius,
                           final long activationDelayTicks, final double knockback,
                           final List<StatusEffectSpec> statusEffects,
                           final int fuseTicks, final int tntCount, final double tntSpread,
                           final double tntHeight, final int chainFuseStep, final double tntPower,
                           final String baitBlock) {
        this.id = id;
        this.displayName = displayName;
        this.triggerType = triggerType;
        this.baseDamage = baseDamage;
        this.cooldownTicks = cooldownTicks;
        this.triggerRadius = triggerRadius;
        this.activationDelayTicks = activationDelayTicks;
        this.knockback = knockback;
        this.statusEffects = statusEffects;
        this.fuseTicks = fuseTicks;
        this.tntCount = tntCount;
        this.tntSpread = tntSpread;
        this.tntHeight = tntHeight;
        this.chainFuseStep = chainFuseStep;
        this.tntPower = tntPower;
        this.baitBlock = baitBlock;
    }

    @NotNull
    public static TrapDefinition fromSection(@NotNull final String id, @NotNull final ConfigurationSection s) {
        TrapTriggerType type;
        try {
            type = TrapTriggerType.valueOf(s.getString("trigger-type", "PLAYER_MOVEMENT").toUpperCase());
        } catch (final IllegalArgumentException ex) {
            type = TrapTriggerType.PLAYER_MOVEMENT;
        }

        final List<StatusEffectSpec> effects = new ArrayList<>();
        for (final Map<?, ?> m : s.getMapList("status-effects")) {
            final Object eff = m.get("effect");
            if (eff == null) continue;
            final PotionEffectType pet = matchEffect(eff.toString());
            if (pet == null) continue;
            effects.add(new StatusEffectSpec(pet,
                    (int) toLong(m.get("duration-ticks"), 100L),
                    (int) toLong(m.get("amplifier"), 0L)));
        }

        return new TrapDefinition(id,
                s.getString("display-name", id), type,
                s.getDouble("base-damage", 4.0),
                s.getLong("cooldown-ticks", 60L),
                s.getDouble("trigger-radius", 1.0),
                s.getLong("activation-delay-ticks", 0L),
                s.getDouble("knockback", 0.0),
                effects,
                s.getInt("fuse-ticks", 30),
                Math.max(1, s.getInt("tnt-count", 1)),
                s.getDouble("tnt-spread", 0.0),
                s.getDouble("tnt-height", 0.0),
                Math.max(0, s.getInt("chain-fuse-step", 0)),
                s.getDouble("tnt-power", 2.0),
                s.getString("bait-block", null));
    }

    @SuppressWarnings("deprecation")
    private static PotionEffectType matchEffect(final String raw) {
        return PotionEffectType.getByName(raw.toUpperCase());
    }

    private static long toLong(final Object o, final long def) {
        return o instanceof Number n ? n.longValue() : def;
    }

    @NotNull public String getId() { return id; }
    @NotNull public String getDisplayName() { return displayName; }
    @NotNull public TrapTriggerType getTriggerType() { return triggerType; }
    public double getBaseDamage() { return baseDamage; }
    public long getCooldownTicks() { return cooldownTicks; }
    public double getTriggerRadius() { return triggerRadius; }
    public long getActivationDelayTicks() { return activationDelayTicks; }
    public double getKnockback() { return knockback; }
    @NotNull public List<StatusEffectSpec> getStatusEffects() { return statusEffects; }
    public int getFuseTicks() { return fuseTicks; }
    public int getTntCount() { return tntCount; }
    public double getTntSpread() { return tntSpread; }
    public double getTntHeight() { return tntHeight; }
    public int getChainFuseStep() { return chainFuseStep; }
    public double getTntPower() { return tntPower; }
    @org.jetbrains.annotations.Nullable public String getBaitBlock() { return baitBlock; }
}
