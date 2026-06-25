package dev.opinion2nd.antifreecam.mask;

import dev.opinion2nd.antifreecam.AfConfig;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central, thread-safe registry of per-player mask state plus the core
 * "should this player be masked at all?" decision used by the packet and block
 * listeners.
 *
 * <p>With the occlusion model there is no per-chunk reveal set any more: every
 * masked player gets exactly the same geometry-based masking, so the only thing
 * this service decides is whether masking is switched on for a given player.
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
     * @return true if outgoing block data for this player must be occlusion-masked
     *         (plugin enabled, player tracked, not bypassed, in an enabled world).
     */
    public boolean isActive(Player player) {
        AfConfig cfg = config;
        if (!cfg.enabled) {
            return false;
        }
        PlayerMaskData data = players.get(player.getUniqueId());
        return data != null && !data.bypass && data.worldActive;
    }
}
