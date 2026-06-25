/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.retrooper.packetevents.PacketEvents
 *  com.github.retrooper.packetevents.event.PacketListenerAbstract
 *  com.github.retrooper.packetevents.event.PacketListenerCommon
 *  com.github.retrooper.packetevents.event.PacketListenerPriority
 *  com.github.retrooper.packetevents.event.PacketReceiveEvent
 *  com.github.retrooper.packetevents.protocol.nbt.NBT
 *  com.github.retrooper.packetevents.protocol.nbt.NBTByte
 *  com.github.retrooper.packetevents.protocol.nbt.NBTCompound
 *  com.github.retrooper.packetevents.protocol.nbt.NBTList
 *  com.github.retrooper.packetevents.protocol.nbt.NBTString
 *  com.github.retrooper.packetevents.protocol.nbt.NBTType
 *  com.github.retrooper.packetevents.protocol.packettype.PacketType$Play$Client
 *  com.github.retrooper.packetevents.protocol.world.blockentity.BlockEntityTypes
 *  com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState
 *  com.github.retrooper.packetevents.protocol.world.states.type.StateType
 *  com.github.retrooper.packetevents.protocol.world.states.type.StateTypes
 *  com.github.retrooper.packetevents.util.Vector3i
 *  com.github.retrooper.packetevents.wrapper.PacketWrapper
 *  com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUpdateSign
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockEntityData
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenSignEditor
 *  net.kyori.adventure.text.Component
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.block.Block
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.plugin.Plugin
 */
