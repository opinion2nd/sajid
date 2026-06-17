package dev.opinion2nd.antifreecam.mask;

import dev.opinion2nd.antifreecam.AfConfig;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central, thread-safe registry of per-player mask state plus the core
 * "should this chunk be masked for this player?" decision used by the packet
 * listeners.
 */
public final class MaskService {

    private final Map<UUID, PlayerMaskData> players = new ConcurrentHashMap<>();
    private volatile AfConfig config;

    public MaskService(AfConfig config) {
        this.config = config;
    }

    public void setConfig(AfConfig config) {
        this.config = config;
    }

    public AfConfig config() {
        return config;
    }

    public PlayerMaskData get(UUID uuid) {
        return players.get(uuid);
    }

    public PlayerMaskData getOrCreate(Player player) {
        return players.computeIfAbsent(player.getUniqueId(), k -> new PlayerMaskData());
    }

    public void remove(UUID uuid) {
        players.remove(uuid);
    }

    /**
     * @return true if blocks below hideBelowY must be masked to STONE for this
     *         player.
     *
     * <p>Surface players have everything below the threshold hidden. Underground
     * players keep a small "bubble" of chunks around their body visible (so their
     * base/cave looks normal) while everything beyond the bubble stays stone — so
     * a freecam camera, whether driven from the surface or from inside a cave,
     * can never scout further than the player's real body could.
     */
    public boolean shouldMaskChunk(UUID uuid, int cx, int cz) {
        PlayerMaskData data = players.get(uuid);
        if (data == null || data.bypass || !data.worldActive) {
            return false;
        }
        if (!data.underground) {
            return true; // surface: hide everything below the threshold
        }
        // underground: mask only chunks outside the reveal bubble
        int r = config.undergroundRevealRadius;
        int dx = Math.abs(cx - data.centerChunkX);
        int dz = Math.abs(cz - data.centerChunkZ);
        return Math.max(dx, dz) > r;
    }
}
