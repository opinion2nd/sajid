/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.destroystokyo.paper.profile.PlayerProfile
 *  com.destroystokyo.paper.profile.ProfileProperty
 *  com.github.retrooper.packetevents.PacketEvents
 *  com.github.retrooper.packetevents.event.PacketListenerAbstract
 *  com.github.retrooper.packetevents.event.PacketListenerCommon
 *  com.github.retrooper.packetevents.event.PacketListenerPriority
 *  com.github.retrooper.packetevents.event.PacketSendEvent
 *  com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
 *  com.github.retrooper.packetevents.protocol.packettype.PacketType$Play$Server
 *  com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon
 *  com.github.retrooper.packetevents.protocol.player.GameMode
 *  com.github.retrooper.packetevents.protocol.player.TextureProperty
 *  com.github.retrooper.packetevents.protocol.player.UserProfile
 *  com.github.retrooper.packetevents.wrapper.PacketWrapper
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate$Action
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate$PlayerInfo
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.format.TextColor
 *  net.kyori.adventure.text.minimessage.MiniMessage
 *  net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
 *  net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Location
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.World
 *  org.bukkit.World$Environment
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.RegisteredServiceProvider
 *  org.bukkit.scoreboard.Scoreboard
 *  org.bukkit.scoreboard.Team
 */
