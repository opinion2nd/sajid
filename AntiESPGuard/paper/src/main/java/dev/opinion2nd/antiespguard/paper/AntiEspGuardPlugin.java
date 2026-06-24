package dev.opinion2nd.antiespguard.paper;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import dev.opinion2nd.antiespguard.common.AntiEspConfig;
import dev.opinion2nd.antiespguard.paper.command.AntiEspCommand;
import dev.opinion2nd.antiespguard.paper.detect.ModDetectionListener;
import dev.opinion2nd.antiespguard.paper.mask.ChunkMaskListener;
import dev.opinion2nd.antiespguard.paper.mask.ChunkResender;
import dev.opinion2nd.antiespguard.paper.mask.EntityMaskListener;
import dev.opinion2nd.antiespguard.paper.mask.MaskService;
import dev.opinion2nd.antiespguard.paper.mask.PlayerTracker;
import dev.opinion2nd.antiespguard.paper.seed.SeedCrackerListener;
import dev.opinion2nd.antiespguard.paper.update.UpdateChecker;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * AntiESPGuard (Paper) — multi-platform anti-freecam / anti-ESP, Bukkit edition.
 *
 * <p>Features (anti-xray intentionally omitted): below-Y chunk masking with
 * progressive reveal, entity/underground-player masking, anti-seed-cracker,
 * cheat-mod detection (brand + channel) with auto-kick / Discord, and an update
 * checker. Folia-compatible.</p>
 */
public final class AntiEspGuardPlugin extends JavaPlugin {

    private MaskService maskService;
    private ModDetectionListener modDetection;
    private final List<PacketListenerCommon> packetListeners = new ArrayList<>();

    @Override
    public void onEnable() {
        PaperConfig config = loadConfiguration();
        this.maskService = new MaskService(config);

        ChunkResender resender = new ChunkResender(this);

        // ---- PacketEvents listeners (run async; read immutable config) -------
        registerPacketListener(new ChunkMaskListener(maskService));
        registerPacketListener(new EntityMaskListener(maskService));
        registerPacketListener(new SeedCrackerListener());
        this.modDetection = new ModDetectionListener(this, maskService);
        registerPacketListener(modDetection);

        // ---- Bukkit listeners ------------------------------------------------
        getServer().getPluginManager().registerEvents(
                new PlayerTracker(maskService, resender), this);
        getServer().getPluginManager().registerEvents(modDetection, this);

        if (config.raw().updateChecker.enabled) {
            UpdateChecker updateChecker =
                    new UpdateChecker(this, config.raw().updateChecker.notifyOps);
            getServer().getPluginManager().registerEvents(updateChecker, this);
            updateChecker.start();
        }

        // ---- Command ---------------------------------------------------------
        AntiEspCommand cmd = new AntiEspCommand(this, maskService);
        if (getCommand("antiespguard") != null) {
            getCommand("antiespguard").setExecutor(cmd);
            getCommand("antiespguard").setTabCompleter(cmd);
        }

        getLogger().info("AntiESPGuard enabled (hideBelowY=" + config.raw().hideBelowY
                + ", folia=" + dev.opinion2nd.antiespguard.paper.util.Schedulers.isFolia() + ").");
    }

    @Override
    public void onDisable() {
        for (PacketListenerCommon listener : packetListeners) {
            try {
                PacketEvents.getAPI().getEventManager().unregisterListener(listener);
            } catch (Throwable ignored) {
            }
        }
        packetListeners.clear();
        HandlerList.unregisterAll(this);
    }

    /** Re-read config.yml and swap the live config snapshot. */
    public void reloadConfiguration() {
        maskService.setConfig(loadConfiguration());
    }

    // ------------------------------------------------------------------------

    private void registerPacketListener(PacketListenerAbstract listener) {
        packetListeners.add(PacketEvents.getAPI().getEventManager().registerListener(listener));
    }

    /**
     * Load config.yml from the data folder (creating it from the bundled default
     * on first run) and parse it with the shared {@link AntiEspConfig} loader.
     */
    private PaperConfig loadConfiguration() {
        Path file = getDataFolder().toPath().resolve("config.yml");
        try {
            if (!Files.exists(file)) {
                Files.createDirectories(getDataFolder().toPath());
                try (InputStream in = getResource("antiespguard/config.yml")) {
                    if (in != null) {
                        Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Could not write default config.yml; using built-in defaults.", e);
        }

        AntiEspConfig cfg;
        try (InputStream in = Files.exists(file)
                ? Files.newInputStream(file)
                : getResource("antiespguard/config.yml")) {
            cfg = AntiEspConfig.load(in);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Failed to read config.yml; using built-in defaults.", e);
            cfg = new AntiEspConfig();
        }

        for (String warning : cfg.validateAndClamp()) {
            getLogger().warning("[config] " + warning);
        }
        return new PaperConfig(cfg);
    }
}
