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
     * @return true if blocks below hideBelowY in chunk (cx,cz) must be masked
     *         to STONE for this player.
     */
    public boolean shouldMaskChunk(UUID uuid, int cx, int cz) {
        PlayerMaskData data = players.get(uuid);
        if (data == null || data.bypass || !data.worldActive) {
            return false;
        }
        // Surface player: hide everything below the threshold.
        if (!data.underground) {
            return true;
        }
        // Underground: reveal the player's neighbourhood, mask the rest so a
        // freecam camera that flies away from the body still hits stone.
        return !data.revealedChunks.contains(PlayerMaskData.chunkKey(cx, cz));
    }
}