import com.anticheat.antiesp.AntiESPFreecamPlugin;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class d {
    private final AntiESPFreecamPlugin a;
    private final int b;
    private final int c;
    private final int d;
    private final boolean e;
    private final boolean f;
    private final Set<UUID> g;
    private final Set<World.Environment> h;
    private final Set<String> i;
    private static final LegacyComponentSerializer j = LegacyComponentSerializer.builder().character('\u00a7').hexColors().useUnusualXRepeatedCharacterHexFormat().build();
    private static final Pattern k = Pattern.compile("[&\u00a7]#([0-9A-Fa-f]{6})");
    private static final MiniMessage l = MiniMessage.miniMessage();
    private static final Pattern m = Pattern.compile("<[#!/a-zA-Z][^<>]*>");
    private final List<String> n;
    private final boolean o;
    private volatile boolean p = false;
    private volatile boolean q = false;
    private Method r;
    private volatile boolean s = false;
    private volatile boolean t = false;
    private Object u;
    private Method v;
    private Method w;
    private volatile boolean x = false;
    private volatile boolean y = false;
    private Object z;
    private Method A;
    private Method B;
    private Method C;
    private Method D;
    private Method E;
    private Method F;
    private final Map<Integer, Integer> G = new ConcurrentHashMap<Integer, Integer>();
    private final Map<UUID, Set<Integer>> H = new ConcurrentHashMap<UUID, Set<Integer>>();
    private final Set<UUID> I = ConcurrentHashMap.newKeySet();
    private final Set<Integer> J = ConcurrentHashMap.newKeySet();
    private PacketListenerAbstract K;
    private Object L;
    private volatile boolean M = false;
    private Method N;

    public d(AntiESPFreecamPlugin antiESPFreecamPlugin, int n, int n2, boolean bl, boolean bl2, Set<UUID> set, Set<World.Environment> set2, Set<String> set3) {
        this.a = antiESPFreecamPlugin;
        this.b = n;
        this.c = n * 4096;
        this.d = n2;
        this.e = bl;
        this.f = bl2;
        this.g = set;
        this.h = set2;
        this.i = set3;
        List<String> list = antiESPFreecamPlugin.getConfig().getStringList("tabPrefix.placeholderFormats");
        if (list == null || list.isEmpty()) {
            String string = antiESPFreecamPlugin.getConfig().getString("tabPrefix.placeholderFormat", "");
            list = string != null && !string.isEmpty() ? List.of(string) : List.of("%luckperms_prefix%%player_name%%luckperms_suffix%", "%vault_prefix%%player_name%%vault_suffix%");
        }
        this.n = list;
        this.o = antiESPFreecamPlugin.getConfig().getBoolean("tabPrefix.useVault", true);
    }

    public void a() {
        if (!this.e && !this.f) {
            return;
        }
        this.K = new PacketListenerAbstract(PacketListenerPriority.NORMAL){

            public void onPacketSend(PacketSendEvent packetSendEvent) {
                PacketTypeCommon packetTypeCommon = packetSendEvent.getPacketType();
                if (packetTypeCommon == PacketType.Play.Server.DESTROY_ENTITIES) {
                    d.this.c(packetSendEvent);
                    return;
                }
                if (packetTypeCommon == PacketType.Play.Server.SPAWN_ENTITY || packetTypeCommon == PacketType.Play.Server.ENTITY_TELEPORT || packetTypeCommon == PacketType.Play.Server.ENTITY_RELATIVE_MOVE || packetTypeCommon == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION || packetTypeCommon == PacketType.Play.Server.ENTITY_METADATA || packetTypeCommon == PacketType.Play.Server.ENTITY_VELOCITY || packetTypeCommon == PacketType.Play.Server.ENTITY_HEAD_LOOK || packetTypeCommon == PacketType.Play.Server.ENTITY_EQUIPMENT || packetTypeCommon == PacketType.Play.Server.ENTITY_ROTATION) {
                    d.this.a(packetSendEvent);
                }
            }
        };
        PacketEvents.getAPI().getEventManager().registerListener((PacketListenerCommon)this.K);
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.J.add(player.getEntityId());
        }
    }

    public void a(Player player) {
        this.J.add(player.getEntityId());
    }

    public void b() {
        if (this.L != null) {
            e.a(this.L);
            this.L = null;
        }
        if (this.K != null) {
            PacketEvents.getAPI().getEventManager().unregisterListener((PacketListenerCommon)this.K);
            this.K = null;
        }
        for (UUID uUID : this.g) {
            Player player = Bukkit.getPlayer((UUID)uUID);
            if (player == null) continue;
            for (UUID uUID2 : this.I) {
                Player player2 = Bukkit.getPlayer((UUID)uUID2);
                if (player2 == null || player.equals((Object)player2)) continue;
                try {
                    player.showPlayer((Plugin)this.a, player2);
                }
                catch (Exception exception) {}
            }
        }
        this.H.clear();
        this.G.clear();
        this.I.clear();
        this.J.clear();
    }

    public void c() {
        this.L = e.a((Plugin)this.a, this::e, 600L, 600L);
    }

    private void e() {
        if (this.G.size() > 16384) {
            this.G.clear();
        }
        this.H.keySet().removeIf(uUID -> Bukkit.getPlayer((UUID)uUID) == null);
        if (this.f) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uUID2 = player.getUniqueId();
                e.a((Plugin)this.a, (Entity)player, () -> {
                    if (!player.isOnline()) {
                        return;
                    }
                    boolean bl = this.g.contains(uUID2);
                    for (Player player2 : Bukkit.getOnlinePlayers()) {
                        boolean bl2;
                        UUID uUID2 = player2.getUniqueId();
                        if (uUID2.equals(uUID2) || (bl2 = bl && this.I.contains(uUID2)) || player.canSee(player2)) continue;
                        player.showPlayer((Plugin)this.a, player2);
                    }
                });
            }
        }
    }

    static Set<Integer> d() {
        return Collections.synchronizedSet(new LinkedHashSet<Integer>(){

            public boolean a(Integer n) {
                Iterator iterator;
                if (this.contains(n)) {
                    return false;
                }
                if (this.size() >= 4096 && (iterator = this.iterator()).hasNext()) {
                    iterator.next();
                    iterator.remove();
                }
                return super.add(n);
            }

            @Override
            public /* synthetic */ boolean add(Object object) {
                return this.a((Integer)object);
            }
        });
    }

    public void b(Player player) {
        UUID uUID2 = player.getUniqueId();
        World world = player.getWorld();
        if (this.f) {
            for (UUID serializable : this.I) {
                Player d2;
                if (serializable.equals(uUID2) || (d2 = Bukkit.getPlayer((UUID)serializable)) == null || !d2.getWorld().equals((Object)world) || !player.canSee(d2)) continue;
                player.hidePlayer((Plugin)this.a, d2);
                this.b(player, d2);
            }
        }
        if (this.e) {
            Set set = this.H.computeIfAbsent(uUID2, uUID -> d.d());
            ArrayList<Integer> arrayList = new ArrayList<Integer>();
            double d2 = Math.min(16.0 * (double)Bukkit.getViewDistance(), 128.0);
            double d3 = player.getLocation().getY() - (double)player.getWorld().getMinHeight();
            for (Entity entity : player.getNearbyEntities(d2, d3, d2)) {
                if (entity instanceof Player || !(entity.getLocation().getY() < (double)this.b)) continue;
                int n = entity.getEntityId();
                arrayList.add(n);
                set.add(n);
                this.G.put(n, (int)(entity.getLocation().getY() * 4096.0));
            }
            if (!arrayList.isEmpty()) {
                this.a(player, arrayList);
            }
        }
    }

    public void c(Player player) {
        UUID uUID = player.getUniqueId();
        if (this.f) {
            e.a((Plugin)this.a, (Entity)player, () -> {
                for (UUID uUID : this.I) {
                    Player player2 = Bukkit.getPlayer((UUID)uUID);
                    if (player2 == null || player2.equals((Object)player)) continue;
                    player.showPlayer((Plugin)this.a, player2);
                }
            });
        }
        if (this.e) {
            this.H.remove(uUID);
            this.k(player);
        }
    }

    public void d(Player player) {
        this.I.add(player.getUniqueId());
        if (!this.f) {
            return;
        }
        World world = player.getWorld();
        for (UUID uUID : this.g) {
            Player player2;
            if (uUID.equals(player.getUniqueId()) || (player2 = Bukkit.getPlayer((UUID)uUID)) == null || !player2.getWorld().equals((Object)world)) continue;
            e.a((Plugin)this.a, (Entity)player2, () -> {
                if (!player2.canSee(player)) {
                    return;
                }
                player2.hidePlayer((Plugin)this.a, player);
                this.b(player2, player);
            });
        }
    }

    public void e(Player player) {
        this.I.remove(player.getUniqueId());
        if (!this.f) {
            return;
        }
        for (UUID uUID : this.g) {
            Player player2;
            if (uUID.equals(player.getUniqueId()) || (player2 = Bukkit.getPlayer((UUID)uUID)) == null) continue;
            e.a((Plugin)this.a, (Entity)player2, () -> player2.showPlayer((Plugin)this.a, player));
        }
    }

    public void a(UUID uUID, int n) {
        this.H.remove(uUID);
        this.I.remove(uUID);
        this.J.remove(n);
    }

    public void f(Player player) {
        Player player2;
        UUID uUID = player.getUniqueId();
        this.J.add(player.getEntityId());
        if (this.I.remove(uUID) && this.f) {
            for (UUID uUID2 : this.g) {
                if (uUID2.equals(uUID) || (player2 = Bukkit.getPlayer((UUID)uUID2)) == null) continue;
                e.a((Plugin)this.a, (Entity)player2, () -> player2.showPlayer((Plugin)this.a, player));
            }
        }
        if (this.f) {
            for (UUID uUID2 : this.I) {
                player2 = Bukkit.getPlayer((UUID)uUID2);
                if (player2 == null || player2.equals((Object)player)) continue;
                player.showPlayer((Plugin)this.a, player2);
            }
        }
        this.H.remove(uUID);
    }

    void a(PacketSendEvent packetSendEvent) {
        Player player = (Player)packetSendEvent.getPlayer();
        if (player == null) {
            return;
        }
        UUID uUID2 = player.getUniqueId();
        if (!this.g.contains(uUID2)) {
            return;
        }
        if (this.i.contains(player.getWorld().getName())) {
            return;
        }
        if (!this.h.contains(player.getWorld().getEnvironment())) {
            return;
        }
        PacketTypeCommon packetTypeCommon = packetSendEvent.getPacketType();
        int n = d.a(packetSendEvent, packetTypeCommon);
        if (n < 0) {
            return;
        }
        if (n == player.getEntityId()) {
            return;
        }
        if (this.J.contains(n)) {
            return;
        }
        Set<Integer> set = this.H.get(uUID2);
        if (packetTypeCommon == PacketType.Play.Server.SPAWN_ENTITY || packetTypeCommon == PacketType.Play.Server.ENTITY_TELEPORT) {
            Double d2 = d.b(packetSendEvent, packetTypeCommon);
            if (d2 == null) {
                return;
            }
            int n2 = (int)(d2 * 4096.0);
            this.G.put(n, n2);
            if (packetTypeCommon == PacketType.Play.Server.SPAWN_ENTITY) {
                if (this.e && n2 < this.c) {
                    if (d.b(packetSendEvent)) {
                        return;
                    }
                    Set set2 = this.H.computeIfAbsent(uUID2, uUID -> d.d());
                    if (!this.g.contains(uUID2)) {
                        this.H.remove(uUID2);
                        return;
                    }
                    packetSendEvent.setCancelled(true);
                    set2.add(n);
                }
                return;
            }
            if (this.e) {
                if (n2 < this.c) {
                    if (set == null || !set.contains(n)) {
                        Set set3 = this.H.computeIfAbsent(uUID2, uUID -> d.d());
                        if (!this.g.contains(uUID2)) {
                            this.H.remove(uUID2);
                            return;
                        }
                        set3.add(n);
                        this.a(player, n);
                    }
                    packetSendEvent.setCancelled(true);
                } else if (set != null && set.contains(n)) {
                    packetSendEvent.setCancelled(true);
                    set.remove(n);
                    e.a((Plugin)this.a, (Entity)player, () -> this.a(player, Set.of(Integer.valueOf(n))));
                }
            }
        } else if (packetTypeCommon == PacketType.Play.Server.ENTITY_RELATIVE_MOVE || packetTypeCommon == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
            Integer n3 = this.G.get(n);
            Short s = d.c(packetSendEvent, packetTypeCommon);
            if (n3 != null && s != null) {
                int n4 = n3 + s;
                this.G.put(n, n4);
                if (this.e) {
                    if (n4 < this.c) {
                        if (set == null || !set.contains(n)) {
                            Set set4 = this.H.computeIfAbsent(uUID2, uUID -> d.d());
                            if (!this.g.contains(uUID2)) {
                                this.H.remove(uUID2);
                                return;
                            }
                            set4.add(n);
                            this.a(player, n);
                        }
                        packetSendEvent.setCancelled(true);
                    } else if (set != null && set.contains(n)) {
                        packetSendEvent.setCancelled(true);
                        set.remove(n);
                        e.a((Plugin)this.a, (Entity)player, () -> this.a(player, Set.of(Integer.valueOf(n))));
                    }
                }
            }
            if (set != null && set.contains(n)) {
                packetSendEvent.setCancelled(true);
            }
        } else if (set != null && set.contains(n)) {
            packetSendEvent.setCancelled(true);
        }
    }

    private static int a(PacketSendEvent packetSendEvent, Object object) {
        try {
            if (object == PacketType.Play.Server.SPAWN_ENTITY) {
                return new WrapperPlayServerSpawnEntity(packetSendEvent).getEntityId();
            }
            if (object == PacketType.Play.Server.ENTITY_TELEPORT) {
                return new WrapperPlayServerEntityTeleport(packetSendEvent).getEntityId();
            }
            if (object == PacketType.Play.Server.ENTITY_RELATIVE_MOVE) {
                return new WrapperPlayServerEntityRelativeMove(packetSendEvent).getEntityId();
            }
            if (object == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
                return new WrapperPlayServerEntityRelativeMoveAndRotation(packetSendEvent).getEntityId();
            }
            if (object == PacketType.Play.Server.ENTITY_METADATA) {
                return new WrapperPlayServerEntityMetadata(packetSendEvent).getEntityId();
            }
            if (object == PacketType.Play.Server.ENTITY_VELOCITY) {
                return new WrapperPlayServerEntityVelocity(packetSendEvent).getEntityId();
            }
            if (object == PacketType.Play.Server.ENTITY_HEAD_LOOK) {
                return new WrapperPlayServerEntityHeadLook(packetSendEvent).getEntityId();
            }
            if (object == PacketType.Play.Server.ENTITY_EQUIPMENT) {
                return new WrapperPlayServerEntityEquipment(packetSendEvent).getEntityId();
            }
            if (object == PacketType.Play.Server.ENTITY_ROTATION) {
                return new WrapperPlayServerEntityRotation(packetSendEvent).getEntityId();
            }
        }
        catch (Exception exception) {
            return -1;
        }
        return -1;
    }

    private static Double b(PacketSendEvent packetSendEvent, Object object) {
        try {
            if (object == PacketType.Play.Server.SPAWN_ENTITY) {
                WrapperPlayServerSpawnEntity wrapperPlayServerSpawnEntity = new WrapperPlayServerSpawnEntity(packetSendEvent);
                return wrapperPlayServerSpawnEntity.getPosition().getY();
            }
            if (object == PacketType.Play.Server.ENTITY_TELEPORT) {
                WrapperPlayServerEntityTeleport wrapperPlayServerEntityTeleport = new WrapperPlayServerEntityTeleport(packetSendEvent);
                return wrapperPlayServerEntityTeleport.getPosition().getY();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private static Short c(PacketSendEvent packetSendEvent, Object object) {
        try {
            if (object == PacketType.Play.Server.ENTITY_RELATIVE_MOVE) {
                WrapperPlayServerEntityRelativeMove wrapperPlayServerEntityRelativeMove = new WrapperPlayServerEntityRelativeMove(packetSendEvent);
                return (short)(wrapperPlayServerEntityRelativeMove.getDeltaY() * 4096.0);
            }
            if (object == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
                WrapperPlayServerEntityRelativeMoveAndRotation wrapperPlayServerEntityRelativeMoveAndRotation = new WrapperPlayServerEntityRelativeMoveAndRotation(packetSendEvent);
                return (short)(wrapperPlayServerEntityRelativeMoveAndRotation.getDeltaY() * 4096.0);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private static boolean b(PacketSendEvent packetSendEvent) {
        try {
            return new WrapperPlayServerSpawnEntity(packetSendEvent).getEntityType() == EntityTypes.PLAYER;
        }
        catch (Exception exception) {
            return false;
        }
    }

    void c(PacketSendEvent packetSendEvent) {
        Player player = (Player)packetSendEvent.getPlayer();
        if (player == null) {
            return;
        }
        UUID uUID = player.getUniqueId();
        Set<Integer> set = this.H.get(uUID);
        try {
            WrapperPlayServerDestroyEntities wrapperPlayServerDestroyEntities = new WrapperPlayServerDestroyEntities(packetSendEvent);
            int[] nArray = wrapperPlayServerDestroyEntities.getEntityIds();
            if (nArray == null) {
                return;
            }
            boolean bl = this.g.contains(uUID);
            for (int n : nArray) {
                this.G.remove(n);
                if (set == null || bl) continue;
                set.remove(n);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void a(Player player, int n) {
        this.a(player, List.of(Integer.valueOf(n)));
    }

    private void a(Player player, List<Integer> list) {
        try {
            int[] nArray = new int[list.size()];
            for (int i = 0; i < nArray.length; ++i) {
                nArray[i] = list.get(i);
            }
            WrapperPlayServerDestroyEntities wrapperPlayServerDestroyEntities = new WrapperPlayServerDestroyEntities(nArray);
            PacketEvents.getAPI().getPlayerManager().sendPacket((Object)player, (PacketWrapper)wrapperPlayServerDestroyEntities);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private Component a(Player player, Player player2) {
        Scoreboard scoreboard;
        String string;
        Component component;
        try {
            component = player2.playerListName();
            if (component != null && !d.a(component) && (string = j.serialize(component)) != null && !string.equals(player2.getName())) {
                return component;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        component = this.g(player2);
        if (component != null) {
            return component;
        }
        string = this.i(player2);
        if (string != null) {
            return string;
        }
        Component component2 = this.h(player2);
        if (component2 != null) {
            return component2;
        }
        Team team = this.a(player.getScoreboard(), player2);
        if (team == null) {
            try {
                scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                if (scoreboard != player.getScoreboard()) {
                    team = this.a(scoreboard, player2);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        if (team != null) {
            try {
                scoreboard = team.prefix();
                Component component3 = team.suffix();
                TextComponent textComponent = Component.text((String)player2.getName());
                try {
                    TextColor textColor = team.color();
                    if (textColor != null) {
                        textComponent = textComponent.color(textColor);
                    }
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                boolean bl = d.a((Component)scoreboard);
                boolean bl2 = d.a(component3);
                if (bl && bl2) {
                    try {
                        if (team.color() == null) {
                            return null;
                        }
                    }
                    catch (Throwable throwable) {
                        return null;
                    }
                }
                return ((TextComponent)((TextComponent)Component.empty().append((Component)scoreboard)).append((Component)textComponent)).append(component3);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return null;
    }

    private Team a(Scoreboard scoreboard, Player player) {
        if (scoreboard == null) {
            return null;
        }
        try {
            return scoreboard.getEntryTeam(player.getName());
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    private static boolean a(Component component) {
        if (component == null) {
            return true;
        }
        if (component == Component.empty()) {
            return true;
        }
        try {
            String string = PlainTextComponentSerializer.plainText().serialize(component);
            return string == null || string.isEmpty();
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void f() {
        if (this.p) {
            return;
        }
        d d2 = this;
        synchronized (d2) {
            if (this.p) {
                return;
            }
            try {
                if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                    Class<?> clazz = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                    this.r = clazz.getMethod("setPlaceholders", OfflinePlayer.class, String.class);
                    this.q = true;
                }
            }
            catch (Throwable throwable) {
                this.q = false;
            }
            finally {
                this.p = true;
            }
        }
    }

    private Component g(Player player) {
        if (this.n == null || this.n.isEmpty()) {
            return null;
        }
        this.f();
        if (!this.q || this.r == null) {
            return null;
        }
        for (String string : this.n) {
            String string2;
            Object object;
            if (string == null || string.isEmpty()) continue;
            try {
                object = this.r.invoke(null, player, string);
                if (!(object instanceof String)) continue;
                string2 = (String)object;
            }
            catch (Throwable throwable) {
                continue;
            }
            if (string2 == null || string2.isEmpty() || string2.indexOf(37) >= 0) continue;
            try {
                object = this.c(string2);
            }
            catch (Throwable throwable) {
                continue;
            }
            String string3 = PlainTextComponentSerializer.plainText().serialize((Component)object);
            if (string3 == null || string3.isEmpty() || string3.equals(player.getName())) continue;
            return object;
        }
        return null;
    }

    private Component a(String string) {
        return j.deserialize(ChatColor.translateAlternateColorCodes((char)'&', (String)d.b(string)));
    }

    private static String b(String string) {
        if (string == null || string.indexOf(35) < 0) {
            return string;
        }
        Matcher matcher = k.matcher(string);
        StringBuilder stringBuilder = new StringBuilder(string.length() + 16);
        while (matcher.find()) {
            String string2 = matcher.group(1);
            StringBuilder stringBuilder2 = new StringBuilder("\u00a7x");
            for (int i = 0; i < string2.length(); ++i) {
                stringBuilder2.append('\u00a7').append(string2.charAt(i));
            }
            matcher.appendReplacement(stringBuilder, Matcher.quoteReplacement(stringBuilder2.toString()));
        }
        matcher.appendTail(stringBuilder);
        return stringBuilder.toString();
    }

    private Component c(String string) {
        if (string == null || string.isEmpty()) {
            return Component.empty();
        }
        if (m.matcher(string).find()) {
            try {
                return l.deserialize((Object)string);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return this.a(string);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void g() {
        if (this.s) {
            return;
        }
        d d2 = this;
        synchronized (d2) {
            if (this.s) {
                return;
            }
            try {
                if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                    Class<?> clazz = Class.forName("net.milkbowl.vault.chat.Chat");
                    RegisteredServiceProvider registeredServiceProvider = Bukkit.getServicesManager().getRegistration(clazz);
                    if (registeredServiceProvider != null && registeredServiceProvider.getProvider() != null) {
                        this.u = registeredServiceProvider.getProvider();
                        this.v = clazz.getMethod("getPlayerPrefix", Player.class);
                        this.w = clazz.getMethod("getPlayerSuffix", Player.class);
                        this.t = true;
                    }
                }
            }
            catch (Throwable throwable) {
                this.t = false;
            }
            finally {
                this.s = true;
            }
        }
    }

    private Component h(Player player) {
        boolean bl;
        Object object;
        if (!this.o) {
            return null;
        }
        this.g();
        if (!this.t || this.v == null) {
            return null;
        }
        String string = "";
        String string2 = "";
        try {
            object = this.v.invoke(this.u, player);
            if (object instanceof String) {
                string = (String)object;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            object = this.w.invoke(this.u, player);
            if (object instanceof String) {
                string2 = (String)object;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        boolean bl2 = string != null && !string.isEmpty();
        boolean bl3 = bl = string2 != null && !string2.isEmpty();
        if (!bl2 && !bl) {
            return null;
        }
        String string3 = bl2 ? string : "";
        String string4 = bl ? string2 : "";
        return this.c(string3 + player.getName() + string4);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void h() {
        if (this.x) {
            return;
        }
        d d2 = this;
        synchronized (d2) {
            if (this.x) {
                return;
            }
            try {
                if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
                    Class<?> clazz = Class.forName("net.luckperms.api.LuckPerms");
                    RegisteredServiceProvider registeredServiceProvider = Bukkit.getServicesManager().getRegistration(clazz);
                    if (registeredServiceProvider != null && registeredServiceProvider.getProvider() != null) {
                        this.z = registeredServiceProvider.getProvider();
                        this.A = clazz.getMethod("getUserManager", new Class[0]);
                        Class<?> clazz2 = Class.forName("net.luckperms.api.model.user.UserManager");
                        this.B = clazz2.getMethod("getUser", UUID.class);
                        Class<?> clazz3 = Class.forName("net.luckperms.api.model.user.User");
                        this.C = clazz3.getMethod("getCachedData", new Class[0]);
                        Class<?> clazz4 = Class.forName("net.luckperms.api.cacheddata.CachedDataManager");
                        this.D = clazz4.getMethod("getMetaData", new Class[0]);
                        Class<?> clazz5 = Class.forName("net.luckperms.api.cacheddata.CachedMetaData");
                        this.E = clazz5.getMethod("getPrefix", new Class[0]);
                        this.F = clazz5.getMethod("getSuffix", new Class[0]);
                        this.y = true;
                    }
                }
            }
            catch (Throwable throwable) {
                this.y = false;
            }
            finally {
                this.x = true;
            }
        }
    }

    private Component i(Player player) {
        this.h();
        if (!this.y || this.z == null) {
            return null;
        }
        try {
            boolean bl;
            Object object = this.A.invoke(this.z, new Object[0]);
            Object object2 = this.B.invoke(object, player.getUniqueId());
            if (object2 == null) {
                return null;
            }
            Object object3 = this.C.invoke(object2, new Object[0]);
            Object object4 = this.D.invoke(object3, new Object[0]);
            Object object5 = this.E.invoke(object4, new Object[0]);
            Object object6 = this.F.invoke(object4, new Object[0]);
            String string = object5 instanceof String ? (String)object5 : "";
            String string2 = object6 instanceof String ? (String)object6 : "";
            boolean bl2 = !string.isEmpty();
            boolean bl3 = bl = !string2.isEmpty();
            if (!bl2 && !bl) {
                return null;
            }
            return this.c(string + player.getName() + string2);
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    private void b(Player player, Player player2) {
        try {
            ProfileProperty profileProperty2;
            GameMode gameMode;
            UserProfile userProfile = new UserProfile(player2.getUniqueId(), player2.getName());
            try {
                PlayerProfile playerProfile = player2.getPlayerProfile();
                if (playerProfile != null) {
                    gameMode = new ArrayList(playerProfile.getProperties().size());
                    for (ProfileProperty profileProperty2 : playerProfile.getProperties()) {
                        gameMode.add(new TextureProperty(profileProperty2.getName(), profileProperty2.getValue(), profileProperty2.getSignature()));
                    }
                    userProfile.setTextureProperties((List)gameMode);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            int n = Math.max(0, player2.getPing());
            gameMode = GameMode.valueOf((String)player2.getGameMode().name());
            Component component = this.a(player, player2);
            profileProperty2 = new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(userProfile, true, n, gameMode, component, null);
            profileProperty2.setListOrder(this.j(player2));
            EnumSet<WrapperPlayServerPlayerInfoUpdate.Action> enumSet = EnumSet.of(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER, WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED, WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LATENCY, WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_GAME_MODE, WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LIST_ORDER);
            if (component != null) {
                enumSet.add(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME);
            }
            WrapperPlayServerPlayerInfoUpdate wrapperPlayServerPlayerInfoUpdate = new WrapperPlayServerPlayerInfoUpdate(enumSet, new WrapperPlayServerPlayerInfoUpdate.PlayerInfo[]{profileProperty2});
            PacketEvents.getAPI().getPlayerManager().sendPacket((Object)player, (PacketWrapper)wrapperPlayServerPlayerInfoUpdate);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int j(Player player) {
        Object object;
        if (!this.M) {
            object = this;
            synchronized (object) {
                if (!this.M) {
                    try {
                        this.N = Player.class.getMethod("getListOrder", new Class[0]);
                    }
                    catch (Throwable throwable) {
                        this.N = null;
                    }
                    this.M = true;
                }
            }
        }
        if (this.N == null) {
            return 0;
        }
        try {
            object = this.N.invoke((Object)player, new Object[0]);
            return object instanceof Integer ? (Integer)object : 0;
        }
        catch (Throwable throwable) {
            return 0;
        }
    }

    private void a(Player player, Set<Integer> set) {
        double d2 = Math.min(16.0 * (double)Bukkit.getViewDistance(), 128.0);
        double d3 = player.getLocation().getY() - (double)player.getWorld().getMinHeight();
        ArrayList<Entity> arrayList = new ArrayList<Entity>();
        for (Entity entity : player.getNearbyEntities(d2, d3, d2)) {
            if (entity instanceof Player || !set.contains(entity.getEntityId()) || entity.isDead() || !entity.isValid()) continue;
            arrayList.add(entity);
        }
        if (arrayList.isEmpty()) {
            return;
        }
        if (e.a()) {
            for (Entity entity : arrayList) {
                e.a((Plugin)this.a, entity, () -> {
                    if (!entity.isValid() || entity.isDead()) {
                        return;
                    }
                    if (!player.isOnline()) {
                        return;
                    }
                    try {
                        player.hideEntity((Plugin)this.a, entity);
                        player.showEntity((Plugin)this.a, entity);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                });
            }
        } else {
            Bukkit.getScheduler().runTask((Plugin)this.a, () -> {
                if (!player.isOnline()) {
                    return;
                }
                for (Entity entity : arrayList) {
                    if (!entity.isValid() || entity.isDead()) continue;
                    try {
                        player.hideEntity((Plugin)this.a, entity);
                        player.showEntity((Plugin)this.a, entity);
                    }
                    catch (Exception exception) {}
                }
            });
        }
    }

    private void k(Player player) {
        double d2 = Math.min(16.0 * (double)Bukkit.getViewDistance(), 128.0);
        Location location = player.getLocation();
        double d3 = location.getY() - (double)player.getWorld().getMinHeight();
        ArrayList<Entity> arrayList = new ArrayList<Entity>();
        for (Entity entity : player.getNearbyEntities(d2, d3, d2)) {
            if (entity instanceof Player || entity.isDead() || !entity.isValid() || entity.getLocation().getY() >= (double)this.b) continue;
            arrayList.add(entity);
        }
        if (arrayList.isEmpty()) {
            return;
        }
        if (e.a()) {
            for (Entity entity : arrayList) {
                e.a((Plugin)this.a, entity, () -> {
                    if (!entity.isValid() || entity.isDead()) {
                        return;
                    }
                    if (!player.isOnline()) {
                        return;
                    }
                    try {
                        player.hideEntity((Plugin)this.a, entity);
                        player.showEntity((Plugin)this.a, entity);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                });
            }
        } else {
            Bukkit.getScheduler().runTask((Plugin)this.a, () -> {
                if (!player.isOnline()) {
                    return;
                }
                for (Entity entity : arrayList) {
                    if (!entity.isValid() || entity.isDead()) continue;
                    try {
                        player.hideEntity((Plugin)this.a, entity);
                        player.showEntity((Plugin)this.a, entity);
                    }
                    catch (Exception exception) {}
                }
            });
        }
    }
}

