/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.retrooper.packetevents.PacketEvents
 *  com.github.retrooper.packetevents.event.PacketListenerAbstract
 *  com.github.retrooper.packetevents.event.PacketListenerCommon
 *  com.github.retrooper.packetevents.event.PacketListenerPriority
 *  com.github.retrooper.packetevents.event.PacketReceiveEvent
 *  com.github.retrooper.packetevents.protocol.packettype.PacketType$Configuration$Client
 *  com.github.retrooper.packetevents.protocol.packettype.PacketType$Play$Client
 *  com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon
 *  com.github.retrooper.packetevents.wrapper.configuration.client.WrapperConfigClientPluginMessage
 *  com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.plugin.Plugin
 */
import com.anticheat.antiesp.AntiESPFreecamPlugin;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.configuration.client.WrapperConfigClientPluginMessage;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class c
implements Listener {
    private final AntiESPFreecamPlugin a;
    private PacketListenerAbstract b;
    private final Map<UUID, String> c = new ConcurrentHashMap<UUID, String>();
    private final Map<UUID, Set<String>> d = new ConcurrentHashMap<UUID, Set<String>>();
    private static final Set<String> e = Set.of("fabric", "quilt", "forge", "fml,forge", "neoforge", "lunarclient", "lunar", "badlion", "labymod");
    private static final String[] f = new String[]{"fabric:", "fabric-", "forge:", "fml:", "neoforge:", "wurst:", "meteor-client:", "litematica:", "freecam:", "xaero", "worldedit:", "tweakeroo:", "plasmo:", "labymod:", "badlion:", "lunarclient:"};

    public c(AntiESPFreecamPlugin antiESPFreecamPlugin) {
        this.a = antiESPFreecamPlugin;
    }

    public void a() {
        this.b = new PacketListenerAbstract(PacketListenerPriority.NORMAL){

            public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
                PacketTypeCommon packetTypeCommon = packetReceiveEvent.getPacketType();
                if (packetTypeCommon == PacketType.Play.Client.PLUGIN_MESSAGE) {
                    WrapperPlayClientPluginMessage wrapperPlayClientPluginMessage = new WrapperPlayClientPluginMessage(packetReceiveEvent);
                    c.this.a(packetReceiveEvent, wrapperPlayClientPluginMessage.getChannelName(), wrapperPlayClientPluginMessage.getData());
                } else if (packetTypeCommon == PacketType.Configuration.Client.PLUGIN_MESSAGE) {
                    WrapperConfigClientPluginMessage wrapperConfigClientPluginMessage = new WrapperConfigClientPluginMessage(packetReceiveEvent);
                    c.this.a(packetReceiveEvent, wrapperConfigClientPluginMessage.getChannelName(), wrapperConfigClientPluginMessage.getData());
                }
            }
        };
        PacketEvents.getAPI().getEventManager().registerListener((PacketListenerCommon)this.b);
    }

    public void b() {
        if (this.b != null) {
            PacketEvents.getAPI().getEventManager().unregisterListener((PacketListenerCommon)this.b);
            this.b = null;
        }
        this.c.clear();
        this.d.clear();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent playerQuitEvent) {
        UUID uUID = playerQuitEvent.getPlayer().getUniqueId();
        this.c.remove(uUID);
        this.d.remove(uUID);
    }

    void a(PacketReceiveEvent packetReceiveEvent, String string, byte[] byArray) {
        Player player = (Player)packetReceiveEvent.getPlayer();
        if (player == null) {
            return;
        }
        UUID uUID = player.getUniqueId();
        if ("minecraft:brand".equals(string) || "MC|Brand".equals(string)) {
            this.a(player, uUID, byArray);
        } else if ("minecraft:register".equals(string) || "REGISTER".equals(string)) {
            this.b(player, uUID, byArray);
        }
    }

    private void a(Player player, UUID uUID, byte[] byArray) {
        if (byArray == null || byArray.length == 0) {
            return;
        }
        String string = c.a(byArray);
        this.c.put(uUID, string);
        String string2 = string.toLowerCase().trim();
        if (c.a(string2) && this.a.getConfig().getBoolean("brandDetection.notifyAdmins", true)) {
            this.c(player.getName() + " joined with modded client: \u00a7e" + string);
        }
    }

    private void b(Player player, UUID uUID2, byte[] byArray) {
        if (byArray == null || byArray.length == 0) {
            return;
        }
        String string = new String(byArray, StandardCharsets.UTF_8);
        String[] stringArray = string.split("\u0000");
        Set set = this.d.computeIfAbsent(uUID2, uUID -> ConcurrentHashMap.newKeySet());
        boolean bl = false;
        StringBuilder stringBuilder = new StringBuilder();
        for (String string2 : stringArray) {
            if (string2.isEmpty()) continue;
            set.add(string2);
            if (!c.b(string2)) continue;
            bl = true;
            if (stringBuilder.length() > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(string2);
        }
        if (bl && this.a.getConfig().getBoolean("brandDetection.notifyAdmins", true)) {
            this.c(player.getName() + " registered suspicious channels: \u00a7e" + String.valueOf(stringBuilder));
        }
    }

    public String a(UUID uUID) {
        return this.c.get(uUID);
    }

    public Set<String> b(UUID uUID) {
        return this.d.getOrDefault(uUID, Set.of());
    }

    public boolean c(UUID uUID) {
        String string = this.c.get(uUID);
        return string != null && c.a(string.toLowerCase().trim());
    }

    private static boolean a(String string) {
        if (e.contains(string)) {
            return true;
        }
        for (String string2 : e) {
            if (!string.contains(string2)) continue;
            return true;
        }
        return !"vanilla".equals(string);
    }

    private static boolean b(String string) {
        String string2 = string.toLowerCase();
        for (String string3 : f) {
            if (!string2.startsWith(string3)) continue;
            return true;
        }
        return false;
    }

    private static String a(byte[] byArray) {
        int n = 0;
        int n2 = 0;
        int n3 = 0;
        while (n < byArray.length) {
            byte by = byArray[n++];
            n2 |= (by & 0x7F) << n3;
            if ((by & 0x80) != 0 && (n3 += 7) <= 21) continue;
            break;
        }
        if (n2 > 0 && n + n2 <= byArray.length) {
            return new String(byArray, n, n2, StandardCharsets.UTF_8);
        }
        return new String(byArray, StandardCharsets.UTF_8).trim();
    }

    private void c(String string) {
        String string2 = "\u00a7c[AntiESP] \u00a77" + string;
        Runnable runnable = () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("antiesp.notify")) continue;
                player.sendMessage(string2);
            }
            Bukkit.getConsoleSender().sendMessage(string2);
        };
        if (e.a()) {
            try {
                Object object = Bukkit.class.getMethod("getGlobalRegionScheduler", new Class[0]).invoke(null, new Object[0]);
                object.getClass().getMethod("execute", Plugin.class, Runnable.class).invoke(object, new Object[]{this.a, runnable});
            }
            catch (Exception exception) {
                runnable.run();
            }
        } else {
            Bukkit.getScheduler().runTask((Plugin)this.a, runnable);
        }
    }
}

