package dev.thewindows.antifreecam.paper;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import dev.thewindows.antifreecam.common.detection.DetectionConfig;
import dev.thewindows.antifreecam.common.detection.FreecamDetector;
import dev.thewindows.antifreecam.paper.command.AntiFreeamCommand;
import dev.thewindows.antifreecam.paper.detection.PaperDetectionManager;
import dev.thewindows.antifreecam.paper.effect.VoidChunkInjector;

import dev.thewindows.antifreecam.paper.listener.PacketListener;
import dev.thewindows.antifreecam.paper.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class AntiFreeamPlugin extends JavaPlugin {

    private PaperDetectionManager detectionManager;
    private FreecamDetector detector;
    private VoidChunkInjector injector;
    private DetectionConfig detectionConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Step 1: Validate license — DISABLED for testing build
        // licenseBootstrap = new PaperLicenseBootstrap(this);
        // try {
        //     licenseBootstrap.validate();
        // } catch (LicenseException e) {
        //     getLogger().severe("[AntiFreeam] LICENSE ERROR: " + e.getMessage());
        //     getLogger().severe("[AntiFreeam] Plugin will not enable.");
        //     getServer().getPluginManager().disablePlugin(this);
        //     return;
        // }
        getLogger().warning("[AntiFreeam] Running in TEST MODE — license check disabled.");

        // Step 2: Build detection config from config.yml
        FileConfiguration cfg = getConfig();
        detectionConfig = new DetectionConfig();
        detectionConfig.setBufferSize(cfg.getInt("detection.buffer-size", 40));
        detectionConfig.setFrozenPositionEpsilon(cfg.getDouble("detection.frozen-position-epsilon", 0.001));
        detectionConfig.setLookDeltaThresholdPerTick(cfg.getDouble("detection.look-delta-threshold-per-tick", 5.0));
        detectionConfig.setFlagConfidenceThreshold(cfg.getDouble("detection.confidence-threshold", 0.70));
        detectionConfig.setAdminNotifyConfidenceThreshold(cfg.getDouble("detection.admin-notify-threshold", 0.90));
        detectionConfig.setEvaluationIntervalTicks(cfg.getInt("detection.evaluation-interval-ticks", 10));
        detectionConfig.setRenderDistanceChunks(cfg.getInt("detection.render-distance-chunks", 8));
        detectionConfig.setPacketSilenceMs(cfg.getLong("detection.packet-silence-ms", 3000));
        detectionConfig.setTriggerY(cfg.getDouble("void-effect.trigger-y", 20.0));
        detectionConfig.setBlockRadius(cfg.getInt("void-effect.block-radius", 5));
        detectionConfig.setVoidRecheckIntervalTicks(cfg.getInt("void-effect.recheck-interval-ticks", 20));

        // Step 3: Init components
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        detector = new FreecamDetector(detectionConfig);
        injector = new VoidChunkInjector(getLogger(),
            detectionConfig.getBlockRadius(), detectionConfig.getTriggerY());

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

    public void reloadAntiFreeamConfig() {
        reloadConfig();
        FileConfiguration cfg = getConfig();
        detectionConfig.setBufferSize(cfg.getInt("detection.buffer-size", 40));
        detectionConfig.setFrozenPositionEpsilon(cfg.getDouble("detection.frozen-position-epsilon", 0.001));
        detectionConfig.setLookDeltaThresholdPerTick(cfg.getDouble("detection.look-delta-threshold-per-tick", 5.0));
        detectionConfig.setFlagConfidenceThreshold(cfg.getDouble("detection.confidence-threshold", 0.70));
        detectionConfig.setAdminNotifyConfidenceThreshold(cfg.getDouble("detection.admin-notify-threshold", 0.90));
        detectionConfig.setRenderDistanceChunks(cfg.getInt("detection.render-distance-chunks", 8));
        detectionConfig.setPacketSilenceMs(cfg.getLong("detection.packet-silence-ms", 3000));
        detectionConfig.setTriggerY(cfg.getDouble("void-effect.trigger-y", 20.0));
        detectionConfig.setBlockRadius(cfg.getInt("void-effect.block-radius", 5));
        injector.updateConfig(detectionConfig.getBlockRadius(), detectionConfig.getTriggerY());
    }

    @Override
    public void onDisable() {
        if (detectionManager != null) detectionManager.stop();
        getLogger().info("[AntiFreeam] Disabled.");
    }
}
