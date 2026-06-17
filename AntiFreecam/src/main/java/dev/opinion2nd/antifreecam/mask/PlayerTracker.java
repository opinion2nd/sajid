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
 * Keeps every player's surface/underground state and reveal-bubble centre in
 * sync on the main thread, re-sending only the chunks whose mask decision
 * actually changes.
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

        boolean oldUnder = data.underground;
        boolean oldActive = data.worldActive;
        int oldCX = data.centerChunkX;
        int oldCZ = data.centerChunkZ;

        boolean newUnder = to.getY() < cfg.revealBelowYWhenUnder;
        boolean newActive = cfg.isWorldActive(to.getWorld());
        int newCX = to.getBlockX() >> 4;
        int newCZ = to.getBlockZ() >> 4;

        boolean stateChanged = newUnder != oldUnder || newActive != oldActive;
        boolean chunkChanged = newCX != oldCX || newCZ != oldCZ;
        if (!stateChanged && !chunkChanged) {
            return;
        }

        data.underground = newUnder;
        data.worldActive = newActive;
        data.centerChunkX = newCX;
        data.centerChunkZ = newCZ;

        if (resender.isBroken() || !newActive && !oldActive) {
            return;
        }

        if (stateChanged) {
            // surface<->underground (or world toggled): refresh the whole view area
            resendArea(player, newCX, newCZ);
        } else if (newUnder) {
            // still underground, just walked into a new chunk: shift the bubble,
            // re-sending only the chunks that entered or left it
            resendBubbleShift(player, oldCX, oldCZ, newCX, newCZ, cfg.undergroundRevealRadius);
        }
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
        data.centerChunkX = loc.getBlockX() >> 4;
        data.centerChunkZ = loc.getBlockZ() >> 4;
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

    /** Re-send only the chunks whose in/out-of-bubble status changed as it moved. */
    private void resendBubbleShift(Player player, int oldCX, int oldCZ, int newCX, int newCZ, int r) {
        int minX = Math.min(oldCX, newCX) - r;
        int maxX = Math.max(oldCX, newCX) + r;
        int minZ = Math.min(oldCZ, newCZ) - r;
        int maxZ = Math.max(oldCZ, newCZ) + r;
        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                boolean wasMasked = Math.max(Math.abs(cx - oldCX), Math.abs(cz - oldCZ)) > r;
                boolean nowMasked = Math.max(Math.abs(cx - newCX), Math.abs(cz - newCZ)) > r;
                if (wasMasked != nowMasked) {
                    resender.resend(player, cx, cz);
                }
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
