package dev.opinion2nd.antiespguard.fabric.mixin;

import dev.opinion2nd.antiespguard.common.AntiEspConfig;
import dev.opinion2nd.antiespguard.fabric.ChunkMasking;
import dev.opinion2nd.antiespguard.fabric.PlayerMaskState;
import dev.opinion2nd.antiespguard.fabric.ServerMaskRuntime;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Per-recipient outgoing-packet interception — the Fabric equivalent of the
 * Paper PacketEvents listeners.
 *
 * <ul>
 *   <li>CHUNK packets to a masking recipient are swapped for a below-Y masked
 *       copy ({@link ChunkMasking}).</li>
 *   <li>ADD_ENTITY packets for entities/players below the hide line are dropped
 *       for surface recipients (entity-radar / combat-ESP defence).</li>
 * </ul>
 */
@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerSendMixin {

    @ModifyVariable(
            method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Packet<?> antiespguard$maskChunk(Packet<?> packet) {
        if (!(((Object) this) instanceof ServerGamePacketListenerImpl game)) {
            return packet;
        }
        if (!(packet instanceof ClientboundLevelChunkWithLightPacket chunkPacket)) {
            return packet;
        }
        ServerPlayer player = ((ServerGamePlayerAccessor) game).antiespguard$getPlayer();
        ServerMaskRuntime rt = ServerMaskRuntime.get();
        if (rt.shouldMaskChunk(player, chunkPacket.getX(), chunkPacket.getZ())) {
            return ChunkMasking.maskedPacket(player, chunkPacket);
        }
        return packet;
    }

    @Inject(
            method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V",
            at = @At("HEAD"), cancellable = true)
    private void antiespguard$maskEntity(Packet<?> packet, PacketSendListener listener, CallbackInfo ci) {
        if (!(((Object) this) instanceof ServerGamePacketListenerImpl game)) {
            return;
        }
        if (!(packet instanceof ClientboundAddEntityPacket add)) {
            return;
        }
        ServerMaskRuntime rt = ServerMaskRuntime.get();
        AntiEspConfig cfg = rt.config();
        if (!cfg.maskEntities && !cfg.maskUndergroundPlayers) {
            return;
        }
        ServerPlayer viewer = ((ServerGamePlayerAccessor) game).antiespguard$getPlayer();
        PlayerMaskState data = rt.state(viewer.getUUID());
        if (data == null || data.bypass || !data.worldActive || data.underground) {
            return; // only surface viewers are protected
        }
        if (add.getY() >= cfg.hideBelowY) {
            return;
        }
        boolean isPlayer = add.getType() == EntityType.PLAYER;
        if (isPlayer ? cfg.maskUndergroundPlayers : cfg.maskEntities) {
            ci.cancel();
        }
    }
}
