package dev.thewindows.antifreecam.neoforge.mixin;

import dev.thewindows.antifreecam.neoforge.AntiFreeamNeoForgeMod;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleMovePlayer", at = @At("HEAD"))
    private void antifreecam$handleMovePlayer(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        if (player == null || (!packet.hasPosition() && !packet.hasRotation())) return;

        double x = packet.hasPosition() ? packet.getX(player.getX()) : player.getX();
        double y = packet.hasPosition() ? packet.getY(player.getY()) : player.getY();
        double z = packet.hasPosition() ? packet.getZ(player.getZ()) : player.getZ();
        float yaw = packet.hasRotation() ? packet.getYRot(player.getYRot()) : player.getYRot();
        float pitch = packet.hasRotation() ? packet.getXRot(player.getXRot()) : player.getXRot();
        boolean onGround = packet.isOnGround();

        AntiFreeamNeoForgeMod.getInstance().recordMovement(
            player.getUUID(), x, y, z, yaw, pitch, onGround
        );
    }
}
