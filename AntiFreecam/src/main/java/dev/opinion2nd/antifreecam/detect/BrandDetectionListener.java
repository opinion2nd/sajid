package dev.opinion2nd.antifreecam.detect;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import dev.opinion2nd.antifreecam.AfConfig;
import dev.opinion2nd.antifreecam.mask.MaskService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Best-effort client detection from the {@code minecraft:brand} channel.
 *
 * <p>Reading the brand is reliable; matching a cheat client by it is not — many
 * clients spoof the brand to {@code vanilla}. Treat a hit as a signal. The
 * watch-list comes from {@code modDetection.detect} in config.yml.
 */
public final class BrandDetectionListener extends PacketListenerAbstract {

    private static final String BRAND_CHANNEL = "minecraft:brand";

    private final Plugin plugin;
    private final MaskService service;

    public BrandDetectionListener(Plugin plugin, MaskService service) {
        super(PacketListenerPriority.NORMAL);
        this.plugin = plugin;
        this.service = service;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        AfConfig cfg = service.config();
        if (!cfg.modDetectionEnabled || cfg.watchedBrands.isEmpty()) {
            return;
        }
        if (event.getPacketType() != PacketType.Play.Client.PLUGIN_MESSAGE) {
            return;
        }
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        WrapperPlayClientPluginMessage msg = new WrapperPlayClientPluginMessage(event);
        if (!BRAND_CHANNEL.equalsIgnoreCase(msg.getChannelName())) {
            return;
        }

        String brand = new String(msg.getData(), StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
        for (String needle : cfg.watchedBrands) {
            if (brand.contains(needle)) {
                flag(player, needle, cfg);
                return;
            }
        }
    }

    private void flag(Player player, String mod, AfConfig cfg) {
        // Hop to the main thread for any Bukkit interaction.
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            if (cfg.notifyAdmins) {
                String note = ChatColor.RED + "[AntiFreecam] " + ChatColor.YELLOW
                        + player.getName() + " flagged for client: " + mod;
                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission("antifreecam.reload"))
                        .forEach(p -> p.sendMessage(note));
                plugin.getLogger().info(player.getName() + " flagged for client brand: " + mod);
            }
            if (cfg.autoKick) {
                player.kickPlayer(cfg.kickMessage.replace("{mod}", mod));
            }
        });
    }
}
