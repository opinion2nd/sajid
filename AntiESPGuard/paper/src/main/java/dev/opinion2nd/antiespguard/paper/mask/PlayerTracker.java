package dev.opinion2nd.antiespguard.paper.mask;

import dev.opinion2nd.antiespguard.paper.PaperConfig;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Keeps each player's {@link PlayerMaskData} in sync on the owning region/main
 * thread and re-sends chunks whenever a player's masking neighbourhood changes
 * (descending underground, surfacing, or moving while underground).
 */
public final class PlayerTracker implements Listener {

    private final MaskService service;
    private final ChunkResender resender;

    public PlayerTracker(MaskService service, ChunkResender resender) {
        this.service = service;
        this.resender = resender;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerMaskData data = service.getOrCreate(player);
        data.bypass = player.hasPermission("antiespguard.bypass");
        refresh(player, data, true);
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
        handleMovement(event.getPlayer(), to);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getTo() == null) {
            return;
        }
        // World / large jumps: force a full recompute.
        refresh(event.getPlayer(), service.getOrCreate(event.getPlayer()), true);
    }

    private void handleMovement(Player player, Location to) {
        PlayerMaskData data = service.getOrCreate(player);
        PaperConfig cfg = service.config();

        boolean nowUnder = to.getY() < cfg.raw().revealBelowYWhenUnder;
        boolean stateFlip = nowUnder != data.underground;

        // Vanilla-style: the reveal set only needs recomputing when the
        // player crosses into a new chunk, exactly like normal chunk loading.
        boolean changedChunk = (to.getBlockX() >> 4) != data.lastScanChunkX
                || (to.getBlockZ() >> 4) != data.lastScanChunkZ;

        if (!stateFlip && !changedChunk) {
            return;
        }
        refresh(player, data, stateFlip);
    }

    /**
     * Recompute world/under state and reveal set; re-send any chunk whose mask
     * decision changed so the client sees the update.
     */
    private void refresh(Player player, PlayerMaskData data, boolean forceResend) {
        PaperConfig cfg = service.config();
        Location loc = player.getLocation();

        data.worldActive = cfg.isWorldActive(player.getWorld());
        data.underground = loc.getY() < cfg.raw().revealBelowYWhenUnder;
        data.lastScanChunkX = loc.getBlockX() >> 4;
        data.lastScanChunkZ = loc.getBlockZ() >> 4;

        Set<Long> previous = new HashSet<>(data.revealedChunks);
        Set<Long> desired = new HashSet<>();

        if (data.worldActive && !data.bypass && data.underground) {
            int radius = player.getViewDistance();
            int pcx = loc.getBlockX() >> 4;
            int pcz = loc.getBlockZ() >> 4;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    desired.add(PlayerMaskData.chunkKey(pcx + dx, pcz + dz));
                }
            }
        }
        // Surface -> desired is empty, so everything that was revealed gets
        // re-sent and re-masked by the chunk listener (remaskOnReturn).

        if (desired.equals(previous) && !forceResend) {
            return;
        }

        data.revealedChunks.clear();
        data.revealedChunks.addAll(desired);

        if (resender.isBroken()) {
            return; // progressive reveal unavailable; surface masking still active
        }

        Set<Long> toResend = new HashSet<>(previous);
        toResend.addAll(desired);
        if (cfg.raw().remaskOnReturn || !desired.isEmpty()) {
            for (long key : toResend) {
                boolean inOld = previous.contains(key);
                boolean inNew = desired.contains(key);
                if (inOld != inNew) {
                    resender.resend(player, (int) (key & 0xFFFFFFFFL), (int) (key >> 32));
                }
            }
        }
    }
}
