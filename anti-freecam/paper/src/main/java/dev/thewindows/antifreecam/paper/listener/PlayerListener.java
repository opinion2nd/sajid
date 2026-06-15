package dev.thewindows.antifreecam.paper.listener;

import dev.thewindows.antifreecam.common.detection.FreecamDetector;
import dev.thewindows.antifreecam.paper.effect.VoidChunkInjector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

public class PlayerListener implements Listener {

    private final FreecamDetector detector;
    private final VoidChunkInjector injector;

    public PlayerListener(FreecamDetector detector, VoidChunkInjector injector) {
        this.detector = detector;
        this.injector = injector;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        detector.initPlayer(uuid);

        if (event.getPlayer().hasPermission("antifreecam.bypass")) {
            detector.whitelistPlayer(uuid);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        detector.removePlayer(uuid);
        injector.cleanup(uuid);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        // Reset buffer on teleport to avoid false positives from position jump
        detector.resetPlayer(event.getPlayer().getUniqueId());
        injector.removeVoidEffect(event.getPlayer());
    }
}
