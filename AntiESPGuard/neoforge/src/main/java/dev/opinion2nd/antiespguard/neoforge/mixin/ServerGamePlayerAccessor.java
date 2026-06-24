package dev.opinion2nd.antiespguard.neoforge.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes {@code ServerGamePacketListenerImpl.player} regardless of its access
 * modifier, so the packet-send mixin (which targets the common superclass) can
 * resolve the recipient without assuming the field is public.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePlayerAccessor {

    @Accessor("player")
    ServerPlayer antiespguard$getPlayer();
}
