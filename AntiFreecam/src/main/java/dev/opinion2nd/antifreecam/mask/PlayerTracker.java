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
 * <p>Masking depends only on the player's body position, so the only thing to
 * handle is the surface/underground transition: chunks already sent in the old
 * state are re-sent so the new decision reaches the client. Fresh chunks (e.g.
 * loaded right after a teleport to a far base) are handled automatically by the
 * packet listener.
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
        applyState(data, player.getLocation());
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

        boolean newUnder = to.getY() < cfg.revealBelowYWhenUnder;
        boolean newActive = cfg.isWorldActive(to.getWorld());

        // Only act when the player crosses the surface/underground line.
        if (newUnder == data.underground && newActive == data.worldActive) {
            return;
        }
        data.underground = newUnder;
        data.worldActive = newActive;
        resendArea(player, to.getBlockX() >> 4, to.getBlockZ() >> 4);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        Player player = event.getPlayer();
        PlayerMaskData data = service.getOrCreate(player);
        // Use the DESTINATION (event fires before the teleport) so a TP straight
        // into an underground base is treated as underground immediately.
        applyState(data, to);
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                Location loc = player.getLocation();
                resendArea(player, loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
            }
        });
    }

    private void applyState(PlayerMaskData data, Location loc) {
        AfConfig cfg = service.config();
        data.worldActive = cfg.isWorldActive(loc.getWorld());
        data.underground = loc.getY() < cfg.revealBelowYWhenUnder;
    }

    /** Re-send every loaded chunk in view range so a state change reaches the client. */
    private void resendArea(Player player, int pcx, int pcz) {
        if (resender.isBroken()) {
            return;
        }
        int r = resendRadius(player);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
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
