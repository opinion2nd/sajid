package dev.opinion2nd.antifreecam.mask;

import dev.opinion2nd.antifreecam.AntiFreecamPlugin;
import dev.opinion2nd.antifreecam.AfConfig;
import dev.opinion2nd.antifreecam.util.ChunkResender;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Keeps every player's surface/underground state in sync on the main thread.
 *
 * <p>Because masking is decided purely from the player's body position, the only
 * thing we must handle is the transition across the {@code revealBelowYWhenUnder}
 * line: chunks that were already sent in the old state need re-sending so the new
 * decision reaches the client. Fresh chunks (e.g. those loaded right after a
 * teleport to a far base) are handled automatically by the packet listener.
 */
public final class PlayerTracker implements Listener {

    private final AntiFreecamPlugin plugin;
    private final MaskService service;
    private final ChunkResender resender;

    public PlayerTracker(AntiFreecamPlugin plugin, MaskService service, ChunkResender resender) {
        this.plugin = plugin;
        this.service = service;
        this.resender = resender;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerMaskData data = service.getOrCreate(player);
        data.bypass = player.hasPermission("antifreecam.bypass");
        applyState(player, data, player.getLocation());
        // Chunks are sent fresh after join, so no re-send is needed here.
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        service.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        Player player = event.getPlayer();
        PlayerMaskData data = service.getOrCreate(player);

        AfConfig cfg = service.config();
        boolean nowUnder = to.getY() < cfg.revealBelowYWhenUnder;

        // Only act when the player actually crosses the surface/underground line.
        if (nowUnder == data.underground && data.worldActive == cfg.isWorldActive(to.getWorld())) {
            return;
        }
        applyState(player, data, to);
        resendAround(player, to);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        Player player = event.getPlayer();
        PlayerMaskData data = service.getOrCreate(player);
        // Use the DESTINATION (event fires before the teleport happens) so a TP
        // straight into an underground base is treated as underground at once.
        applyState(player, data, to);
        // Re-send a moment later, once the teleport has completed and the new
        // chunks exist, to catch any that were already loaded around the target.
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                resendAround(player, player.getLocation());
            }
        });
    }

    private void applyState(Player player, PlayerMaskData data, Location loc) {
        AfConfig cfg = service.config();
        data.worldActive = cfg.isWorldActive(loc.getWorld());
        data.underground = loc.getY() < cfg.revealBelowYWhenUnder;
    }

    /** Re-send the loaded chunks around the player so the new mask state applies. */
    private void resendAround(Player player, Location loc) {
        if (resender.isBroken()) {
            return; // masking still works on fresh chunks; only in-place refresh is lost
        }
        int radius = resendRadius(player);
        int pcx = loc.getBlockX() >> 4;
        int pcz = loc.getBlockZ() >> 4;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                resender.resend(player, pcx + dx, pcz + dz);
            }
        }
    }

    private int resendRadius(Player player) {
        int view;
        try {
            view = player.getViewDistance();
        } catch (Throwable t) {
            view = Bukkit.getViewDistance();
        }
        if (view < 4) {
            view = 8;
        }
        return Math.min(view, 12);
    }
}
