package dev.thewindows.antifreecam.fabric.mixin;

import dev.thewindows.antifreecam.fabric.AntiFreeamFabricMod;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onPlayerMove", at = @At("HEAD"))
    private void antifreecam$onPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if (player == null || !packet.changesPosition() && !packet.changesLook()) return;

        double x = packet.changesPosition() ? packet.getX(player.getX()) : player.getX();
        double y = packet.changesPosition() ? packet.getY(player.getY()) : player.getY();
        double z = packet.changesPosition() ? packet.getZ(player.getZ()) : player.getZ();
        float yaw = packet.changesLook() ? packet.getYaw(player.getYaw()) : player.getYaw();
        float pitch = packet.changesLook() ? packet.getPitch(player.getPitch()) : player.getPitch();
        boolean onGround = packet.isOnGround();

        AntiFreeamFabricMod.getInstance().recordMovement(
            player.getUuid(), x, y, z, yaw, pitch, onGround
        );
    }
}
