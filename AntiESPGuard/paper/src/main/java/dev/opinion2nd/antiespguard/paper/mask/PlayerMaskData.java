package dev.opinion2nd.antiespguard.paper.mask;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player masking state. Written on the owning region/main thread (move and
 * world events) and read from the async packet listeners — every field is
 * therefore volatile or a concurrent collection.
 */
public final class PlayerMaskData {

    /** True while the player's own Y is below revealBelowYWhenUnder. */
    public volatile boolean underground = false;

    /** True when this player should never be masked (staff/bypass). */
    public volatile boolean bypass = false;

    /** True while the player's current world is an enabled masking world. */
    public volatile boolean worldActive = false;

    /** Chunks (packed key) currently revealed to this underground player. */
    public final Set<Long> revealedChunks = ConcurrentHashMap.newKeySet();

    /** Last position where we recomputed the reveal set (rescan throttle). */
    public volatile double lastScanX = Double.NaN;
    public volatile double lastScanZ = Double.NaN;

    public static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX & 0xFFFFFFFFL) | (((long) chunkZ & 0xFFFFFFFFL) << 32);
    }
}
