package com.ultimatedungeon.boss.abilities;

import com.ultimatedungeon.api.boss.IBossAbility;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for boss abilities: holds id, damage, range and cooldown, gates
 * activation behind the cooldown, and provides player-targeting helpers so
 * concrete abilities only implement {@link #perform}.
 */
public abstract class AbstractBossAbility implements IBossAbility {

    protected final String id;
    protected final double damage;
    protected final long cooldownTicks;
    protected final double range;
    private long nextReadyAt;

    protected AbstractBossAbility(@NotNull final String id, final double damage,
                                  final long cooldownTicks, final double range) {
        this.id = id;
        this.damage = damage;
        this.cooldownTicks = cooldownTicks;
        this.range = range;
    }

    @Override @NotNull public String getAbilityId() { return id; }

    @Override public boolean isReady() { return System.currentTimeMillis() >= nextReadyAt; }

    @Override
    public void activate(@NotNull final LivingEntity boss) {
        if (!isReady()) return;
        perform(boss);
        nextReadyAt = System.currentTimeMillis() + cooldownTicks * 50L;
    }

    @Override public void onCooldownEnd() { /* hook for subclasses */ }

    /** The concrete ability effect. */
    protected abstract void perform(@NotNull LivingEntity boss);

    // ── Helpers ─────────────────────────────────────────────────────────────

    @NotNull
    protected List<Player> nearbyPlayers(@NotNull final LivingEntity boss) {
        final List<Player> players = new ArrayList<>();
        if (boss.getWorld() == null) return players;
        final double r = Math.max(1.0, range);
        for (final var e : boss.getWorld().getNearbyEntities(boss.getLocation(), r, r, r)) {
            if (e instanceof final Player p && p.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
                players.add(p);
            }
        }
        return players;
    }

    @Nullable
    protected Player nearestPlayer(@NotNull final LivingEntity boss) {
        Player best = null;
        double bestDist = Double.MAX_VALUE;
        for (final Player p : nearbyPlayers(boss)) {
            final double d = p.getLocation().distanceSquared(boss.getLocation());
            if (d < bestDist) { bestDist = d; best = p; }
        }
        return best;
    }
}
