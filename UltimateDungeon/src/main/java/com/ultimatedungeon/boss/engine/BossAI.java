package com.ultimatedungeon.boss.engine;

import com.ultimatedungeon.api.boss.IBossAbility;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Drives a boss each tick: keeps it targeting the nearest player and fires the
 * first ready ability in its rotation, so abilities cycle as cooldowns allow.
 */
public final class BossAI {

    private final List<IBossAbility> abilities;
    private int rotation;

    public BossAI(@NotNull final List<IBossAbility> abilities) {
        this.abilities = abilities;
    }

    public void tick(@NotNull final LivingEntity boss) {
        if (boss.isDead()) return;
        retarget(boss);
        if (abilities.isEmpty()) return;
        // Rotate the starting point so abilities feel varied, then fire the first ready one.
        for (int i = 0; i < abilities.size(); i++) {
            final IBossAbility ability = abilities.get((rotation + i) % abilities.size());
            if (ability.isReady()) {
                ability.activate(boss);
                rotation = (rotation + i + 1) % abilities.size();
                break;
            }
        }
    }

    private void retarget(@NotNull final LivingEntity boss) {
        if (!(boss instanceof final Mob mob) || boss.getWorld() == null) return;
        Player nearest = null;
        double best = Double.MAX_VALUE;
        for (final Player p : boss.getWorld().getPlayers()) {
            if (p.getGameMode() == org.bukkit.GameMode.SPECTATOR) continue;
            final double d = p.getLocation().distanceSquared(boss.getLocation());
            if (d < best) { best = d; nearest = p; }
        }
        if (nearest != null) mob.setTarget(nearest);
    }
}
