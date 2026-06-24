package dev.opinion2nd.antiespguard.fabric.mixin;

import dev.opinion2nd.antiespguard.common.AntiEspConfig;
import dev.opinion2nd.antiespguard.common.ModSignatures;
import dev.opinion2nd.antiespguard.common.WebhookClient;
import dev.opinion2nd.antiespguard.fabric.PlayerMaskState;
import dev.opinion2nd.antiespguard.fabric.ServerMaskRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.Inject;

/**
 * Cheat-mod detection from the client's {@code minecraft:brand} payload and any
 * custom channel namespaces it sends — the Fabric equivalent of the Paper
 * {@code ModDetectionListener}. Reports to ops + Discord and can auto-kick.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGameCustomPayloadMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    private void antiespguard$detect(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        ServerMaskRuntime rt = ServerMaskRuntime.get();
        AntiEspConfig.ModDetection md = rt.config().modDetection;
        PlayerMaskState data = rt.getOrCreate(player.getUUID());
        if (data.modFlagged) {
            return;
        }

        CustomPacketPayload payload = packet.payload();
        String haystack;
        if (payload instanceof BrandPayload brand) {
            haystack = brand.brand();
        } else if (payload != null && payload.type() != null) {
            haystack = payload.type().id().toString();
        } else {
            return;
        }

        String mod = ModSignatures.match(haystack, md.detect);
        if (mod == null) {
            return;
        }
        data.modFlagged = true;
        report(md, mod);
    }

    private void report(AntiEspConfig.ModDetection md, String mod) {
        if (md.notifyAdmins) {
            Component note = Component.literal("[AntiESPGuard] " + player.getGameProfile().getName()
                    + " flagged for cheat mod: " + mod);
            player.server.getPlayerList().getPlayers().stream()
                    .filter(p -> p.hasPermissions(2))
                    .forEach(p -> p.sendSystemMessage(note));
        }
        WebhookClient.postEmbed(md.discordWebhook,
                "AntiESPGuard — cheat mod detected",
                "**" + player.getGameProfile().getName() + "** flagged for `" + mod + "`",
                md.discordColor);
        if (md.autoKick) {
            String msg = md.kickMessage.replace("{mod}", mod).replace('§', '&');
            player.connection.disconnect(Component.literal(msg));
        }
    }
}
