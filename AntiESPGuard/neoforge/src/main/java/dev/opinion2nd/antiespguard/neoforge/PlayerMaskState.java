package dev.opinion2nd.antiespguard.neoforge;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Per-player masking state (mirror of the Paper/Fabric modules). */
public final class PlayerMaskState {

    public volatile boolean underground = false;
    public volatile boolean bypass = false;
    public volatile boolean worldActive = false;
    public volatile boolean modFlagged = false;
    public final Set<Long> revealedChunks = ConcurrentHashMap.newKeySet();
    public volatile double lastScanX = Double.NaN;
    public volatile double lastScanZ = Double.NaN;

    public static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX & 0xFFFFFFFFL) | (((long) chunkZ & 0xFFFFFFFFL) << 32);
    }
}
