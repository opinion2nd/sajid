package dev.thewindows.antifreecam.neoforge.detection;

import dev.thewindows.antifreecam.common.detection.DetectionConfig;
import dev.thewindows.antifreecam.common.detection.DetectionResult;
import dev.thewindows.antifreecam.common.detection.FreecamDetector;
import dev.thewindows.antifreecam.neoforge.effect.NeoForgeVoidChunkInjector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class NeoForgeDetectionManager {

    private final FreecamDetector detector;
    private final NeoForgeVoidChunkInjector injector;
    private final DetectionConfig config;
    private int evalTick = 0;
    private int recheckTick = 0;

    public NeoForgeDetectionManager(FreecamDetector detector, NeoForgeVoidChunkInjector injector,
                                     DetectionConfig config) {
        this.detector = detector;
        this.injector = injector;
        this.config = config;
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        evalTick++;
        recheckTick++;

        if (evalTick >= config.getEvaluationIntervalTicks()) {
            evalTick = 0;
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                evaluatePlayer(player);
            }
        }

        if (recheckTick >= config.getVoidRecheckIntervalTicks()) {
            recheckTick = 0;
            for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                injector.recheckActive(player);
            }
        }
    }

    private void evaluatePlayer(ServerPlayer player) {
        DetectionResult result = detector.evaluate(player.getUUID());

        if (result.detected()) {
            if (!injector.hasVoidEffect(player.getUUID())) {
                injector.applyVoidEffect(player);
                System.out.println("[AntiFreeam] Flagged " + player.getName().getString() +
                    " as freecam (confidence=" + String.format("%.2f", result.confidence()) + ")");

                if (result.confidence() >= config.getAdminNotifyConfidenceThreshold()) {
                    notifyAdmins(player);
                }
            }
        } else if (injector.hasVoidEffect(player.getUUID())) {
            injector.removeVoidEffect(player);
        }
    }

    private void notifyAdmins(ServerPlayer flagged) {
        String msg = "[AntiFreeam] " + flagged.getName().getString() + " may be using freecam!";
        flagged.getServer().getPlayerList().getPlayers().stream()
            .filter(p -> p.hasPermissions(2))
            .forEach(admin -> admin.sendSystemMessage(Component.literal(msg)));
    }
}