import com.anticheat.antiesp.AntiESPFreecamPlugin;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.nbt.NBT;
import com.github.retrooper.packetevents.protocol.nbt.NBTByte;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTList;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.nbt.NBTType;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.blockentity.BlockEntityTypes;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUpdateSign;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockEntityData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenSignEditor;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class f
implements Listener {
    private static final String[][] a = new String[][]{{"key.freecam.toggle", "Toggle Freecam", "Freecam"}, {"key.meteor-client.open-gui", "Open GUI", "Meteor Client"}, {"key.wurst.zoom", "Zoom", "Wurst"}, {"gui.xaero_enlarge_map", "Enlarge Minimap", "Xaero's Minimap"}};
    private String[][] b = a;
    private final AntiESPFreecamPlugin c;
    private final Logger d;
    private boolean e;
    private final Map<UUID, Vector3i> f = new ConcurrentHashMap<UUID, Vector3i>();
    private final Map<UUID, Integer> g = new ConcurrentHashMap<UUID, Integer>();
    private final Set<UUID> h = ConcurrentHashMap.newKeySet();
    private final Map<UUID, String> i = new ConcurrentHashMap<UUID, String>();
    private PacketListenerAbstract j;

    public f(AntiESPFreecamPlugin antiESPFreecamPlugin) {
        this.c = antiESPFreecamPlugin;
        this.d = antiESPFreecamPlugin.getLogger();
    }

    public String a(UUID uUID) {
        return this.i.get(uUID);
    }

    public boolean b(UUID uUID) {
        return this.h.contains(uUID);
    }

    public void a() {
        this.b();
        this.e = f.e();
        this.d.info("[SignProbe] Started. useJsonTextComponents=" + this.e + ", version=" + Bukkit.getMinecraftVersion() + ", activeMods=" + this.b.length);
        this.d();
        this.j = new PacketListenerAbstract(PacketListenerPriority.HIGHEST){

            public void onPacketReceive(PacketReceiveEvent packetReceiveEvent) {
                if (packetReceiveEvent.getPacketType() == PacketType.Play.Client.UPDATE_SIGN) {
                    f.this.a(packetReceiveEvent);
                }
            }
        };
        PacketEvents.getAPI().getEventManager().registerListener((PacketListenerCommon)this.j);
    }

    private void d() {
        long l = System.nanoTime();
        try {
            WrappedBlockState.getDefaultState((StateType)StateTypes.OAK_SIGN).getGlobalId();
            WrappedBlockState.getByString((String)"minecraft:stone").getGlobalId();
            Objects.requireNonNull(BlockEntityTypes.SIGN);
        }
        catch (Throwable throwable) {
            this.d.warning("[SignProbe] registry warmup failed (non-fatal, probe will init lazily): " + String.valueOf(throwable));
            return;
        }
        long l2 = (System.nanoTime() - l) / 1000000L;
        this.d.info("[SignProbe] packetevents block registries warmed up in " + l2 + "ms.");
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent playerJoinEvent) {
        if (!this.c.getConfig().getBoolean("modDetection.probeOnJoin", true)) {
            return;
        }
        Player player = playerJoinEvent.getPlayer();
        if (player.hasPermission("antiesp.probe.bypass")) {
            return;
        }
        if (f.c(player)) {
            return;
        }
        if (this.h.contains(player.getUniqueId())) {
            return;
        }
        e.a((Plugin)this.c, (Entity)player, () -> {
            if (player.isOnline()) {
                this.b(player);
            }
        }, 100L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent playerQuitEvent) {
        UUID uUID = playerQuitEvent.getPlayer().getUniqueId();
        this.f.remove(uUID);
        this.g.remove(uUID);
        this.h.remove(uUID);
        this.i.remove(uUID);
    }

    public void a(Player player) {
        UUID uUID = player.getUniqueId();
        this.a(player, uUID);
        this.f.remove(uUID);
        this.g.remove(uUID);
        this.h.remove(uUID);
        e.a((Plugin)this.c, (Entity)player, () -> {
            if (player.isOnline()) {
                this.b(player);
            }
        });
    }

    private void b(Player player) {
        if (!player.isOnline()) {
            return;
        }
        if (player.hasActiveItem()) {
            e.a((Plugin)this.c, (Entity)player, () -> {
                if (player.isOnline()) {
                    this.b(player);
                }
            }, 40L);
            return;
        }
        this.h.add(player.getUniqueId());
        Location location = player.getLocation();
        int n = Math.max(location.getWorld().getMinHeight(), location.getBlockY() - 2);
        Vector3i vector3i = new Vector3i(location.getBlockX(), n, location.getBlockZ());
        Block block = location.getWorld().getBlockAt(vector3i.getX(), vector3i.getY(), vector3i.getZ());
        int n2 = this.a(block);
        this.g.put(player.getUniqueId(), n2);
        this.f.put(player.getUniqueId(), vector3i);
        WrappedBlockState wrappedBlockState = WrappedBlockState.getDefaultState((StateType)StateTypes.OAK_SIGN);
        WrapperPlayServerBlockChange wrapperPlayServerBlockChange = new WrapperPlayServerBlockChange(vector3i, wrappedBlockState.getGlobalId());
        PacketEvents.getAPI().getPlayerManager().sendPacket((Object)player, (PacketWrapper)wrapperPlayServerBlockChange);
        NBTCompound nBTCompound = this.c(player.getUniqueId());
        WrapperPlayServerBlockEntityData wrapperPlayServerBlockEntityData = new WrapperPlayServerBlockEntityData(vector3i, BlockEntityTypes.SIGN, nBTCompound);
        PacketEvents.getAPI().getPlayerManager().sendPacket((Object)player, (PacketWrapper)wrapperPlayServerBlockEntityData);
        e.a((Plugin)this.c, (Entity)player, () -> {
            if (!player.isOnline() || !this.f.containsKey(player.getUniqueId())) {
                return;
            }
            WrapperPlayServerOpenSignEditor wrapperPlayServerOpenSignEditor = new WrapperPlayServerOpenSignEditor(vector3i, true);
            PacketEvents.getAPI().getPlayerManager().sendPacket((Object)player, (PacketWrapper)wrapperPlayServerOpenSignEditor);
            e.a((Plugin)this.c, (Entity)player, () -> {
                if (!player.isOnline() || !this.f.containsKey(player.getUniqueId())) {
                    return;
                }
                this.a(player, player.getUniqueId());
                e.a((Plugin)this.c, (Entity)player, () -> {
                    if (this.f.containsKey(player.getUniqueId())) {
                        this.d.warning("[SignProbe] TIMEOUT: No UpdateSign from " + player.getName() + " -- sign editor never opened or client didn't respond.");
                        this.f.remove(player.getUniqueId());
                        this.g.remove(player.getUniqueId());
                    }
                }, 200L);
            }, 3L);
        }, 1L);
    }

    private NBTCompound c(UUID uUID) {
        NBTList nBTList;
        NBTCompound nBTCompound = new NBTCompound();
        int n = Math.min(this.b.length, 4);
        NBTCompound nBTCompound2 = new NBTCompound();
        if (this.e) {
            nBTList = new NBTList(NBTType.STRING);
            for (var6_6 = 0; var6_6 < n; ++var6_6) {
                nBTList.addTag((NBT)new NBTString("{\"translate\":\"" + this.b[var6_6][0] + "\"}"));
            }
            for (var6_6 = n; var6_6 < 4; ++var6_6) {
                nBTList.addTag((NBT)new NBTString("{\"text\":\"\"}"));
            }
            nBTCompound2.setTag("messages", (NBT)nBTList);
        } else {
            NBTCompound nBTCompound3;
            nBTList = new NBTList(NBTType.COMPOUND);
            for (var6_6 = 0; var6_6 < n; ++var6_6) {
                nBTCompound3 = new NBTCompound();
                nBTCompound3.setTag("translate", (NBT)new NBTString(this.b[var6_6][0]));
                nBTList.addTag((NBT)nBTCompound3);
            }
            for (var6_6 = n; var6_6 < 4; ++var6_6) {
                nBTCompound3 = new NBTCompound();
                nBTCompound3.setTag("text", (NBT)new NBTString(""));
                nBTList.addTag((NBT)nBTCompound3);
            }
            nBTCompound2.setTag("messages", (NBT)nBTList);
        }
        nBTCompound2.setTag("has_glowing_text", (NBT)new NBTByte(0));
        nBTCompound2.setTag("color", (NBT)new NBTString("black"));
        nBTCompound.setTag("front_text", (NBT)nBTCompound2);
        nBTList = new NBTCompound();
        if (this.e) {
            NBTList nBTList2 = new NBTList(NBTType.STRING);
            for (int i = 0; i < 4; ++i) {
                nBTList2.addTag((NBT)new NBTString("{\"text\":\"\"}"));
            }
            nBTList.setTag("messages", (NBT)nBTList2);
        } else {
            NBTList nBTList3 = new NBTList(NBTType.COMPOUND);
            for (int i = 0; i < 4; ++i) {
                NBTCompound nBTCompound4 = new NBTCompound();
                nBTCompound4.setTag("text", (NBT)new NBTString(""));
                nBTList3.addTag((NBT)nBTCompound4);
            }
            nBTList.setTag("messages", (NBT)nBTList3);
        }
        nBTList.setTag("has_glowing_text", (NBT)new NBTByte(0));
        nBTList.setTag("color", (NBT)new NBTString("black"));
        nBTCompound.setTag("back_text", (NBT)nBTList);
        nBTCompound.setTag("is_waxed", (NBT)new NBTByte(0));
        return nBTCompound;
    }

    void a(PacketReceiveEvent packetReceiveEvent) {
        String string;
        Player player = (Player)packetReceiveEvent.getPlayer();
        if (player == null) {
            return;
        }
        UUID uUID = player.getUniqueId();
        Vector3i vector3i = this.f.get(uUID);
        if (vector3i == null) {
            return;
        }
        WrapperPlayClientUpdateSign wrapperPlayClientUpdateSign = new WrapperPlayClientUpdateSign(packetReceiveEvent);
        Vector3i vector3i2 = wrapperPlayClientUpdateSign.getBlockPosition();
        if (vector3i.getX() != vector3i2.getX() || vector3i.getY() != vector3i2.getY() || vector3i.getZ() != vector3i2.getZ()) {
            return;
        }
        packetReceiveEvent.setCancelled(true);
        this.a(player, uUID);
        this.f.remove(uUID);
        this.g.remove(uUID);
        String[] stringArray = wrapperPlayClientUpdateSign.getTextLines();
        String string2 = null;
        int n = Math.min(this.b.length, 4);
        for (int i = 0; i < n; ++i) {
            String string3 = string = stringArray != null && i < stringArray.length ? stringArray[i] : "";
            if (string == null || string.isEmpty() || string.equals(this.b[i][0])) continue;
            string2 = this.b[i][2];
            break;
        }
        if (string2 != null) {
            int n2;
            this.i.put(uUID, string2);
            String string4 = string2;
            if (this.c.getConfig().getBoolean("modDetection.notifyAdmins", true)) {
                e.a((Plugin)this.c, (Entity)player, () -> {
                    for (Player player2 : Bukkit.getOnlinePlayers()) {
                        if (!player2.hasPermission("antiesp.notify")) continue;
                        player2.sendMessage((Component)Component.text((String)("\u00a7c[AntiESP] \u00a7e" + player.getName() + " \u00a77detected using \u00a7c" + string4 + " \u00a77(sign probe)")));
                    }
                });
            }
            if ((string = this.c.getConfig().getString("modDetection.discordWebhook", "")) != null && !string.isEmpty()) {
                n2 = this.c.getConfig().getInt("modDetection.discordColor", 0xFF0000);
                this.a(string, player.getName(), string4, n2);
            }
            if ((n2 = this.c.getConfig().getBoolean("modDetection.autoKick", true)) != 0) {
                String string5 = this.c.getConfig().getString("modDetection.kickMessage", "\u00a7cYou are using a cheat mod that is not allowed on this server ({mod})!");
                String string6 = string5.replace("{mod}", string4);
                e.a((Plugin)this.c, (Entity)player, () -> {
                    if (player.isOnline()) {
                        player.kick((Component)Component.text((String)string6));
                    }
                }, 1L);
            }
        }
    }

    private void a(Player player, UUID uUID) {
        Vector3i vector3i = this.f.get(uUID);
        Integer n = this.g.get(uUID);
        if (vector3i == null || n == null || !player.isOnline()) {
            return;
        }
        WrapperPlayServerBlockChange wrapperPlayServerBlockChange = new WrapperPlayServerBlockChange(vector3i, n.intValue());
        PacketEvents.getAPI().getPlayerManager().sendPacket((Object)player, (PacketWrapper)wrapperPlayServerBlockChange);
    }

    private int a(Block block) {
        try {
            String string = block.getBlockData().getAsString();
            return WrappedBlockState.getByString((String)string).getGlobalId();
        }
        catch (Exception exception) {
            return 0;
        }
    }

    private static boolean c(Player player) {
        return player.getUniqueId().version() == 0;
    }

    private static boolean e() {
        try {
            int n;
            String string = Bukkit.getMinecraftVersion();
            String[] stringArray = string.split("\\.");
            int n2 = Integer.parseInt(stringArray[0]);
            int n3 = stringArray.length > 1 ? Integer.parseInt(stringArray[1]) : 0;
            int n4 = n = stringArray.length > 2 ? Integer.parseInt(stringArray[2]) : 0;
            if (n2 != 1) {
                return false;
            }
            if (n3 != 21) {
                return n3 < 21;
            }
            return n < 5;
        }
        catch (Exception exception) {
            return false;
        }
    }

    private void a(String string, String string2, String string3, int n) {
        Thread thread = new Thread(() -> {
            try {
                String string4 = string2.replace("\\", "\\\\").replace("\"", "\\\"");
                String string5 = string3.replace("\\", "\\\\").replace("\"", "\\\"");
                String string6 = "{\"embeds\":[{\"title\":\"Cheat Mod Detected\",\"description\":\"**" + string4 + "** was detected using **" + string5 + "**\",\"color\":" + n + ",\"footer\":{\"text\":\"AntiESP Mod Detection\"},\"timestamp\":\"" + Instant.now().toString() + "\"}]}";
                HttpURLConnection httpURLConnection = (HttpURLConnection)URI.create(string).toURL().openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setReadTimeout(5000);
                try (OutputStream outputStream = httpURLConnection.getOutputStream();){
                    outputStream.write(string6.getBytes(StandardCharsets.UTF_8));
                }
                httpURLConnection.getResponseCode();
                httpURLConnection.disconnect();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }, "AntiESP-Webhook");
        thread.setDaemon(true);
        thread.start();
    }

    public void b() {
        ConfigurationSection configurationSection = this.c.getConfig().getConfigurationSection("modDetection.detect");
        ArrayList<String[]> arrayList = new ArrayList<String[]>();
        for (String[] stringArray : a) {
            boolean bl;
            boolean bl2 = bl = configurationSection == null || configurationSection.getBoolean(stringArray[2], true);
            if (!bl) continue;
            arrayList.add(stringArray);
        }
        this.b = arrayList.isEmpty() ? a : (String[][])arrayList.toArray((T[])new String[0][]);
    }

    public void c() {
        if (this.j != null) {
            PacketEvents.getAPI().getEventManager().unregisterListener((PacketListenerCommon)this.j);
            this.j = null;
        }
        for (UUID uUID : new ArrayList<UUID>(this.f.keySet())) {
            Player player = Bukkit.getPlayer((UUID)uUID);
            if (player == null || !player.isOnline()) continue;
            this.a(player, uUID);
        }
        this.f.clear();
        this.g.clear();
        this.i.clear();
        this.h.clear();
    }
}

