package com.ultimatedungeon.monster.ai;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Drives monster behaviour each AI tick by running an {@link AIBehaviorTree}
 * over every live monster in an instance, giving allies to each so they can
 * coordinate.
 */
public final class MonsterAI {

    private final AIBehaviorTree behaviourTree = new AIBehaviorTree(20.0, false);

    /** Ticks AI for a group of monsters that share an instance. */
    public void tick(@NotNull final Collection<LivingEntity> monsters) {
        if (monsters.isEmpty()) return;
        final List<LivingEntity> live = new ArrayList<>();
        for (final LivingEntity m : monsters) {
            if (m != null && !m.isDead() && m.isValid()) live.add(m);
        }
        final Set<UUID> alive = new HashSet<>(live.size());
        for (final LivingEntity monster : live) {
            alive.add(monster.getUniqueId());
            behaviourTree.tick(monster, live);
        }
        behaviourTree.retainChaseState(alive);
    }
}
