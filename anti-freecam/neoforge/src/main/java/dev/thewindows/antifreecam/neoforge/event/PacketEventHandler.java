package dev.thewindows.antifreecam.neoforge.event;

import dev.thewindows.antifreecam.common.detection.FreecamDetector;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

// NeoForge does not expose a packet-receive event directly for movement.
// We use a mixin approach instead (see AntiFreeamNeoForgeMod for setup).
// This handler is for player join/quit lifecycle events.
public class PacketEventHandler {

    private final FreecamDetector detector;
    private long tick = 0;

    public PacketEventHandler(FreecamDetector detector) {
        this.detector = detector;
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) {
            detector.initPlayer(sp.getUUID());
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        detector.removePlayer(event.getEntity().getUUID());
    }

    public void recordMovement(java.util.UUID player, double x, double y, double z,
                                float yaw, float pitch, boolean onGround) {
        detector.recordMovement(player, x, y, z, yaw, pitch, onGround, tick++);
    }
}
