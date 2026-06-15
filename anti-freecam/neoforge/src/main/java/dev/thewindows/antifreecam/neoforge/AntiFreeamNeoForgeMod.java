package dev.thewindows.antifreecam.neoforge;

import dev.thewindows.antifreecam.common.detection.DetectionConfig;
import dev.thewindows.antifreecam.common.detection.FreecamDetector;
import dev.thewindows.antifreecam.neoforge.detection.NeoForgeDetectionManager;
import dev.thewindows.antifreecam.neoforge.effect.NeoForgeVoidChunkInjector;
import dev.thewindows.antifreecam.neoforge.event.PacketEventHandler;
import dev.thewindows.antifreecam.neoforge.license.NeoForgeLicenseBootstrap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod("antifreecam")
public class AntiFreeamNeoForgeMod {

    private static AntiFreeamNeoForgeMod instance;

    private FreecamDetector detector;
    private PacketEventHandler packetHandler;

    public static AntiFreeamNeoForgeMod getInstance() {
        return instance;
    }

    public AntiFreeamNeoForgeMod(IEventBus modEventBus) {
        instance = this;

        // Validate license at mod construction time
        try {
            new NeoForgeLicenseBootstrap().validate();
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("AntiFreeam license validation failed.", e);
        }

        DetectionConfig config = new DetectionConfig();
        detector = new FreecamDetector(config);
        NeoForgeVoidChunkInjector injector = new NeoForgeVoidChunkInjector(config);

        packetHandler = new PacketEventHandler(detector);
        NeoForgeDetectionManager detectionManager = new NeoForgeDetectionManager(detector, injector, config);

        NeoForge.EVENT_BUS.register(packetHandler);
        NeoForge.EVENT_BUS.register(detectionManager);

        System.out.println("[AntiFreeam] NeoForge mod initialized.");
    }

    /** Called from the movement mixin to record packet data. */
    public void recordMovement(java.util.UUID player, double x, double y, double z,
                                float yaw, float pitch, boolean onGround) {
        packetHandler.recordMovement(player, x, y, z, yaw, pitch, onGround);
    }
}
