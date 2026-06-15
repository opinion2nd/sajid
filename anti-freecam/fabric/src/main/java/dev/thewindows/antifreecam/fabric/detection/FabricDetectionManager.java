package dev.thewindows.antifreecam.fabric.detection;

import dev.thewindows.antifreecam.common.detection.DetectionConfig;
import dev.thewindows.antifreecam.common.detection.DetectionResult;
import dev.thewindows.antifreecam.common.detection.FreecamDetector;
import dev.thewindows.antifreecam.fabric.effect.FabricVoidChunkInjector;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class FabricDetectionManager {

    private final FreecamDetector detector;
    private final FabricVoidChunkInjector injector;
    private final DetectionConfig config;
    private int evalTick = 0;
    private int recheckTick = 0;

    public FabricDetectionManager(FreecamDetector detector, FabricVoidChunkInjector injector, DetectionConfig config) {
        this.detector = detector;
        this.injector = injector;
        this.config = config;
    }

    public void register() {
        ServerTickEvents.END_SERVER_TICK.register(this::onTick);
    }

    private void onTick(MinecraftServer server) {
        evalTick++;
        recheckTick++;

        if (evalTick >= config.getEvaluationIntervalTicks()) {
            evalTick = 0;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                evaluatePlayer(player);
            }
        }

        if (recheckTick >= config.getVoidRecheckIntervalTicks()) {
            recheckTick = 0;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                injector.recheckActive(player);
            }
        }
    }

    private void evaluatePlayer(ServerPlayerEntity player) {
        DetectionResult result = detector.evaluate(player.getUuid());

        if (result.detected()) {
            if (!injector.hasVoidEffect(player.getUuid())) {
                injector.applyVoidEffect(player);
                System.out.println("[AntiFreeam] Flagged " + player.getName().getString() +
                    " as freecam user (confidence=" + String.format("%.2f", result.confidence()) + ")");

                if (result.confidence() >= config.getAdminNotifyConfidenceThreshold()) {
                    notifyAdmins(player.getServer(), player, result);
                }
            }
        } else if (injector.hasVoidEffect(player.getUuid())) {
            injector.removeVoidEffect(player);
        }
    }

    private void notifyAdmins(MinecraftServer server, ServerPlayerEntity flagged, DetectionResult result) {
        String msg = "[AntiFreeam] " + flagged.getName().getString() +
            " may be using freecam (" + String.format("%.0f%%", result.confidence() * 100) + " confidence)";

        server.getPlayerManager().getPlayerList().stream()
            .filter(p -> p.hasPermissionLevel(2))
            .forEach(admin -> admin.sendMessage(Text.literal(msg)));
    }
}
