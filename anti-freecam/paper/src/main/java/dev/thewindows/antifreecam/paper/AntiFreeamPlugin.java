package dev.thewindows.antifreecam.paper;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import dev.thewindows.antifreecam.common.detection.DetectionConfig;
import dev.thewindows.antifreecam.common.detection.FreecamDetector;
import dev.thewindows.antifreecam.paper.command.AntiFreeamCommand;
import dev.thewindows.antifreecam.paper.detection.PaperDetectionManager;
import dev.thewindows.antifreecam.paper.effect.VoidChunkInjector;
import dev.thewindows.antifreecam.paper.license.LicenseException;
import dev.thewindows.antifreecam.paper.license.PaperLicenseBootstrap;
import dev.thewindows.antifreecam.paper.listener.PacketListener;
import dev.thewindows.antifreecam.paper.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class AntiFreeamPlugin extends JavaPlugin {

    private PaperLicenseBootstrap licenseBootstrap;
    private PaperDetectionManager detectionManager;
    private FreecamDetector detector;
    private VoidChunkInjector injector;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Step 1: Validate license (aborts enable if invalid)
        licenseBootstrap = new PaperLicenseBootstrap(this);
        try {
            licenseBootstrap.validate();
        } catch (LicenseException e) {
            getLogger().severe("[AntiFreeam] LICENSE ERROR: " + e.getMessage());
            getLogger().severe("[AntiFreeam] Plugin will not enable.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Step 2: Build detection config from config.yml
        FileConfiguration cfg = getConfig();
        DetectionConfig detectionConfig = new DetectionConfig();
        detectionConfig.setBufferSize(cfg.getInt("detection.buffer-size", 40));
        detectionConfig.setFrozenPositionEpsilon(cfg.getDouble("detection.frozen-position-epsilon", 0.001));
        detectionConfig.setLookDeltaThresholdPerTick(cfg.getDouble("detection.look-delta-threshold-per-tick", 5.0));
        detectionConfig.setFlagConfidenceThreshold(cfg.getDouble("detection.confidence-threshold", 0.70));
        detectionConfig.setAdminNotifyConfidenceThreshold(cfg.getDouble("detection.admin-notify-threshold", 0.90));
        detectionConfig.setEvaluationIntervalTicks(cfg.getInt("detection.evaluation-interval-ticks", 10));
        detectionConfig.setRenderDistanceChunks(cfg.getInt("detection.render-distance-chunks", 8));
        detectionConfig.setTriggerY(cfg.getDouble("void-effect.trigger-y", 20.0));
        detectionConfig.setChunkRadius(cfg.getInt("void-effect.chunk-radius", 10));
        detectionConfig.setVoidRecheckIntervalTicks(cfg.getInt("void-effect.recheck-interval-ticks", 20));

        // Step 3: Init components
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        detector = new FreecamDetector(detectionConfig);
        injector = new VoidChunkInjector(protocolManager, getLogger(),
            detectionConfig.getChunkRadius());

        // Step 4: Register whitelisted players from config
        List<String> whitelistUUIDs = cfg.getStringList("whitelist.players");
        for (String uuidStr : whitelistUUIDs) {
            try {
                detector.whitelistPlayer(java.util.UUID.fromString(uuidStr));
            } catch (IllegalArgumentException ignored) {
                getLogger().warning("[AntiFreeam] Invalid whitelist UUID: " + uuidStr);
            }
        }

        // Step 5: Register listeners and packet listener
        boolean notifyAdmins = cfg.getBoolean("notifications.notify-admins", true);
        String notifyPerm = cfg.getString("notifications.notify-permission", "antifreecam.admin");

        getServer().getPluginManager().registerEvents(new PlayerListener(detector, injector), this);
        protocolManager.addPacketListener(new PacketListener(this, protocolManager, detector));

        // Init buffers for already-online players (reload scenario)
        Bukkit.getOnlinePlayers().forEach(p -> detector.initPlayer(p.getUniqueId()));

        // Step 6: Start detection manager
        detectionManager = new PaperDetectionManager(this, detector, injector, detectionConfig,
            notifyAdmins, notifyPerm);
        detectionManager.start();

        // Step 7: Register command
        AntiFreeamCommand cmd = new AntiFreeamCommand(this, detector, injector);
        var paperCmd = getCommand("antifreecam");
        if (paperCmd != null) {
            paperCmd.setExecutor(cmd);
            paperCmd.setTabCompleter(cmd);
        }

        getLogger().info("[AntiFreeam] Enabled successfully on " + Bukkit.getVersion());
    }

    @Override
    public void onDisable() {
        if (detectionManager != null) detectionManager.stop();
        if (licenseBootstrap != null) licenseBootstrap.shutdown();
        getLogger().info("[AntiFreeam] Disabled.");
    }
}
