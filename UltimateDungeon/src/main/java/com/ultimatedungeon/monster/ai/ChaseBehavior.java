package com.ultimatedungeon.monster.ai;

import org.bukkit.Location;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Actively drives a monster toward its target using the Paper Pathfinder API and
 * a per-mob stuck detector.
 *
 * <p>Relying on {@code Mob#setTarget} alone leaves mobs idling against walls and
 * corners when the vanilla navigator gives up. This behaviour re-issues a
 * navigation path on a short cadence (so mobs keep chasing across the whole room)
 * and, when a mob has clearly wedged itself, gives it a forward-and-up hop to
 * clear one-block lips, door frames and corners it would otherwise stick on.</p>
 *
 * <p>Per-mob state is keyed by entity UUID. One ChaseBehavior serves every
 * dungeon instance, so stale entries are expired by age (a periodic sweep of
 * entries untouched for {@code EXPIRE_MS}) rather than by comparing against any
 * single instance's alive list — which would wrongly wipe other instances'
 * state when several dungeons run concurrently.</p>
 */
public final class ChaseBehavior {

    /** Blocks of movement below which a mob is considered "not moving" this tick. */
    private static final double MOVED_EPSILON = 0.045;
    /** Consecutive stuck AI-ticks before we force an unstick hop (~1.5s at 0.5s/tick). */
    private static final int STUCK_LIMIT = 3;
    /** AI-ticks between fresh navigation paths (~2s at 0.5s/tick). */
    private static final int REPATH_INTERVAL = 4;
    /** Within this distance the mob is in melee range — hand back to vanilla. */
    private static final double MELEE_RANGE = 2.2;
    /** Navigation speed multiplier; >1 so dungeon mobs press the attack. */
    private static final double CHASE_SPEED = 1.3;
    /** State entries untouched this long belong to despawned mobs — drop them. */
    private static final long EXPIRE_MS = 60_000L;
    /** How often the expiry sweep runs. */
    private static final long SWEEP_INTERVAL_MS = 10_000L;

    private static final class State {
        double lastX, lastY, lastZ;
        int stuckTicks;
        int repathCooldown;
        boolean primed;
        long touchedAt;
    }

    private final Map<UUID, State> states = new ConcurrentHashMap<>();
    private volatile long nextSweepAt;

    /** Drives {@code mob} toward {@code target}, re-pathing and unsticking as needed. */
    public void chase(@NotNull final Mob mob, @NotNull final Player target) {
        sweepExpired();
        final State st = states.computeIfAbsent(mob.getUniqueId(), k -> new State());
        st.touchedAt = System.currentTimeMillis();
        final Location loc = mob.getLocation();

        final double distSq = loc.distanceSquared(target.getLocation());
        if (distSq <= MELEE_RANGE * MELEE_RANGE) {
            // Close enough to swing — let vanilla combat AI take over cleanly.
            st.stuckTicks = 0;
            st.repathCooldown = 0;
            rememberPosition(st, loc);
            return;
        }

        if (st.primed) {
            final double moved = Math.hypot(loc.getX() - st.lastX, loc.getZ() - st.lastZ);
            if (moved < MOVED_EPSILON) st.stuckTicks++;
            else st.stuckTicks = 0;
        }
        rememberPosition(st, loc);

        // Re-issue a navigation path on a short cadence so the mob keeps chasing
        // rather than losing interest at the edge of the vanilla follow range.
        if (st.repathCooldown-- <= 0) {
            st.repathCooldown = REPATH_INTERVAL;
            try {
                mob.getPathfinder().moveTo(target.getLocation(), CHASE_SPEED);
            } catch (final Throwable ignored) {
                // Pathfinder API unavailable — the unstick hop below still applies.
            }
        }

        // Genuinely wedged: hop toward the target to clear the obstacle.
        if (st.stuckTicks >= STUCK_LIMIT) {
            final Vector dir = target.getLocation().toVector().subtract(loc.toVector());
            dir.setY(0);
            if (dir.lengthSquared() > 1.0E-4) {
                dir.normalize().multiply(0.3);
                final double upward = Math.max(mob.getVelocity().getY(), 0.42);
                mob.setVelocity(new Vector(dir.getX(), upward, dir.getZ()));
            }
            st.stuckTicks = 0;
        }
    }

    /** Periodically drops state entries that haven't been touched in a while. */
    private void sweepExpired() {
        final long now = System.currentTimeMillis();
        if (now < nextSweepAt) return;
        nextSweepAt = now + SWEEP_INTERVAL_MS;
        states.values().removeIf(st -> now - st.touchedAt > EXPIRE_MS);
    }

    private void rememberPosition(@NotNull final State st, @NotNull final Location loc) {
        st.lastX = loc.getX();
        st.lastY = loc.getY();
        st.lastZ = loc.getZ();
        st.primed = true;
    }
}
