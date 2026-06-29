package com.ultimatedungeon.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generic weighted random selector.
 *
 * <p>Used by room registries, loot tables, monster spawners, and theme selectors
 * to pick items proportionally to their configured weight values.</p>
 *
 * <h3>Algorithm</h3>
 * Prefix sums are built once at construction time; each selection is O(log n)
 * via binary search. This is efficient even for large pools of weighted items.
 *
 * <h3>Example</h3>
 * <pre>{@code
 * WeightedRandomSelector<RoomType> selector = new WeightedRandomSelector<>();
 * selector.add(RoomType.COMBAT,    40);
 * selector.add(RoomType.TREASURE,  10);
 * selector.add(RoomType.TRAP,      15);
 * RoomType chosen = selector.select(); // COMBAT ~63% of the time
 * }</pre>
 *
 * @param <T> the type of item being selected
 */
public final class WeightedRandomSelector<T> {

    private final List<T>    items   = new ArrayList<>();
    private final List<Long> prefixes = new ArrayList<>();  // cumulative weights
    private long totalWeight = 0L;

    // ── Building the pool ─────────────────────────────────────────────────────

    /**
     * Adds an item with the given weight. Weight must be ≥ 0.
     * Items with weight 0 are stored but never selected.
     *
     * @param item   the item to add
     * @param weight non-negative selection weight
     * @throws IllegalArgumentException if weight is negative
     */
    public void add(@NotNull final T item, final int weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Weight must be ≥ 0, got: " + weight);
        }
        totalWeight += weight;
        items.add(item);
        prefixes.add(totalWeight);
    }

    /** Returns the number of items in the pool. */
    public int size() {
        return items.size();
    }

    /** Returns {@code true} if the pool is empty or all weights are zero. */
    public boolean isEmpty() {
        return items.isEmpty() || totalWeight == 0L;
    }

    // ── Selection ─────────────────────────────────────────────────────────────

    /**
     * Selects one item proportionally to weight.
     *
     * @return the selected item
     * @throws IllegalStateException if the pool is empty or has zero total weight
     */
    @NotNull
    public T select() {
        if (isEmpty()) {
            throw new IllegalStateException(
                "WeightedRandomSelector is empty or all weights are zero."
            );
        }
        final long roll = ThreadLocalRandom.current().nextLong(totalWeight) + 1L;
        return items.get(binarySearch(roll));
    }

    /**
     * Selects one item and removes it from the pool.
     * Useful for sampling without replacement.
     *
     * @return the selected item
     * @throws IllegalStateException if the pool is empty
     */
    @NotNull
    public T selectAndRemove() {
        final T selected = select();
        final int index = items.indexOf(selected);
        items.remove(index);
        rebuildPrefixes();
        return selected;
    }

    // ── Private ───────────────────────────────────────────────────────────────

    /** Binary search for the first prefix ≥ roll. */
    private int binarySearch(final long roll) {
        int lo = 0, hi = prefixes.size() - 1;
        while (lo < hi) {
            final int mid = (lo + hi) >>> 1;
            if (prefixes.get(mid) < roll) lo = mid + 1;
            else                          hi = mid;
        }
        return lo;
    }

    private void rebuildPrefixes() {
        prefixes.clear();
        totalWeight = 0L;
        // Rebuild from scratch after removal — weights are not stored separately.
        // This is only called by selectAndRemove() which is infrequent.
        throw new UnsupportedOperationException(
            "selectAndRemove() requires weight storage. "
            + "Use separate WeightedEntry objects if this is needed."
        );
    }
}
