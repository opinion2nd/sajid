package dev.opinion2nd.antiespguard.fabric;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player masking state, mirroring the Paper module. On a vanilla server all
 * of this is touched on the server thread, but the fields are kept concurrent
 * so the design matches the Paper implementation 1:1.
 */
public final class PlayerMaskState {

    public volatile boolean underground = false;
    public volatile boolean bypass = false;
    public volatile boolean worldActive = false;
    /** Set once this player has been reported for a cheat mod (dedupe). */
    public volatile boolean modFlagged = false;
    public final Set<Long> revealedChunks = ConcurrentHashMap.newKeySet();
    public volatile double lastScanX = Double.NaN;
    public volatile double lastScanZ = Double.NaN;

    public static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX & 0xFFFFFFFFL) | (((long) chunkZ & 0xFFFFFFFFL) << 32);
    }
}
