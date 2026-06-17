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
     * <p>Masking is tied to the player's OWN body position. Only players who are
     * on the surface get the area below the threshold hidden (so a freecam camera
     * flown down from the surface — the common case — sees only void). The moment
     * the player's body is underground, nothing is masked for them, so normal
     * underground play is completely vanilla with no void anywhere.
     */
    public boolean shouldMaskChunk(UUID uuid, int cx, int cz) {
        PlayerMaskData data = players.get(uuid);
        return data != null && !data.bypass && data.worldActive && !data.underground;
    }
}
