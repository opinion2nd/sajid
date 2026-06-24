package dev.opinion2nd.antiespguard.paper.detect;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import dev.opinion2nd.antiespguard.common.AntiEspConfig;
import dev.opinion2nd.antiespguard.common.ModSignatures;
import dev.opinion2nd.antiespguard.common.WebhookClient;
import dev.opinion2nd.antiespguard.paper.PaperConfig;
import dev.opinion2nd.antiespguard.paper.mask.MaskService;
import dev.opinion2nd.antiespguard.paper.util.Schedulers;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

/**
 * Cheat-mod detection from two non-intrusive client signals:
 * <ul>
 *   <li>{@code minecraft:brand} — the client's self-reported brand string.</li>
 *   <li>{@code minecraft:register} — the custom plugin/mod channels a client
 *       advertises (many client mods register tell-tale channels).</li>
 * </ul>
 *
 * <p>A match is reported to staff (and optionally Discord) and can auto-kick.
 * The watch-list is {@code modDetection.detect} in config.yml. Brand spoofing
 * means this is a strong signal, not proof — matches are deduplicated per
 * player so staff aren't spammed.</p>
 */
public final class ModDetectionListener extends PacketListenerAbstract implements Listener {

    private static final String BRAND_CHANNEL = "minecraft:brand";
    private static final String REGISTER_CHANNEL = "minecraft:register";

    private final Plugin plugin;
    private final MaskService service;
    private final Set<UUID> flagged = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public ModDetectionListener(Plugin plugin, MaskService service) {
        super(PacketListenerPriority.NORMAL);
        this.plugin = plugin;
        this.service = service;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.PLUGIN_MESSAGE) {
            return;
        }
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        PaperConfig cfg = service.config();
        AntiEspConfig.ModDetection md = cfg.raw().modDetection;
        if (flagged.contains(player.getUniqueId())) {
            return;
        }

        WrapperPlayClientPluginMessage msg = new WrapperPlayClientPluginMessage(event);
        String channel = msg.getChannelName();
        if (channel == null) {
            return;
        }

        String haystack;
        if (channel.equalsIgnoreCase(BRAND_CHANNEL)) {
            haystack = new String(msg.getData(), StandardCharsets.UTF_8);
        } else if (channel.equalsIgnoreCase(REGISTER_CHANNEL)) {
            // REGISTER payload is a NUL-separated list of channel names.
            haystack = new String(msg.getData(), StandardCharsets.UTF_8).replace('\0', ' ');
        } else {
            // Some mods register their own namespaced channels directly.
            haystack = channel;
        }

        String mod = ModSignatures.match(haystack, md.detect);
        if (mod != null) {
            flagged.add(player.getUniqueId());
            flag(player, mod, cfg);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        flagged.remove(event.getPlayer().getUniqueId());
    }

    private void flag(Player player, String mod, PaperConfig cfg) {
        AntiEspConfig.ModDetection md = cfg.raw().modDetection;
        // Any Bukkit interaction must run on the player's owning thread.
        Schedulers.runForEntity(plugin, player, () -> {
            if (!player.isOnline()) {
                return;
            }
            if (md.notifyAdmins) {
                String note = ChatColor.RED + "[AntiESPGuard] " + ChatColor.YELLOW
                        + player.getName() + " flagged for cheat mod: " + mod;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.hasPermission("antiespguard.notify")) {
                        p.sendMessage(note);
                    }
                }
                plugin.getLogger().info(player.getName() + " flagged for cheat mod: " + mod);
            }
            WebhookClient.postEmbed(md.discordWebhook,
                    "AntiESPGuard — cheat mod detected",
                    "**" + player.getName() + "** flagged for `" + mod + "`",
                    md.discordColor);
            if (md.autoKick) {
                player.kickPlayer(md.kickMessage.replace("{mod}", mod));
            }
        });
    }
}
