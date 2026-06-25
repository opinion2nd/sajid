package dev.opinion2nd.antifreecam.mask;

import dev.opinion2nd.antifreecam.AfConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Keeps each player's {@link PlayerMaskData} in sync on the main thread.
 *
 * <p>The occlusion masking is the same for every position, so this tracker only
 * needs to know two things: whether the player bypasses masking, and whether
 * their current world is an enabled masking world. No movement tracking, no
 * reveal radius, no chunk re-sends.
 */
public final class PlayerTracker implements Listener {

    private final MaskService service;

    public PlayerTracker(MaskService service) {
        this.service = service;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        update(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        service.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        update(event.getPlayer());
    }

    /** Recompute bypass + world-active flags for the player. */
    public void update(Player player) {
        AfConfig cfg = service.config();
        PlayerMaskData data = service.getOrCreate(player);
        data.bypass = player.hasPermission("antifreecam.bypass");
        data.worldActive = cfg.isWorldActive(player.getWorld());
    }
}
