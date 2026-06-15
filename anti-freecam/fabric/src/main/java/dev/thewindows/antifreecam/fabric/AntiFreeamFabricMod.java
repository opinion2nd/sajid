package dev.thewindows.antifreecam.fabric;

import dev.thewindows.antifreecam.common.detection.DetectionConfig;
import dev.thewindows.antifreecam.common.detection.FreecamDetector;
import dev.thewindows.antifreecam.fabric.detection.FabricDetectionManager;
import dev.thewindows.antifreecam.fabric.effect.FabricVoidChunkInjector;
import dev.thewindows.antifreecam.fabric.license.FabricLicenseBootstrap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class AntiFreeamFabricMod implements ModInitializer {

    private static AntiFreeamFabricMod instance;

    private FreecamDetector detector;
    private FabricVoidChunkInjector injector;
    private long tick = 0;

    public static AntiFreeamFabricMod getInstance() {
        return instance;
    }

    @Override
    public void onInitialize() {
        instance = this;

        // Validate license before anything else
        try {
            new FabricLicenseBootstrap().validate();
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("AntiFreeam license validation failed — server start aborted.", e);
        }

        DetectionConfig config = new DetectionConfig();
        detector = new FreecamDetector(config);
        injector = new FabricVoidChunkInjector(config);

        FabricDetectionManager detectionManager = new FabricDetectionManager(detector, injector, config);
        detectionManager.register();

        // Init player buffers on join
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            detector.initPlayer(player.getUuid());
        });

        // Cleanup on quit
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID uuid = handler.getPlayer().getUuid();
            detector.removePlayer(uuid);
            injector.cleanup(uuid);
        });

        System.out.println("[AntiFreeam] Fabric mod initialized.");
    }

    public void recordMovement(UUID playerUuid, double x, double y, double z,
                                float yaw, float pitch, boolean onGround) {
        detector.recordMovement(playerUuid, x, y, z, yaw, pitch, onGround, tick++);
    }
}
