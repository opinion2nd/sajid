package com.ultimatedungeon.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Thread-safe random number utilities backed by {@link ThreadLocalRandom}.
 *
 * <p>{@link ThreadLocalRandom} is preferred over {@link java.util.Random} because
 * it avoids contention on the shared seed when async generation tasks run
 * concurrently on multiple threads.</p>
 */
public final class RandomUtil {

    private RandomUtil() {}

    // ── Numeric ───────────────────────────────────────────────────────────────

    /**
     * Returns a random {@code int} in {@code [min, max]} inclusive.
     *
     * <p>Never throws for any input: a reversed range is swapped and the
     * internal bound is computed with long arithmetic, so
     * {@code randomInt(Integer.MIN_VALUE, Integer.MAX_VALUE)} can never trigger
     * "bound must be greater than origin".</p>
     *
     * @param min inclusive lower bound
     * @param max inclusive upper bound
     * @return random int in range
     */
    public static int randomInt(final int min, final int max) {
        final int lo = Math.min(min, max);
        final int hi = Math.max(min, max);
        if (lo == hi) return lo;
        // long arithmetic: hi + 1 must not overflow int
        return (int) ThreadLocalRandom.current().nextLong(lo, (long) hi + 1L);
    }

    /**
     * Safe range roll for values that come from configuration: swaps reversed
     * bounds and clamps both ends into {@code [floor, ceiling]} before rolling.
     *
     * @return a random int inside the sanitised range
     */
    public static int safeRange(final int min, final int max, final int floor, final int ceiling) {
        final int lo = Math.max(floor, Math.min(Math.min(min, max), ceiling));
        final int hi = Math.max(floor, Math.min(Math.max(min, max), ceiling));
        return randomInt(lo, hi);
    }

    /** Returns a random {@code long} across the full range — used for layout seeds. */
    public static long randomSeed() {
        return ThreadLocalRandom.current().nextLong();
    }

    /**
     * Returns a random {@code double} in {@code [min, max)}.
     *
     * @param min inclusive lower bound
     * @param max exclusive upper bound
     * @return random double
     */
    public static double randomDouble(final double min, final double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    /**
     * Returns a random {@code double} in {@code [0.0, 1.0)}.
     * Convenient for probability checks.
     *
     * @return random double in [0, 1)
     */
    public static double random() {
        return ThreadLocalRandom.current().nextDouble();
    }

    // ── Probability ───────────────────────────────────────────────────────────

    /**
     * Returns {@code true} with probability {@code chance}.
     *
     * <p>Example: {@code rollChance(0.25)} returns {@code true} 25% of the time.</p>
     *
     * @param chance probability in {@code [0.0, 1.0]}
     * @return {@code true} if the roll succeeds
     */
    public static boolean rollChance(final double chance) {
        if (chance <= 0.0) return false;
        if (chance >= 1.0) return true;
        return ThreadLocalRandom.current().nextDouble() < chance;
    }

    /**
     * Returns {@code true} with probability {@code percent / 100.0}.
     *
     * @param percent percentage in {@code [0, 100]}
     * @return {@code true} if the roll succeeds
     */
    public static boolean rollPercent(final double percent) {
        return rollChance(percent / 100.0);
    }

    // ── Collection helpers ────────────────────────────────────────────────────

    /**
     * Picks a uniformly random element from a non-empty list.
     *
     * @param list the source list
     * @param <T>  element type
     * @return a random element
     * @throws IllegalArgumentException if the list is empty
     */
    @NotNull
    public static <T> T randomElement(@NotNull final List<T> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Cannot pick a random element from an empty list.");
        }
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    /**
     * Picks a uniformly random element from a non-empty collection.
     *
     * @param collection the source collection
     * @param <T>        element type
     * @return a random element
     */
    @NotNull
    public static <T> T randomElement(@NotNull final Collection<T> collection) {
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("Cannot pick a random element from an empty collection.");
        }
        final int index = ThreadLocalRandom.current().nextInt(collection.size());
        int i = 0;
        for (final T element : collection) {
            if (i++ == index) return element;
        }
        // Unreachable, but satisfies the compiler.
        throw new AssertionError("Unreachable.");
    }

    // ── Angle / direction ─────────────────────────────────────────────────────

    /**
     * Returns a random yaw angle in {@code [0, 360)}.
     *
     * @return random yaw in degrees
     */
    public static float randomYaw() {
        return (float) ThreadLocalRandom.current().nextDouble(0.0, 360.0);
    }

    /**
     * Randomly shuffles a list in place using Fisher-Yates.
     *
     * @param list the list to shuffle
     * @param <T>  element type
     */
    public static <T> void shuffle(@NotNull final List<T> list) {
        final java.util.Random rng = ThreadLocalRandom.current();
        for (int i = list.size() - 1; i > 0; i--) {
            final int j = rng.nextInt(i + 1);
            final T   t = list.get(j);
            list.set(j, list.get(i));
            list.set(i, t);
        }
    }
}
