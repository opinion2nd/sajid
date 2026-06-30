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
        }

        if (retreat.shouldRetreat(monster)) {
            retreat.retreat(monster, target);
            return;
        }
        if (rangedType) {
            ranged.maintainDistance(monster, target);
        } else {
            flanking.flank(monster, target, monster.getEntityId() % 2 == 0);
        }
    }
}
