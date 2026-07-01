package com.ultimatedungeon.monster.ai;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Lightweight behaviour tree that decides what a single monster does each AI tick.
 *
 * <p>Priority order: retreat when badly wounded → coordinate the pack on a new
 * target → flank while closing in. Ranged monsters additionally kite to keep
 * their preferred distance.</p>
 */
public final class AIBehaviorTree {

    private final TargetSelectorBehavior targeting = new TargetSelectorBehavior();
    private final RetreatBehavior retreat = new RetreatBehavior();
    private final FlankingBehavior flanking = new FlankingBehavior();
    private final GroupCoordinationBehavior coordination = new GroupCoordinationBehavior(8.0);
    private final RangedEngagementBehavior ranged = new RangedEngagementBehavior(8.0);
    private final ChaseBehavior chase = new ChaseBehavior();

    /** Beyond this range flanking nudges are suppressed so they don't fight navigation. */
    private static final double FLANK_RANGE = 5.0;

    private final double engageRadius;
    private final boolean rangedType;

    public AIBehaviorTree(final double engageRadius, final boolean rangedType) {
        this.engageRadius = engageRadius;
        this.rangedType = rangedType;
    }

    public void tick(@NotNull final LivingEntity monster, @NotNull final Collection<LivingEntity> allies) {
        final Player target = targeting.selectTarget(monster, engageRadius);
        if (target == null) return;

        if (monster instanceof final Mob mob) {
            mob.setTarget(target);
            coordination.shareTarget(monster, target, allies);
            if (!rangedType) chase.chase(mob, target);
        }

        if (retreat.shouldRetreat(monster)) {
            retreat.retreat(monster, target);
            return;
        }
        if (rangedType) {
            ranged.maintainDistance(monster, target);
        } else if (monster.getLocation().distanceSquared(target.getLocation()) <= FLANK_RANGE * FLANK_RANGE) {
            // Only spread out once in close quarters, so the sideways nudge never
            // pushes a chasing mob off its navigation path into a wall.
            flanking.flank(monster, target, monster.getEntityId() % 2 == 0);
        }
    }

    /** Prunes chase state for mobs that have despawned. */
    public void retainChaseState(@NotNull final java.util.Set<java.util.UUID> alive) {
        chase.retain(alive);
    }
}
