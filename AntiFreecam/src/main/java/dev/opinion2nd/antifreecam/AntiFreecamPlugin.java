package dev.opinion2nd.antifreecam;

import com.github.retrooper.packetevents.PacketEvents;
import dev.opinion2nd.antifreecam.command.AfCommand;
import dev.opinion2nd.antifreecam.detect.BrandDetectionListener;
import dev.opinion2nd.antifreecam.mask.ChunkMaskListener;
import dev.opinion2nd.antifreecam.mask.EntityMaskListener;
import dev.opinion2nd.antifreecam.mask.MaskService;
import dev.opinion2nd.antifreecam.mask.PlayerTracker;
import dev.opinion2nd.antifreecam.util.ChunkResender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * AntiFreecam — original anti-freecam / anti-ESP block masker for Paper 1.21.x.
 *
 * <p>Pipeline: a Bukkit {@link PlayerTracker} maintains each player's
 * surface/underground state on the main thread; the PacketEvents
 * {@link ChunkMaskListener} rewrites below-Y blocks to stone on the way out; the
 * {@link ChunkResender} re-sends chunks when a player crosses the boundary so
 * the reveal/re-mask is seamless.
 */
public final class AntiFreecamPlugin extends JavaPlugin {

    private MaskService maskService;
    private final java.util.List<Object> packetListeners = new java.util.ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.maskService = new MaskService(new AfConfig(getConfig()));

        ChunkResender resender = new ChunkResender(this);

        // PacketEvents is initialised by the PacketEvents plugin (a hard depend),
        // so the API is ready by the time our onEnable runs.
        registerPacketListener(new ChunkMaskListener(maskService));
        registerPacketListener(new EntityMaskListener(maskService));
        registerPacketListener(new BrandDetectionListener(this, maskService));

        getServer().getPluginManager().registerEvents(
                new PlayerTracker(this, maskService, resender), this);

        AfCommand cmd = new AfCommand(this, maskService);
        getCommand("antifreecam").setExecutor(cmd);
        getCommand("antifreecam").setTabCompleter(cmd);

        getLogger().info("AntiFreecam enabled (hideBelowY="
                + maskService.config().hideBelowY + ").");
    }

    @Override
    public void onDisable() {
        for (Object listener : packetListeners) {
            try {
                PacketEvents.getAPI().getEventManager()
                        .unregisterListener((com.github.retrooper.packetevents.event.PacketListenerCommon) listener);
            } catch (Throwable ignored) {
            }
        }
        packetListeners.clear();
    }

    private void registerPacketListener(com.github.retrooper.packetevents.event.PacketListenerAbstract listener) {
        packetListeners.add(PacketEvents.getAPI().getEventManager().registerListener(listener));
    }

    /** Re-read config.yml and swap the live config snapshot. */
    public void reloadConfiguration() {
        reloadConfig();
        maskService.setConfig(new AfConfig(getConfig()));
    }
}
