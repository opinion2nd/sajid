/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.retrooper.packetevents.PacketEvents
 *  com.github.retrooper.packetevents.event.PacketListenerAbstract
 *  com.github.retrooper.packetevents.event.PacketListenerCommon
 *  com.github.retrooper.packetevents.event.PacketListenerPriority
 *  com.github.retrooper.packetevents.event.PacketSendEvent
 *  com.github.retrooper.packetevents.protocol.packettype.PacketType$Play$Server
 *  com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon
 *  com.github.retrooper.packetevents.protocol.world.blockentity.BlockEntityType
 *  com.github.retrooper.packetevents.protocol.world.blockentity.BlockEntityTypes
 *  com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk
 *  com.github.retrooper.packetevents.protocol.world.chunk.Column
 *  com.github.retrooper.packetevents.protocol.world.chunk.LightData
 *  com.github.retrooper.packetevents.protocol.world.chunk.TileEntity
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockAction
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockEntityData
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame
 *  com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRespawn
 *  org.bukkit.Bukkit
 *  org.bukkit.ChunkSnapshot
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.World
 *  org.bukkit.World$Environment
 *  org.bukkit.block.Block
 *  org.bukkit.block.data.BlockData
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.HandlerList
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.block.BlockBreakEvent
 *  org.bukkit.event.player.PlayerChangedWorldEvent
 *  org.bukkit.event.player.PlayerGameModeChangeEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.player.PlayerLoginEvent
 *  org.bukkit.event.player.PlayerLoginEvent$Result
 *  org.bukkit.event.player.PlayerMoveEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.event.player.PlayerRespawnEvent
 *  org.bukkit.event.player.PlayerTeleportEvent
 *  org.bukkit.plugin.Plugin
 */
import com.anticheat.antiesp.AntiESPFreecamPlugin;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.world.blockentity.BlockEntityType;
import com.github.retrooper.packetevents.protocol.world.blockentity.BlockEntityTypes;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.chunk.LightData;
import com.github.retrooper.packetevents.protocol.world.chunk.TileEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockAction;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockEntityData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRespawn;
import java.lang.constant.Constable;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.runtime.ObjectMethods;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

public class b
implements Listener {
    private final AntiESPFreecamPlugin a;
    private final int b;
    private final int c;
    private final int d;
    private final Material e;
    private final BlockData f;
    private final boolean g;
    private final int h;
    private final Set<World.Environment> i;
    private final Set<String> j;
    volatile boolean k;
    private final int l;
    private final int m;
    private final boolean n;
    private final boolean o;
    private final int p;
    private final int q;
    private final boolean r;
    private final int s;
    static final long t = new SecureRandom().nextLong();
    final boolean u;
    private volatile Method v;
    private volatile boolean w;
    private volatile boolean[] x;
    private volatile Set<Material> y;
    private volatile Set<Material> z;
    private int A;
    private int B;
    private int C;
    private volatile int[] D;
    private String E;
    private String F;
    private int G = 0;
    final Set<UUID> H = ConcurrentHashMap.newKeySet();
    final Set<UUID> I = ConcurrentHashMap.newKeySet();
    private final Set<UUID> J = ConcurrentHashMap.newKeySet();
    private final boolean K;
    private d L;
    private final Map<UUID, Queue<a>> M = new ConcurrentHashMap<UUID, Queue<a>>();
    private Object N;
    private Object O;
    private final Map<UUID, Integer> P = new ConcurrentHashMap<UUID, Integer>();
    private final Set<UUID> Q = ConcurrentHashMap.newKeySet();
    private final Location R = new Location(null, 0.0, 0.0, 0.0);
    private final Map<UUID, Set<Long>> S = new ConcurrentHashMap<UUID, Set<Long>>();
    private final Map<UUID, long[]> T = new ConcurrentHashMap<UUID, long[]>();
    private final int U;
    private final int V;
    private final int W;
    private final int X;
    private final int Y;
    private final int Z;
    private final Map<UUID, int[]> aa = new ConcurrentHashMap<UUID, int[]>();
    final Map<UUID, long[]> ab = new ConcurrentHashMap<UUID, long[]>();
    private final Map<UUID, Set<Long>> ac = new ConcurrentHashMap<UUID, Set<Long>>();
    private final BlockData ad = Material.STONE.createBlockData();
    private final BlockData ae = Material.DEEPSLATE.createBlockData();
    private final BlockData af = Material.NETHERRACK.createBlockData();
    private final Map<UUID, long[]> ag = new ConcurrentHashMap<UUID, long[]>();
    private Object ah;
    private final List<UUID> ai = new ArrayList<UUID>();
    private int aj = 0;
    private int ak = 0;
    private boolean al;
    private volatile boolean am = false;
    private volatile boolean an = true;
    private volatile Field ao;
    private volatile Field ap;
    private volatile Field aq;
    private volatile Method ar;
    private volatile Object as;
    private volatile Object at;
    private volatile Class<?> au;
    private int av;
    private PacketListenerAbstract aw;
    private final ExecutorService[] ax = b.g();
    private final boolean ay;
    private Method az;
    private Method aA;
    private Method aB;
    private Method aC;
    private Constructor<?> aD;
    private Field aE;
    private Method aF;
    private boolean aG = false;
    private boolean aH = false;
    private static Field aI;
    private volatile boolean aJ = false;
    private static final int[][] aK;
    private volatile Object[] aL;
    private final ThreadLocal<Object[]> aM = ThreadLocal.withInitial(() -> {
        Object[] objectArray = this.aL;
        return objectArray == null ? null : (Object[])objectArray.clone();
    });
    private static final Set<Material> aN;

    private static Set<Long> f() {
        return Collections.synchronizedSet(new LinkedHashSet<Long>(){

            public boolean a(Long l) {
                Iterator iterator;
                if (this.contains(l)) {
                    return false;
                }
                if (this.size() >= 8192 && (iterator = this.iterator()).hasNext()) {
                    iterator.next();
                    iterator.remove();
                }
                return super.add(l);
            }

            @Override
            public /* synthetic */ boolean add(Object object) {
                return this.a((Long)object);
            }
        });
    }

    private static ExecutorService[] g() {
        ExecutorService[] executorServiceArray = new ExecutorService[4];
        for (int i = 0; i < 4; ++i) {
            int n = i;
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(2048), runnable -> {
                Thread thread = new Thread(runnable, "AntiESP-ChunkSend-" + n);
                thread.setDaemon(true);
                return thread;
            }, new ThreadPoolExecutor.DiscardOldestPolicy());
            executorServiceArray[i] = threadPoolExecutor;
        }
        return executorServiceArray;
    }

    public b(AntiESPFreecamPlugin antiESPFreecamPlugin, int n, int n2, int n3, Material material, boolean bl, int n4, Set<World.Environment> set, boolean bl2, int n5, int n6, int n7, boolean bl3, int n8, List<String> list, Set<String> set2, int n9, int n10, int n11, int n12, String string, String string2, boolean bl4) {
        StringBuilder stringBuilder;
        Object object;
        Material material2;
        this.a = antiESPFreecamPlugin;
        this.b = n;
        this.c = n2;
        this.d = Math.max(0, n3);
        this.e = material;
        this.f = material.createBlockData();
        this.g = bl;
        this.h = Math.max(1, n4);
        this.U = Math.max(16, n9);
        this.V = Math.max(1, n11) * Math.max(1, n11);
        this.W = Math.max(2, (this.U >> 4) + 1);
        this.X = Math.max(16, n10);
        this.Y = Math.max(1, n12) * Math.max(1, n12);
        this.Z = Math.max(2, (this.X >> 4) + 1);
        this.i = set;
        this.j = set2;
        this.K = bl4;
        this.G = this.a(material);
        this.al = this.G == 0;
        this.av = Bukkit.getViewDistance();
        this.ay = e.e() || e.c();
        this.k = bl2;
        this.l = n5;
        int n13 = antiESPFreecamPlugin.getConfig().getInt("antiXray.netherBelowY", -1);
        this.m = n13 >= 0 ? n13 : n5;
        this.n = antiESPFreecamPlugin.getConfig().getBoolean("antiXray.netherEnabled", true);
        this.o = antiESPFreecamPlugin.getConfig().getBoolean("antiXray.fullMaskSurface", false);
        this.p = Math.max(1, n6);
        this.q = Math.max(n6, n7);
        this.r = bl3;
        if (n8 > 0 && n8 <= n2) {
            antiESPFreecamPlugin.getLogger().warning("[AntiESP] skybaseAboveY (" + n8 + ") must be > revealBelowYWhenUnder (" + n2 + ") \u2014 skybase masking disabled.");
            this.s = 0;
        } else {
            this.s = Math.max(0, n8);
        }
        this.u = antiESPFreecamPlugin.getConfig().getBoolean("antiSeedCracker.hideSeed", true);
        Method method = null;
        try {
            method = Player.class.getMethod("refreshChunk", Integer.TYPE, Integer.TYPE);
        }
        catch (NoSuchMethodException noSuchMethodException) {
            for (Method object2 : Player.class.getMethods()) {
                if (object2.getParameterCount() != 2 || (material2 = object2.getParameterTypes())[0] != Integer.TYPE || material2[1] != Integer.TYPE || !((String)(object = object2.getName().toLowerCase())).contains("refreshchunk") && !((String)object).contains("resendchunk") && !((String)object).contains("sendchunk") && !((String)object).contains("requestchunk")) continue;
                method = object2;
            }
        }
        catch (Throwable throwable) {
            antiESPFreecamPlugin.getLogger().warning("[AntiESP] Unexpected error probing Paper API: " + String.valueOf(throwable));
        }
        this.v = method;
        boolean bl5 = this.w = method != null;
        if (this.w) {
            antiESPFreecamPlugin.getLogger().info("[AntiESP] Paper API " + this.v.getName() + "(int,int) available \u2014 will fall back to it if NMS reflection breaks.");
        } else {
            stringBuilder = new StringBuilder();
            for (Method n16 : Player.class.getMethods()) {
                if (n16.getParameterCount() != 2 || n16.getParameterTypes()[0] != Integer.TYPE || n16.getParameterTypes()[1] != Integer.TYPE) continue;
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(n16.getName());
            }
            antiESPFreecamPlugin.getLogger().info("[AntiESP] No Paper-API chunk-refresh method found on Player. Available (int,int) methods: [" + String.valueOf(stringBuilder.length() == 0 ? "none" : stringBuilder) + "]. Falling back to NMS reflection + SMBC.");
        }
        this.A = this.a(Material.STONE);
        stringBuilder = Material.matchMaterial((String)"DEEPSLATE");
        this.B = stringBuilder != null ? this.a((Material)stringBuilder) : this.A;
        this.C = this.a(Material.NETHERRACK);
        if (bl2) {
            int n14 = 0;
            ArrayList arrayList = new ArrayList();
            for (String string3 : list) {
                material2 = Material.matchMaterial((String)string3.trim());
                if (material2 == null || (object = this.b(material2)).isEmpty()) continue;
                arrayList.addAll(object);
            }
            Iterator<String> iterator = arrayList.iterator();
            while (iterator.hasNext()) {
                int n15 = (Integer)((Object)iterator.next());
                if (n15 <= n14) continue;
                n14 = n15;
            }
            this.x = new boolean[n14 + 1];
            iterator = arrayList.iterator();
            while (iterator.hasNext()) {
                int n16 = (Integer)((Object)iterator.next());
                this.x[n16] = true;
            }
            this.y = EnumSet.noneOf(Material.class);
            for (String string4 : list) {
                material2 = Material.matchMaterial((String)string4.trim());
                if (material2 == null) continue;
                this.y.add(material2);
            }
            this.z = b.a(this.y);
            this.E = string;
            this.F = string2;
            this.D = this.a(this.x, Material.LAVA, string, Material.WATER, string2);
        } else {
            this.x = new boolean[0];
            this.D = new int[0];
            this.E = string;
            this.F = string2;
            this.y = EnumSet.noneOf(Material.class);
            this.z = EnumSet.noneOf(Material.class);
        }
    }

    public Set<UUID> a() {
        return this.H;
    }

    public boolean b() {
        return this.k;
    }

    public Set<Material> c() {
        return this.y;
    }

    public void a(boolean bl) {
        boolean bl2 = this.k;
        this.k = bl;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (e.a()) {
                e.a((Plugin)this.a, (Entity)player, () -> this.m(player));
                continue;
            }
            this.m(player);
        }
        if (bl && !bl2) {
            if (this.ah == null) {
                this.ah = e.a((Plugin)this.a, this::j, 20L, 5L);
            }
        } else if (!bl && bl2 && this.ah != null) {
            e.a(this.ah);
            this.ah = null;
        }
    }

    public void a(List<String> list) {
        int n = 0;
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        EnumSet<Material> enumSet = EnumSet.noneOf(Material.class);
        for (String string : list) {
            Material material = Material.matchMaterial((String)string.trim());
            if (material == null) continue;
            enumSet.add(material);
            List<Integer> list2 = this.b(material);
            arrayList.addAll(list2);
        }
        Object object = arrayList.iterator();
        while (object.hasNext()) {
            int n2 = (Integer)((Object)object.next());
            if (n2 <= n) continue;
            n = n2;
        }
        object = new boolean[n + 1];
        Iterator iterator = arrayList.iterator();
        while (iterator.hasNext()) {
            int n3 = (Integer)iterator.next();
            object[n3] = true;
        }
        this.x = (boolean[])object;
        this.D = this.a((boolean[])object, Material.LAVA, this.E, Material.WATER, this.F);
        this.y = enumSet;
        this.z = b.a(enumSet);
    }

    public void a(d d2) {
        this.L = d2;
    }

    public void a(Player player) {
        UUID uUID = player.getUniqueId();
        this.I.add(uUID);
        if (this.H.remove(uUID)) {
            this.M.remove(uUID);
            this.k(player);
            if (this.L != null) {
                this.L.c(player);
            }
        }
    }

    public void b(Player player) {
        UUID uUID = player.getUniqueId();
        this.I.remove(uUID);
        if (!this.a(player.getWorld())) {
            return;
        }
        if (player.getLocation().getBlockY() >= this.c) {
            this.H.add(uUID);
            if (this.K || !this.J.contains(uUID)) {
                this.b(player, true);
            }
            if (this.L != null) {
                this.L.b(player);
            }
        }
    }

    public boolean c(Player player) {
        return this.I.contains(player.getUniqueId());
    }

    public void d() {
        this.aw = new PacketListenerAbstract(PacketListenerPriority.NORMAL){

            public void onPacketSend(PacketSendEvent packetSendEvent) {
                PacketTypeCommon packetTypeCommon = packetSendEvent.getPacketType();
                if (packetTypeCommon == PacketType.Play.Server.RESPAWN) {
                    Player player = (Player)packetSendEvent.getPlayer();
                    if (player != null) {
                        b.this.ab.remove(player.getUniqueId());
                    }
                    if (b.this.u) {
                        try {
                            WrapperPlayServerRespawn wrapperPlayServerRespawn = new WrapperPlayServerRespawn(packetSendEvent);
                            wrapperPlayServerRespawn.setHashedSeed(t);
                            packetSendEvent.markForReEncode(true);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    return;
                }
                if (packetTypeCommon == PacketType.Play.Server.JOIN_GAME) {
                    if (b.this.u) {
                        try {
                            WrapperPlayServerJoinGame wrapperPlayServerJoinGame = new WrapperPlayServerJoinGame(packetSendEvent);
                            wrapperPlayServerJoinGame.setHashedSeed(t);
                            packetSendEvent.markForReEncode(true);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    return;
                }
                if (packetTypeCommon == PacketType.Play.Server.BLOCK_ENTITY_DATA || packetTypeCommon == PacketType.Play.Server.BLOCK_ACTION) {
                    b.this.a(packetSendEvent, packetTypeCommon == PacketType.Play.Server.BLOCK_ENTITY_DATA);
                    return;
                }
                if (packetTypeCommon != PacketType.Play.Server.CHUNK_DATA) {
                    return;
                }
                Player player = (Player)packetSendEvent.getPlayer();
                if (player == null) {
                    return;
                }
                UUID uUID = player.getUniqueId();
                if (b.this.I.contains(uUID)) {
                    return;
                }
                if (b.this.H.contains(uUID)) {
                    b.this.a(packetSendEvent);
                } else if (b.this.k) {
                    b.this.b(packetSendEvent);
                }
            }
        };
        PacketEvents.getAPI().getEventManager().registerListener((PacketListenerCommon)this.aw);
        if (this.k) {
            this.ah = e.a((Plugin)this.a, this::j, 20L, 5L);
        }
        if (e.a() || e.d()) {
            this.O = e.a((Plugin)this.a, this::k, 10L, 10L);
        }
        e.a((Plugin)this.a, this::i, 200L, 200L);
        e.a((Plugin)this.a, this::h, 1200L, 1200L);
    }

    private void h() {
        Predicate<UUID> predicate = uUID -> Bukkit.getPlayer((UUID)uUID) == null;
        this.H.removeIf(predicate);
        this.J.removeIf(predicate);
        this.Q.removeIf(predicate);
        this.I.removeIf(predicate);
        this.M.keySet().removeIf(predicate);
        this.P.keySet().removeIf(predicate);
        this.S.keySet().removeIf(predicate);
        this.T.keySet().removeIf(predicate);
        this.aa.keySet().removeIf(predicate);
        this.ab.keySet().removeIf(predicate);
        this.ac.keySet().removeIf(predicate);
        this.ag.keySet().removeIf(predicate);
    }

    private void i() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Set<Long> set;
            UUID uUID = player.getUniqueId();
            if (this.H.contains(uUID) || this.I.contains(uUID) || !this.J.contains(uUID) || !this.a(player.getWorld()) || (set = this.S.get(uUID)) == null || set.isEmpty()) continue;
            if (this.k) {
                this.ab.remove(uUID);
            }
            boolean bl = player.isGliding() || player.getGameMode() == GameMode.SPECTATOR;
            this.a(player, bl);
        }
    }

    public void e() {
        if (this.N != null) {
            e.a(this.N);
            this.N = null;
        }
        if (this.O != null) {
            e.a(this.O);
            this.O = null;
        }
        if (this.ah != null) {
            e.a(this.ah);
            this.ah = null;
        }
        this.M.clear();
        this.P.clear();
        this.Q.clear();
        this.S.clear();
        this.T.clear();
        this.J.clear();
        if (this.aw != null) {
            PacketEvents.getAPI().getEventManager().unregisterListener((PacketListenerCommon)this.aw);
            this.aw = null;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!this.H.contains(player.getUniqueId())) continue;
            this.k(player);
        }
        for (ExecutorService executorService : this.ax) {
            executorService.shutdown();
        }
        for (ExecutorService executorService : this.ax) {
            try {
                executorService.awaitTermination(2L, TimeUnit.SECONDS);
            }
            catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        if (this.L != null) {
            this.L.b();
        }
        this.H.clear();
        HandlerList.unregisterAll((Listener)this);
    }

    void a(PacketSendEvent packetSendEvent) {
        try {
            int n;
            int n2;
            BaseChunk baseChunk;
            int n3;
            int n4;
            int n5;
            int n6;
            World world;
            boolean bl;
            Player player = (Player)packetSendEvent.getPlayer();
            int[] nArray = this.aa.get(player.getUniqueId());
            if (nArray == null) {
                return;
            }
            int n7 = nArray[0];
            int n8 = nArray[1];
            int n9 = nArray.length >= 3 ? nArray[2] : 0;
            int n10 = nArray.length >= 4 ? nArray[3] : 0;
            boolean bl2 = nArray.length >= 5 && nArray[4] == World.Environment.NETHER.ordinal();
            int n11 = nArray.length >= 6 ? nArray[5] : -1;
            int n12 = nArray.length >= 7 ? nArray[6] : 0;
            Integer n13 = this.P.get(player.getUniqueId());
            int n14 = n13 != null ? n13.intValue() : player.getLocation().getBlockY();
            boolean bl3 = n11 >= 0 && n14 < this.s;
            boolean bl4 = bl = this.k && n9 > 0;
            if (n8 <= 0 && n12 <= 0 && !bl3 && !bl) {
                return;
            }
            if (n8 > n7) {
                return;
            }
            WrapperPlayServerChunkData wrapperPlayServerChunkData = new WrapperPlayServerChunkData(packetSendEvent);
            Column column = wrapperPlayServerChunkData.getColumn();
            if (column == null) {
                return;
            }
            BaseChunk[] baseChunkArray = column.getChunks();
            if (baseChunkArray == null || baseChunkArray.length == 0) {
                return;
            }
            if (baseChunkArray.length != n7 && ((world = player.getWorld()).getEnvironment().ordinal() != nArray[4] || world.getMinHeight() != n10)) {
                return;
            }
            int n15 = Math.min(n8, baseChunkArray.length);
            boolean bl5 = false;
            for (n6 = 0; n6 < n15; ++n6) {
                BaseChunk baseChunk2 = baseChunkArray[n6];
                if (baseChunk2 == null) continue;
                bl5 = true;
                if (this.a(baseChunk2)) continue;
                for (n5 = 0; n5 < 16; ++n5) {
                    for (n4 = 0; n4 < 16; ++n4) {
                        for (n3 = 0; n3 < 16; ++n3) {
                            baseChunk2.set(n5, n4, n3, this.G);
                        }
                    }
                }
            }
            if (n12 > 0 && n15 < baseChunkArray.length && (baseChunk = baseChunkArray[n15]) != null) {
                bl5 = true;
                for (int i = 0; i < 16; ++i) {
                    for (n5 = 0; n5 < n12; ++n5) {
                        for (n4 = 0; n4 < 16; ++n4) {
                            baseChunk.set(i, n5, n4, this.G);
                        }
                    }
                }
            }
            n6 = n15;
            if (this.k && n9 > n15) {
                int n16;
                if (this.o) {
                    n5 = (n14 - n10) / 16;
                    n16 = Math.min(n9, n5);
                } else {
                    n5 = (int)Math.ceil((double)(this.c - n10) / 16.0);
                    n16 = Math.min(n9, Math.max(n15, n5));
                }
                if (n16 > n15) {
                    BaseChunk baseChunk3;
                    if (n12 > 0 && n15 < baseChunkArray.length && (baseChunk3 = baseChunkArray[n15]) != null) {
                        if (this.o) {
                            for (n4 = 0; n4 < 16; ++n4) {
                                for (n3 = n12; n3 < 16; ++n3) {
                                    for (n2 = 0; n2 < 16; ++n2) {
                                        baseChunk3.set(n4, n3, n2, this.G);
                                    }
                                }
                            }
                            bl5 = true;
                        } else if (this.a(baseChunk3, n12, 16, this.a(n10 + n15 * 16, bl2))) {
                            bl5 = true;
                        }
                    }
                    n5 = n15 + (n12 > 0 ? 1 : 0);
                    n4 = (this.o ? this.a(baseChunkArray, n5, n16) : this.a(baseChunkArray, n5, n16, n10, bl2)) ? 1 : 0;
                    if (n4 != 0) {
                        bl5 = true;
                    }
                    if (this.o) {
                        n6 = n16;
                    }
                }
            }
            int n17 = n = nArray.length >= 8 ? nArray[7] : 0;
            if (bl3 && n11 >= 0 && n11 < baseChunkArray.length) {
                BaseChunk baseChunk4;
                if (n > 0 && (baseChunk4 = baseChunkArray[n11]) != null) {
                    bl5 = true;
                    for (n4 = 0; n4 < 16; ++n4) {
                        for (n3 = n; n3 < 16; ++n3) {
                            for (n2 = 0; n2 < 16; ++n2) {
                                baseChunk4.set(n4, n3, n2, 0);
                            }
                        }
                    }
                }
                for (n4 = n5 = n > 0 ? n11 + 1 : n11; n4 < baseChunkArray.length; ++n4) {
                    BaseChunk baseChunk5 = baseChunkArray[n4];
                    if (baseChunk5 == null) continue;
                    bl5 = true;
                    if (this.b(baseChunk5)) continue;
                    for (n2 = 0; n2 < 16; ++n2) {
                        for (int i = 0; i < 16; ++i) {
                            for (int j = 0; j < 16; ++j) {
                                baseChunk5.set(n2, i, j, 0);
                            }
                        }
                    }
                }
            }
            n5 = n8 > 0 || n12 > 0 || n6 > n15 ? 1 : 0;
            int n18 = n4 = n6 > n15 ? n10 + n6 * 16 : this.b;
            if ((n5 != 0 || bl3) && this.a(column, n5 != 0, n4, bl3, this.s)) {
                bl5 = true;
            }
            LightData lightData = wrapperPlayServerChunkData.getLightData();
            if (bl5 && lightData != null) {
                byte[][] byArray = lightData.getSkyLightArray();
                byte[][] byArray2 = lightData.getBlockLightArray();
                BitSet bitSet = lightData.getSkyLightMask();
                BitSet bitSet2 = lightData.getBlockLightMask();
                int n19 = Math.max(n15, n6);
                for (int i = 0; i < n19; ++i) {
                    int n20;
                    int n21 = i + 1;
                    if (bitSet != null && byArray != null && bitSet.get(n21) && (n20 = bitSet.get(0, n21).cardinality()) < byArray.length) {
                        byArray[n20] = new byte[2048];
                    }
                    if (bitSet2 == null || byArray2 == null || !bitSet2.get(n21) || (n20 = bitSet2.get(0, n21).cardinality()) >= byArray2.length) continue;
                    byArray2[n20] = new byte[2048];
                }
            }
            if (bl5) {
                packetSendEvent.markForReEncode(true);
            }
        }
        catch (Exception exception) {
            this.a.getLogger().warning("[AntiESP] Exception in chunk packet handler: " + String.valueOf(exception));
        }
    }

    private boolean a(Column column, boolean bl, int n, boolean bl2, int n2) {
        if (this.aJ) {
            return false;
        }
        try {
            Object object;
            TileEntity[] tileEntityArray = column.getTileEntities();
            if (tileEntityArray == null || tileEntityArray.length == 0) {
                return false;
            }
            TileEntity[] tileEntityArray2 = new TileEntity[tileEntityArray.length];
            int n3 = 0;
            for (TileEntity tileEntity : tileEntityArray) {
                if (tileEntity != null) {
                    int n4 = tileEntity.getY();
                    if (bl && n4 < n || bl2 && n4 >= n2) continue;
                }
                tileEntityArray2[n3++] = tileEntity;
            }
            if (n3 == tileEntityArray.length) {
                return false;
            }
            if (aI == null) {
                object = Column.class.getDeclaredField("tileEntities");
                ((Field)object).setAccessible(true);
                aI = object;
            }
            object = new TileEntity[n3];
            System.arraycopy(tileEntityArray2, 0, object, 0, n3);
            aI.set(column, object);
            return true;
        }
        catch (Throwable throwable) {
            this.aJ = true;
            this.a.getLogger().warning("[AntiESP] Tile-entity strip unavailable (chest-ESP packet hardening off): " + String.valueOf(throwable));
            return false;
        }
    }

    void a(PacketSendEvent packetSendEvent, boolean bl) {
        try {
            int n;
            Player player = (Player)packetSendEvent.getPlayer();
            if (player == null) {
                return;
            }
            UUID uUID = player.getUniqueId();
            if (this.I.contains(uUID)) {
                return;
            }
            if (!this.H.contains(uUID)) {
                return;
            }
            World world = player.getWorld();
            if (this.b(world)) {
                return;
            }
            if (!this.i.contains(world.getEnvironment())) {
                return;
            }
            if (bl) {
                WrapperPlayServerBlockEntityData wrapperPlayServerBlockEntityData = new WrapperPlayServerBlockEntityData(packetSendEvent);
                BlockEntityType blockEntityType = wrapperPlayServerBlockEntityData.getBlockEntityType();
                if (blockEntityType == BlockEntityTypes.SIGN || blockEntityType == BlockEntityTypes.HANGING_SIGN) {
                    return;
                }
                n = wrapperPlayServerBlockEntityData.getPosition().getY();
            } else {
                n = new WrapperPlayServerBlockAction(packetSendEvent).getBlockPosition().getY();
            }
            if (n < this.b) {
                packetSendEvent.setCancelled(true);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private boolean a(BaseChunk baseChunk) {
        if (this.an) {
            return false;
        }
        if (!this.am) {
            this.c(baseChunk);
            if (this.an) {
                return false;
            }
        }
        try {
            if (!this.au.isInstance(baseChunk)) {
                return false;
            }
            Object object = this.ao.get(baseChunk);
            this.ap.set(object, this.as);
            this.aq.set(object, null);
            this.ar.invoke((Object)baseChunk, this.al ? 0 : 4096);
            return true;
        }
        catch (Throwable throwable) {
            this.an = true;
            this.a.getLogger().warning("[AntiESP] fastMaskSection failed, falling back to per-block: " + String.valueOf(throwable));
            return false;
        }
    }

    private boolean b(BaseChunk baseChunk) {
        if (this.an) {
            return false;
        }
        if (!this.am) {
            this.c(baseChunk);
            if (this.an) {
                return false;
            }
        }
        try {
            if (!this.au.isInstance(baseChunk)) {
                return false;
            }
            Object object = this.ao.get(baseChunk);
            this.ap.set(object, this.at);
            this.aq.set(object, null);
            this.ar.invoke((Object)baseChunk, 0);
            return true;
        }
        catch (Throwable throwable) {
            this.an = true;
            this.a.getLogger().warning("[AntiESP] fastSkybaseMaskSection failed: " + String.valueOf(throwable));
            return false;
        }
    }

    private synchronized void c(BaseChunk baseChunk) {
        if (this.am || this.an) {
            return;
        }
        try {
            Class clazz = baseChunk.getClass();
            if (!clazz.getName().contains("Chunk_v")) {
                this.an = true;
                return;
            }
            this.au = clazz;
            this.ao = clazz.getDeclaredField("chunkData");
            this.ao.setAccessible(true);
            this.ar = clazz.getMethod("setBlockCount", Integer.TYPE);
            Object object = this.ao.get(baseChunk);
            Class<?> clazz2 = object.getClass();
            this.ap = clazz2.getField("palette");
            this.aq = clazz2.getField("storage");
            this.ap.setAccessible(true);
            this.aq.setAccessible(true);
            Class<?> clazz3 = Class.forName("com.github.retrooper.packetevents.protocol.world.chunk.palette.SingletonPalette");
            Constructor<?> constructor = clazz3.getConstructor(Integer.TYPE);
            this.as = constructor.newInstance(this.G);
            this.at = constructor.newInstance(0);
            this.am = true;
        }
        catch (Throwable throwable) {
            this.an = true;
            this.a.getLogger().warning("[AntiESP] fast-mask reflection init failed: " + String.valueOf(throwable));
        }
    }

    private int a(int n, boolean bl) {
        return bl ? this.C : (n < 0 ? this.B : this.A);
    }

    private boolean a(BaseChunk[] baseChunkArray, int n, int n2, int n3, boolean bl) {
        boolean[] blArray = this.x;
        int n4 = blArray.length;
        if (n4 == 0) {
            return false;
        }
        int[] nArray = this.D;
        int n5 = nArray.length;
        boolean bl2 = false;
        int n6 = Math.min(n2, baseChunkArray.length);
        for (int i = Math.max(0, n); i < n6; ++i) {
            BaseChunk baseChunk = baseChunkArray[i];
            if (baseChunk == null || baseChunk.isEmpty()) continue;
            int n7 = this.a(n3 + i * 16, bl);
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    for (int i2 = 0; i2 < 16; ++i2) {
                        int n8 = baseChunk.getBlockId(j, k, i2);
                        if (n8 <= 0 || n8 >= n4 || !blArray[n8]) continue;
                        int n9 = n8 < n5 && nArray[n8] >= 0 ? nArray[n8] : n7;
                        baseChunk.set(j, k, i2, n9);
                        bl2 = true;
                    }
                }
            }
        }
        return bl2;
    }

    private boolean a(BaseChunk baseChunk, int n, int n2, int n3) {
        boolean[] blArray = this.x;
        int n4 = blArray.length;
        if (n4 == 0 || baseChunk == null || baseChunk.isEmpty()) {
            return false;
        }
        int[] nArray = this.D;
        int n5 = nArray.length;
        boolean bl = false;
        for (int i = 0; i < 16; ++i) {
            for (int j = n; j < n2; ++j) {
                for (int k = 0; k < 16; ++k) {
                    int n6 = baseChunk.getBlockId(i, j, k);
                    if (n6 <= 0 || n6 >= n4 || !blArray[n6]) continue;
                    int n7 = n6 < n5 && nArray[n6] >= 0 ? nArray[n6] : n3;
                    baseChunk.set(i, j, k, n7);
                    bl = true;
                }
            }
        }
        return bl;
    }

    private boolean a(BaseChunk[] baseChunkArray, int n, int n2) {
        boolean bl = false;
        int n3 = Math.min(n2, baseChunkArray.length);
        for (int i = Math.max(0, n); i < n3; ++i) {
            BaseChunk baseChunk = baseChunkArray[i];
            if (baseChunk == null) continue;
            bl = true;
            if (this.a(baseChunk)) continue;
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    for (int i2 = 0; i2 < 16; ++i2) {
                        baseChunk.set(j, k, i2, this.G);
                    }
                }
            }
        }
        return bl;
    }

    void b(PacketSendEvent packetSendEvent) {
        try {
            int n;
            World world;
            int n2;
            Player player = (Player)packetSendEvent.getPlayer();
            int[] nArray = this.aa.get(player.getUniqueId());
            if (nArray == null || nArray.length < 5) {
                return;
            }
            int n3 = nArray[0];
            int n4 = nArray[2];
            int n5 = nArray[3];
            boolean bl = nArray[4] == World.Environment.NETHER.ordinal();
            int n6 = n2 = nArray.length >= 6 ? nArray[5] : -1;
            if (n4 <= 0 && n2 < 0) {
                return;
            }
            WrapperPlayServerChunkData wrapperPlayServerChunkData = new WrapperPlayServerChunkData(packetSendEvent);
            Column column = wrapperPlayServerChunkData.getColumn();
            if (column == null) {
                return;
            }
            BaseChunk[] baseChunkArray = column.getChunks();
            if (baseChunkArray == null || baseChunkArray.length == 0) {
                return;
            }
            if (baseChunkArray.length != n3 && ((world = player.getWorld()).getEnvironment().ordinal() != nArray[4] || world.getMinHeight() != n5)) {
                return;
            }
            boolean bl2 = this.a(baseChunkArray, 0, n4, n5, bl);
            int n7 = n = nArray.length >= 8 ? nArray[7] : 0;
            if (n2 >= 0 && n2 < baseChunkArray.length) {
                int n8;
                int n9;
                int n10;
                BaseChunk baseChunk;
                if (n > 0 && (baseChunk = baseChunkArray[n2]) != null) {
                    bl2 = true;
                    for (n10 = 0; n10 < 16; ++n10) {
                        for (int i = n; i < 16; ++i) {
                            for (n9 = 0; n9 < 16; ++n9) {
                                baseChunk.set(n10, i, n9, 0);
                            }
                        }
                    }
                }
                for (n10 = n8 = n > 0 ? n2 + 1 : n2; n10 < baseChunkArray.length; ++n10) {
                    BaseChunk baseChunk2 = baseChunkArray[n10];
                    if (baseChunk2 == null) continue;
                    bl2 = true;
                    if (this.b(baseChunk2)) continue;
                    for (n9 = 0; n9 < 16; ++n9) {
                        for (int i = 0; i < 16; ++i) {
                            for (int j = 0; j < 16; ++j) {
                                baseChunk2.set(n9, i, j, 0);
                            }
                        }
                    }
                }
            }
            if (bl2) {
                packetSendEvent.markForReEncode(true);
            }
        }
        catch (Exception exception) {
            this.a.getLogger().warning("[AntiESP] Exception in anti-xray handler: " + String.valueOf(exception));
        }
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onPlayerInteract(PlayerInteractEvent playerInteractEvent) {
        if (!this.k) {
            return;
        }
        if (playerInteractEvent.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        Block block = playerInteractEvent.getClickedBlock();
        if (block == null) {
            return;
        }
        Location location = block.getLocation();
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        if (this.b(world)) {
            return;
        }
        int n = location.getBlockY();
        if (n >= this.c(world)) {
            return;
        }
        Player player = playerInteractEvent.getPlayer();
        UUID uUID2 = player.getUniqueId();
        if (this.I.contains(uUID2)) {
            return;
        }
        if (this.H.contains(uUID2)) {
            return;
        }
        Set<Material> set = this.y;
        if (set == null || set.isEmpty()) {
            return;
        }
        Material material = block.getType();
        if (!set.contains(material)) {
            return;
        }
        int n2 = location.getBlockX();
        int n3 = location.getBlockZ();
        long l = System.currentTimeMillis();
        long[] lArray = this.ag.get(uUID2);
        if (lArray != null) {
            boolean bl;
            boolean bl2 = lArray[1] == (long)n2 && lArray[2] == (long)n && lArray[3] == (long)n3;
            boolean bl3 = bl = l - lArray[0] < 200L;
            if (bl2 && bl) {
                return;
            }
            if (bl && !bl2) {
                return;
            }
            lArray[0] = l;
            lArray[1] = n2;
            lArray[2] = n;
            lArray[3] = n3;
        } else {
            this.ag.put(uUID2, new long[]{l, n2, n, n3});
        }
        HashMap<Location, BlockData> hashMap = new HashMap<Location, BlockData>(1);
        hashMap.put(location, block.getBlockData());
        player.sendMultiBlockChange(hashMap);
        this.ac.computeIfAbsent(uUID2, uUID -> b.f()).add(b.a(n2, n, n3));
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onBlockBreak(BlockBreakEvent blockBreakEvent) {
        if (!this.k) {
            return;
        }
        Block block = blockBreakEvent.getBlock();
        Location location = block.getLocation();
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        int n = location.getBlockX();
        int n2 = location.getBlockY();
        int n3 = location.getBlockZ();
        if (n2 >= this.c(world)) {
            return;
        }
        if (this.b(world)) {
            return;
        }
        Set<Material> set = this.z;
        if (set == null || set.isEmpty()) {
            return;
        }
        HashMap<Location, BlockData> hashMap = null;
        int n4 = world.getMinHeight();
        int n5 = world.getMaxHeight();
        for (int[] object : aK) {
            Block d5;
            int player = n + object[0];
            int uUID = n2 + object[1];
            int location2 = n3 + object[2];
            if (uUID < n4 || uUID >= n5 || !set.contains((d5 = world.getBlockAt(player, uUID, location2)).getType())) continue;
            if (hashMap == null) {
                hashMap = new HashMap<Location, BlockData>();
            }
            hashMap.put(new Location(world, (double)player, (double)uUID, (double)location2), d5.getBlockData());
        }
        if (hashMap != null) {
            double d2 = 2304.0;
            int n9 = this.c(world);
            for (Player player : world.getPlayers()) {
                double d3;
                double d4;
                double d5;
                Location location2;
                UUID uUID = player.getUniqueId();
                if (this.I.contains(uUID) || this.H.contains(uUID) || (location2 = player.getLocation()).getBlockY() >= n9 || !((d5 = (double)(location2.getBlockX() - n)) * d5 + (d4 = (double)(location2.getBlockY() - n2)) * d4 + (d3 = (double)(location2.getBlockZ() - n3)) * d3 <= d2)) continue;
                player.sendMultiBlockChange(hashMap);
            }
        }
    }

    private void j() {
        if (--this.ak <= 0 || this.ai.isEmpty()) {
            this.ak = 20;
            this.ai.clear();
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uUID = player.getUniqueId();
                if (this.I.contains(uUID) || this.H.contains(uUID)) continue;
                this.ai.add(uUID);
            }
        }
        if (this.ai.isEmpty()) {
            return;
        }
        int n = this.ai.size();
        if (this.aj >= n) {
            this.aj = 0;
        }
        int n2 = Math.min(Math.max(1, (n + 3) / 4), n);
        boolean bl = e.a();
        for (int i = 0; i < n2; ++i) {
            long[] lArray;
            Location location;
            int n3 = (this.aj + i) % n;
            UUID uUID = this.ai.get(n3);
            Player player = Bukkit.getPlayer((UUID)uUID);
            if (player == null || !player.isOnline() || (location = player.getLocation()).getBlockY() >= this.c(player.getWorld()) || (lArray = this.ab.get(uUID)) != null && location.getBlockX() == (int)lArray[0] && location.getBlockY() == (int)lArray[1] && location.getBlockZ() == (int)lArray[2]) continue;
            if (bl) {
                e.a((Plugin)this.a, (Entity)player, () -> this.e(player));
                continue;
            }
            this.e(player);
        }
        this.aj = (this.aj + n2) % Math.max(1, n);
    }

    private void e(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        World world = player.getWorld();
        if (world == null) {
            return;
        }
        if (this.b(world)) {
            return;
        }
        Location location = player.getLocation();
        if (location == null) {
            return;
        }
        UUID uUID = player.getUniqueId();
        if (location.getBlockY() >= this.c(world)) {
            this.ab.remove(uUID);
            if (this.y != null && !this.y.isEmpty()) {
                this.a(player, world, uUID);
            }
            return;
        }
        UUID uUID2 = player.getUniqueId();
        int n = location.getBlockX();
        int n2 = location.getBlockY();
        int n3 = location.getBlockZ();
        long[] lArray = this.ab.get(uUID2);
        if (lArray != null && Math.abs(n - (int)lArray[0]) < 1 && Math.abs(n2 - (int)lArray[1]) < 1 && Math.abs(n3 - (int)lArray[2]) < 1) {
            return;
        }
        if (lArray != null) {
            lArray[0] = n;
            lArray[1] = n2;
            lArray[2] = n3;
        } else {
            this.ab.put(uUID2, new long[]{n, n2, n3});
        }
        this.a(player, location);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void a(Player player, World world, UUID uUID) {
        Long l2;
        long[] lArray;
        Set<Long> set = this.ac.remove(uUID);
        if (set == null || set.isEmpty()) {
            return;
        }
        boolean bl = world.getEnvironment() == World.Environment.NETHER;
        int n = world.getMinHeight();
        Object object = set;
        synchronized (object) {
            lArray = new long[set.size()];
            int n2 = 0;
            for (Long l2 : set) {
                lArray[n2++] = l2;
            }
        }
        object = new HashMap(Math.min(lArray.length, 1024));
        BlockData blockData = this.af;
        BlockData blockData2 = this.ad;
        l2 = this.ae;
        for (long l3 : lArray) {
            int n3;
            int n4;
            int n5 = (int)(l3 >> 40);
            if ((n5 & 0x800000) != 0) {
                n5 |= 0xFF000000;
            }
            if (((n4 = (int)(l3 >> 24 & 0xFFFFL)) & 0x8000) != 0) {
                n4 |= 0xFFFF0000;
            }
            if (((n3 = (int)(l3 & 0xFFFFFFL)) & 0x800000) != 0) {
                n3 |= 0xFF000000;
            }
            if (n4 < n || n4 >= this.c(world)) continue;
            BlockData blockData3 = bl ? blockData : (n4 < 0 ? l2 : blockData2);
            object.put(new Location(world, (double)n5, (double)n4, (double)n3), blockData3);
        }
        if (!object.isEmpty() && player.isOnline()) {
            player.sendMultiBlockChange((Map)object);
        }
    }

    private void f(Player player) {
        if (!this.k || this.y == null || this.y.isEmpty()) {
            return;
        }
        UUID uUID = player.getUniqueId();
        Set<Long> set = this.ac.get(uUID);
        if (set == null || set.isEmpty()) {
            return;
        }
        e.a((Plugin)this.a, (Entity)player, () -> {
            if (!player.isOnline()) {
                return;
            }
            if (!this.H.contains(uUID)) {
                return;
            }
            World world = player.getWorld();
            if (world == null || this.b(world)) {
                return;
            }
            this.a(player, world, uUID);
        }, 40L);
    }

    private void a(Player player, Location location) {
        int n;
        int n2;
        int n3;
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        int n4 = location.getBlockX();
        int n5 = location.getBlockY();
        int n6 = location.getBlockZ();
        int n7 = this.p;
        int n8 = this.q;
        int n9 = Math.max(n7, n8);
        Set<Material> set = this.y;
        if (set == null || set.isEmpty()) {
            return;
        }
        int n10 = world.getMinHeight();
        int n11 = Math.min(this.c(world), world.getMaxHeight());
        int n12 = Math.max(n10, n5 - n9);
        if (n12 >= (n3 = Math.min(n11, n5 + n9 + 1))) {
            return;
        }
        int n13 = n4 - n9 >> 4;
        int n14 = n4 + n9 >> 4;
        int n15 = n6 - n9 >> 4;
        int n16 = n6 + n9 >> 4;
        int n17 = n14 - n13 + 1;
        int n18 = n16 - n15 + 1;
        ChunkSnapshot[] chunkSnapshotArray = new ChunkSnapshot[n17 * n18];
        for (n2 = n13; n2 <= n14; ++n2) {
            for (n = n15; n <= n16; ++n) {
                if (!world.isChunkLoaded(n2, n)) continue;
                try {
                    chunkSnapshotArray[(n2 - n13) * n18 + (n - n15)] = world.getChunkAt(n2, n).getChunkSnapshot(false, false, false);
                    continue;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        n2 = Math.max(n12, n5 - n9);
        if (n2 >= (n = Math.min(n3, n5 + n9 + 1))) {
            return;
        }
        boolean bl = this.r;
        double d2 = location.getX();
        double d3 = location.getY() + player.getEyeHeight();
        double d4 = location.getZ();
        int n19 = world.getMaxHeight();
        int n20 = 3 * n9 + 8;
        HashMap<Location, BlockData> hashMap = null;
        UUID uUID2 = player.getUniqueId();
        Set set2 = null;
        for (int i = -n9; i <= n9; ++i) {
            int n21 = n4 + i;
            int n22 = n21 >> 4;
            int n23 = n21 & 0xF;
            int n24 = (n22 - n13) * n18;
            boolean bl2 = i >= -n7 && i <= n7;
            for (int j = -n9; j <= n9; ++j) {
                int n25 = n6 + j;
                int n26 = n25 >> 4;
                int n27 = n25 & 0xF;
                ChunkSnapshot chunkSnapshot = chunkSnapshotArray[n24 + (n26 - n15)];
                if (chunkSnapshot == null) continue;
                boolean bl3 = bl2 && j >= -n7 && j <= n7;
                for (int k = n2; k < n; ++k) {
                    int n28 = k - n5;
                    boolean bl4 = bl3 && n28 >= -n7 && n28 <= n7;
                    BlockData blockData = chunkSnapshot.getBlockData(n23, k, n27);
                    Material material = blockData.getMaterial();
                    if (!bl4 && material != Material.LAVA && material != Material.WATER || !set.contains(material) || bl && bl4 && material != Material.LAVA && material != Material.WATER && (i < -1 || i > 1 || n28 < -1 || n28 > 1 || j < -1 || j > 1) && this.a(d2, d3, d4, n21, k, n25, chunkSnapshotArray, n13, n15, n18, n10, n19, n20)) continue;
                    if (hashMap == null) {
                        hashMap = new HashMap<Location, BlockData>();
                    }
                    if (set2 == null) {
                        set2 = this.ac.computeIfAbsent(uUID2, uUID -> b.f());
                    }
                    hashMap.put(new Location(world, (double)n21, (double)k, (double)n25), blockData);
                    set2.add(b.a(n21, k, n25));
                }
            }
        }
        if (hashMap != null) {
            player.sendMultiBlockChange(hashMap);
        }
    }

    private boolean a(double d2, double d3, double d4, int n, int n2, int n3, ChunkSnapshot[] chunkSnapshotArray, int n4, int n5, int n6, int n7, int n8, int n9) {
        double[][] dArrayArray;
        for (double[] dArray : dArrayArray = new double[][]{{(double)n + 0.5, (double)n2 + 0.5, (double)n3 + 0.5}, {(double)n + 0.15, (double)n2 + 0.15, (double)n3 + 0.15}, {(double)n + 0.85, (double)n2 + 0.15, (double)n3 + 0.15}, {(double)n + 0.15, (double)n2 + 0.85, (double)n3 + 0.15}, {(double)n + 0.85, (double)n2 + 0.85, (double)n3 + 0.15}, {(double)n + 0.15, (double)n2 + 0.15, (double)n3 + 0.85}, {(double)n + 0.85, (double)n2 + 0.15, (double)n3 + 0.85}, {(double)n + 0.15, (double)n2 + 0.85, (double)n3 + 0.85}, {(double)n + 0.85, (double)n2 + 0.85, (double)n3 + 0.85}}) {
            if (!this.a(d2, d3, d4, dArray[0], dArray[1], dArray[2], n, n2, n3, chunkSnapshotArray, n4, n5, n6, n7, n8, n9)) continue;
            return false;
        }
        return true;
    }

    private boolean a(double d2, double d3, double d4, double d5, double d6, double d7, int n, int n2, int n3, ChunkSnapshot[] chunkSnapshotArray, int n4, int n5, int n6, int n7, int n8, int n9) {
        int n10;
        int n11;
        double d8 = d5 - d2;
        double d9 = d6 - d3;
        double d10 = d7 - d4;
        int n12 = b.a(d2);
        int n13 = b.a(d3);
        int n14 = b.a(d4);
        int n15 = d8 > 0.0 ? 1 : (n11 = d8 < 0.0 ? -1 : 0);
        int n16 = d9 > 0.0 ? 1 : (n10 = d9 < 0.0 ? -1 : 0);
        int n17 = d10 > 0.0 ? 1 : (d10 < 0.0 ? -1 : 0);
        double d11 = n11 != 0 ? b.a(d2, d8, n12, n11) : Double.POSITIVE_INFINITY;
        double d12 = n10 != 0 ? b.a(d3, d9, n13, n10) : Double.POSITIVE_INFINITY;
        double d13 = n17 != 0 ? b.a(d4, d10, n14, n17) : Double.POSITIVE_INFINITY;
        double d14 = n11 != 0 ? Math.abs(1.0 / d8) : Double.POSITIVE_INFINITY;
        double d15 = n10 != 0 ? Math.abs(1.0 / d9) : Double.POSITIVE_INFINITY;
        double d16 = n17 != 0 ? Math.abs(1.0 / d10) : Double.POSITIVE_INFINITY;
        for (int i = 0; i < n9; ++i) {
            if (d11 <= d12 && d11 <= d13) {
                n12 += n11;
                d11 += d14;
            } else if (d12 <= d13) {
                n13 += n10;
                d12 += d15;
            } else {
                n14 += n17;
                d13 += d16;
            }
            if (n12 == n && n13 == n2 && n14 == n3) {
                return true;
            }
            if (!this.a(n12, n13, n14, chunkSnapshotArray, n4, n5, n6, n7, n8)) continue;
            return false;
        }
        return true;
    }

    private static double a(double d2, double d3, int n, int n2) {
        double d4 = n2 > 0 ? (double)(n + 1) : (double)n;
        return (d4 - d2) / d3;
    }

    private static int a(double d2) {
        int n = (int)d2;
        return d2 < (double)n ? n - 1 : n;
    }

    private boolean a(int n, int n2, int n3, ChunkSnapshot[] chunkSnapshotArray, int n4, int n5, int n6, int n7, int n8) {
        if (n2 < n7 || n2 >= n8) {
            return false;
        }
        int n9 = ((n >> 4) - n4) * n6 + ((n3 >> 4) - n5);
        if (n9 < 0 || n9 >= chunkSnapshotArray.length) {
            return false;
        }
        ChunkSnapshot chunkSnapshot = chunkSnapshotArray[n9];
        if (chunkSnapshot == null) {
            return false;
        }
        try {
            Material material = chunkSnapshot.getBlockType(n & 0xF, n2, n3 & 0xF);
            return material != null && material.isOccluding();
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    private int a(Material material) {
        if (b.c(material)) {
            return 0;
        }
        try {
            BlockData blockData = material.createBlockData();
            Method method = blockData.getClass().getMethod("getState", new Class[0]);
            Object object = method.invoke((Object)blockData, new Object[0]);
            Class<?> clazz = Class.forName("net.minecraft.world.level.block.Block");
            for (Method method2 : clazz.getDeclaredMethods()) {
                if (!Modifier.isStatic(method2.getModifiers()) || method2.getReturnType() != Integer.TYPE || method2.getParameterCount() != 1 || !method2.getParameterTypes()[0].isAssignableFrom(object.getClass())) continue;
                method2.setAccessible(true);
                int n = (Integer)method2.invoke(null, object);
                return n;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return 0;
    }

    private List<Integer> b(Material material) {
        ArrayList<Integer> arrayList;
        block14: {
            arrayList = new ArrayList<Integer>();
            if (b.c(material)) {
                return arrayList;
            }
            try {
                int n;
                BlockData blockData = material.createBlockData();
                Method method = blockData.getClass().getMethod("getState", new Class[0]);
                Object object = method.invoke((Object)blockData, new Object[0]);
                Class<?> clazz = Class.forName("net.minecraft.world.level.block.Block");
                Method methodArray = null;
                for (Method object2 : clazz.getDeclaredMethods()) {
                    if (!Modifier.isStatic(object2.getModifiers()) || object2.getReturnType() != Integer.TYPE || object2.getParameterCount() != 1 || !object2.getParameterTypes()[0].isAssignableFrom(object.getClass())) continue;
                    object2.setAccessible(true);
                    methodArray = object2;
                    break;
                }
                if (methodArray == null) {
                    return arrayList;
                }
                Method method2 = object.getClass().getMethod("getBlock", new Class[0]);
                Object object3 = method2.invoke(object, new Object[0]);
                Method method3 = null;
                for (Method method4 : object3.getClass().getMethods()) {
                    if (method4.getParameterCount() != 0 || !method4.getReturnType().getSimpleName().contains("StateDefinition")) continue;
                    method3 = method4;
                    break;
                }
                if (method3 != null) {
                    Object object2 = method3.invoke(object3, new Object[0]);
                    Object object4 = null;
                    for (Method method5 : object2.getClass().getMethods()) {
                        if (method5.getParameterCount() != 0 || !Collection.class.isAssignableFrom(method5.getReturnType()) || !method5.getName().contains("ossible")) continue;
                        object4 = method5;
                        break;
                    }
                    if (object4 == null) {
                        for (Method method6 : object2.getClass().getMethods()) {
                            if (method6.getParameterCount() != 0 || !Collection.class.isAssignableFrom(method6.getReturnType())) continue;
                            object4 = method6;
                            break;
                        }
                    }
                    if (object4 != null) {
                        Collection collection = (Collection)((Method)object4).invoke(object2, new Object[0]);
                        Iterator iterator = collection.iterator();
                        while (iterator.hasNext()) {
                            Object e2 = iterator.next();
                            int n2 = (Integer)methodArray.invoke(null, e2);
                            if (n2 <= 0) continue;
                            arrayList.add(n2);
                        }
                        if (!arrayList.isEmpty()) {
                            return arrayList;
                        }
                    }
                }
                if ((n = ((Integer)methodArray.invoke(null, object)).intValue()) > 0) {
                    arrayList.add(n);
                }
            }
            catch (Exception exception) {
                int n = this.a(material);
                if (n <= 0) break block14;
                arrayList.add(n);
            }
        }
        return arrayList;
    }

    private boolean a(Player player, World world, int n, int n2) {
        if (!world.isChunkLoaded(n, n2)) {
            return true;
        }
        try {
            if (!this.aG) {
                this.a(player, world);
            }
            if (this.aH) {
                return false;
            }
            Object object = this.aA.invoke((Object)world, new Object[0]);
            Object object2 = this.aB.invoke(object, n, n2);
            Object object3 = this.aC.invoke(object, new Object[0]);
            Object object4 = this.b(object2, object3);
            Object object5 = this.az.invoke((Object)player, new Object[0]);
            Object object6 = this.aE.get(object5);
            this.a(object6, object4);
            return true;
        }
        catch (Exception exception) {
            return false;
        }
    }

    private void a(int n, int n2, Object object, Object object2, Object object3) {
        try {
            Object object4 = this.aB.invoke(object, n, n2);
            if (this.ay) {
                Object object5 = this.b(object4, object2);
                this.a(object3, object5);
                return;
            }
            ExecutorService executorService = this.ax[(System.identityHashCode(object3) & Integer.MAX_VALUE) % 4];
            executorService.execute(() -> {
                try {
                    Object object4 = this.b(object4, object2);
                    this.a(object3, object4);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            });
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private boolean a(Player player, World world, int n, int n2, Object object, Object object2, Object object3) {
        if (!world.isChunkLoaded(n, n2)) {
            return true;
        }
        try {
            if (object == null || object2 == null || object3 == null) {
                return this.a(player, world, n, n2);
            }
            Object object4 = this.aB.invoke(object, n, n2);
            Object object5 = this.b(object4, object2);
            this.a(object3, object5);
            return true;
        }
        catch (Exception exception) {
            return false;
        }
    }

    /*
     * WARNING - void declaration
     */
    private synchronized void a(Player player, World world) {
        if (this.aG) {
            return;
        }
        this.aG = true;
        try {
            boolean bl;
            void var14_39;
            Object object;
            void var14_35;
            void var14_33;
            void var10_21;
            Object object2;
            this.az = player.getClass().getMethod("getHandle", new Class[0]);
            Object object3 = this.az.invoke((Object)player, new Object[0]);
            this.aA = world.getClass().getMethod("getHandle", new Class[0]);
            Object object4 = this.aA.invoke((Object)world, new Object[0]);
            Method method = null;
            Method method2 = null;
            for (Method genericDeclaration2 : object4.getClass().getMethods()) {
                String string;
                String string2;
                Class<?> clazz;
                if (genericDeclaration2.getParameterCount() != 2 || (object2 = genericDeclaration2.getParameterTypes())[0] != Integer.TYPE || object2[1] != Integer.TYPE || (clazz = genericDeclaration2.getReturnType()).isPrimitive() || (string2 = clazz.getSimpleName()).equals("CompletableFuture") || !string2.contains("Chunk") || (string = genericDeclaration2.getName()).contains("IfLoaded") || string.endsWith("Now") || string.contains("Immediately")) continue;
                if (string.equals("getChunk") || string.equals("getChunkAt")) {
                    method = genericDeclaration2;
                    break;
                }
                if (method2 != null) continue;
                method2 = genericDeclaration2;
            }
            Method method3 = this.aB = method != null ? method : method2;
            if (this.aB == null) {
                throw new RuntimeException("Cannot find (int,int)->Chunk on " + object4.getClass().getName());
            }
            for (Method method4 : object4.getClass().getMethods()) {
                if (method4.getParameterCount() != 0 || !method4.getReturnType().getSimpleName().contains("Light")) continue;
                this.aC = method4;
                break;
            }
            if (this.aC == null) {
                for (Method method5 : object4.getClass().getMethods()) {
                    if (method5.getParameterCount() != 0 || !method5.getName().equals("getLightEngine")) continue;
                    this.aC = method5;
                    break;
                }
            }
            if (this.aC == null) {
                throw new RuntimeException("Cannot find getLightEngine");
            }
            Object object6 = this.aC.invoke(object4, new Object[0]);
            Object object7 = this.aB.invoke(object4, player.getLocation().getBlockX() >> 4, player.getLocation().getBlockZ() >> 4);
            String[] stringArray = new String[]{"net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket", "net.minecraft.network.protocol.game.PacketPlayOutMapChunk"};
            Object var10_19 = null;
            for (String string : stringArray) {
                try {
                    Class<?> clazz = Class.forName(string);
                    break;
                }
                catch (ClassNotFoundException classNotFoundException) {
                }
            }
            if (var10_21 == null) {
                throw new RuntimeException("Cannot find chunk packet class");
            }
            object2 = new ArrayList();
            Object object5 = var10_21.getConstructors();
            int n = ((Constructor<?>[])object5).length;
            boolean bl2 = false;
            while (var14_33 < n) {
                Constructor<?> constructor = object5[var14_33];
                Class<?>[] classArray = constructor.getParameterTypes();
                if (classArray.length >= 2 && classArray[0].isAssignableFrom(object7.getClass()) && classArray[1].isAssignableFrom(object6.getClass())) {
                    object2.add(constructor);
                }
                ++var14_33;
            }
            object2.sort(Comparator.comparingInt(Constructor::getParameterCount));
            if (object2.isEmpty()) {
                throw new RuntimeException("Cannot find chunk packet constructor on " + var10_21.getName());
            }
            this.aD = (Constructor)object2.get(0);
            object5 = object3.getClass().getFields();
            n = ((AccessibleObject[])object5).length;
            boolean bl3 = false;
            while (var14_35 < n) {
                AccessibleObject accessibleObject = object5[var14_35];
                if (((Field)accessibleObject).getType().getSimpleName().contains("Connection") || ((Field)accessibleObject).getType().getSimpleName().contains("ServerGamePacketListener")) {
                    this.aE = accessibleObject;
                    break;
                }
                ++var14_35;
            }
            if (this.aE == null) {
                void var14_37;
                object5 = object3.getClass().getDeclaredFields();
                n = ((AccessibleObject[])object5).length;
                boolean bl4 = false;
                while (var14_37 < n) {
                    AccessibleObject accessibleObject = object5[var14_37];
                    ((Field)accessibleObject).setAccessible(true);
                    if (((Field)accessibleObject).getType().getSimpleName().contains("Connection") || ((Field)accessibleObject).getType().getSimpleName().contains("ServerGamePacketListener")) {
                        this.aE = accessibleObject;
                        break;
                    }
                    ++var14_37;
                }
            }
            if (this.aE == null) {
                throw new RuntimeException("Cannot find connection field");
            }
            this.aE.setAccessible(true);
            object5 = this.aE.get(object3);
            Method method6 = null;
            Object var14_38 = null;
            for (Method method7 : object5.getClass().getMethods()) {
                String string;
                if (method7.getParameterCount() != 1 || (object = method7.getParameterTypes()[0]) == Object.class || !(string = ((Class)object).getSimpleName()).equals("Packet") && !string.endsWith("Packet")) continue;
                if (method7.getName().equals("send") || method7.getName().equals("sendPacket")) {
                    method6 = method7;
                    break;
                }
                if (var14_39 != null) continue;
                Method method8 = method7;
            }
            Object object8 = this.aF = method6 != null ? method6 : var14_39;
            if (this.aF == null) {
                throw new RuntimeException("Cannot find send(Packet) on " + object5.getClass().getName());
            }
            boolean bl5 = false;
            Throwable throwable = null;
            Iterator iterator = object2.iterator();
            while (iterator.hasNext()) {
                Constructor constructor;
                this.aD = constructor = (Constructor)iterator.next();
                this.aL = null;
                try {
                    object = this.b(object7, object6);
                    if (object == null) continue;
                    bl = true;
                    break;
                }
                catch (Throwable throwable2) {
                    throwable = throwable2;
                    this.a.getLogger().info("[AntiESP] ctor " + constructor.getParameterCount() + "-arg failed sanity test: " + throwable2.getClass().getSimpleName() + ": " + throwable2.getMessage());
                }
            }
            if (!bl) {
                throw new RuntimeException("All " + object2.size() + " matching chunk-packet ctors failed sanity test" + (String)(throwable != null ? " (last: " + String.valueOf(throwable) + ")" : ""));
            }
        }
        catch (Exception exception) {
            this.aH = true;
            String string = this.w ? "Paper API Player.refreshChunk" : "SMBC (sendMultiBlockChange) \u2014 slowest path";
            this.a.getLogger().warning("[AntiESP] NMS reflection init FAILED \u2014 falling back to " + string + ". MC=" + Bukkit.getMinecraftVersion() + ", server=" + Bukkit.getName() + " " + Bukkit.getVersion() + ". Cause: " + String.valueOf(exception));
            exception.printStackTrace();
        }
        if (!this.aH) {
            this.a.getLogger().info("[AntiESP] NMS reflection ready: MC=" + Bukkit.getMinecraftVersion() + ", getChunk=" + (this.aB == null ? "null" : this.aB.getName()) + ", chunkPacketCtor=" + (String)(this.aD == null ? "null" : this.aD.getDeclaringClass().getSimpleName() + "(" + this.aD.getParameterCount() + " args)") + ", send=" + (String)(this.aF == null ? "null" : this.aF.getDeclaringClass().getSimpleName() + "." + this.aF.getName() + "(" + this.aF.getParameterTypes()[0].getSimpleName() + ")") + ", paperRefreshChunk=" + this.w);
        }
    }

    private void a(Object object, Object object2) {
        boolean bl;
        Method method = this.aF;
        if (method == null) {
            return;
        }
        Class<?> clazz = method.getDeclaringClass();
        Class<?> clazz2 = method.getParameterTypes()[0];
        boolean bl2 = bl = clazz.isInstance(object) && clazz2.isInstance(object2);
        if (!bl) {
            Method method2 = null;
            for (Method method3 : object.getClass().getMethods()) {
                Class<?> clazz3;
                if (method3.getParameterCount() != 1 || !(clazz3 = method3.getParameterTypes()[0]).isInstance(object2)) continue;
                if (method3.getName().equals("send") || method3.getName().equals("sendPacket")) {
                    method2 = method3;
                    break;
                }
                if (method2 != null) continue;
                method2 = method3;
            }
            if (method2 != null) {
                method2.setAccessible(true);
                this.aF = method2;
                method = method2;
            }
        }
        method.invoke(object, object2);
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onMove(PlayerMoveEvent playerMoveEvent) {
        int n;
        int n2;
        int n3;
        int n4;
        int n5;
        int n6;
        int n7;
        if (playerMoveEvent instanceof PlayerTeleportEvent) {
            return;
        }
        Location location = playerMoveEvent.getFrom();
        Location location2 = playerMoveEvent.getTo();
        if (location2 == null) {
            return;
        }
        Player player = playerMoveEvent.getPlayer();
        UUID uUID = player.getUniqueId();
        if (this.S.containsKey(uUID)) {
            n7 = location2.getBlockX();
            n6 = location2.getBlockZ();
            n5 = player.isGliding() || player.getGameMode() == GameMode.SPECTATOR ? 1 : 0;
            n4 = n5 != 0 ? this.Y : this.V;
            long[] lArray = this.T.get(uUID);
            int n8 = n3 = lArray == null ? 1 : 0;
            if (n3 == 0) {
                n2 = n7 - (int)lArray[0];
                int n9 = n6 - (int)lArray[1];
                int n10 = n3 = n2 * n2 + n9 * n9 >= n4 ? 1 : 0;
            }
            if (n3 != 0) {
                this.T.put(uUID, new long[]{n7, n6});
                this.a(player, n5 != 0);
            }
        }
        if ((n7 = location.getBlockY()) == (n6 = location2.getBlockY())) {
            return;
        }
        if (this.I.contains(uUID)) {
            return;
        }
        if (!this.a(player.getWorld())) {
            return;
        }
        this.P.put(uUID, n6);
        n5 = n6 < n7 ? 5 : 2;
        n4 = n7 >= this.c + n5 ? 1 : 0;
        int n11 = n = n6 >= this.c + n5 ? 1 : 0;
        if (n4 != n) {
            if (n != 0) {
                this.H.add(uUID);
                this.S.remove(uUID);
                this.T.remove(uUID);
                if (this.K || !this.J.contains(uUID)) {
                    this.b(player, true);
                    this.f(player);
                }
                if (this.L != null) {
                    this.L.b(player);
                }
            } else {
                this.a(player, n7);
            }
        } else if (this.H.contains(uUID) && n6 < this.c && n7 >= this.c) {
            this.a(player, n7);
        } else if (!this.H.contains(uUID) && this.S.containsKey(uUID)) {
            this.a(player, player.isGliding() || player.getGameMode() == GameMode.SPECTATOR);
        }
        if (this.L != null) {
            n3 = n7 >= this.b ? 1 : 0;
            int n12 = n2 = n6 >= this.b ? 1 : 0;
            if (n3 != n2) {
                if (n2 != 0) {
                    this.L.e(player);
                } else {
                    this.L.d(player);
                }
            }
        }
        if (this.s > 0) {
            n3 = n7 < this.s ? 1 : 0;
            int n13 = n2 = n6 < this.s ? 1 : 0;
            if (n3 != n2) {
                this.M.remove(uUID);
                Location location3 = location2.clone();
                e.a((Plugin)this.a, (Entity)player, () -> {
                    if (player.isOnline() && this.a(player.getWorld())) {
                        this.b(player, location3);
                    }
                }, 1L);
            }
        }
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onTeleport(PlayerTeleportEvent playerTeleportEvent) {
        int n;
        int n2;
        boolean bl;
        Location location = playerTeleportEvent.getTo();
        if (location == null) {
            return;
        }
        Player player = playerTeleportEvent.getPlayer();
        UUID uUID = player.getUniqueId();
        if (this.I.contains(uUID)) {
            return;
        }
        this.ab.remove(uUID);
        if (!playerTeleportEvent.getFrom().getWorld().equals((Object)location.getWorld())) {
            return;
        }
        if (!this.a(player.getWorld())) {
            return;
        }
        int n3 = location.getBlockY();
        this.P.put(uUID, n3);
        boolean bl2 = this.H.contains(uUID);
        boolean bl3 = bl = n3 >= this.c;
        if (bl2 && !bl) {
            n2 = playerTeleportEvent.getFrom().getBlockY();
            e.a((Plugin)this.a, (Entity)player, () -> {
                if (!player.isOnline()) {
                    return;
                }
                if (!this.a(player.getWorld())) {
                    return;
                }
                if (this.H.contains(uUID) && player.getLocation().getBlockY() >= this.c) {
                    return;
                }
                this.a(player, n2);
            }, 1L);
        } else if (!bl2 && bl) {
            this.H.add(uUID);
            this.S.remove(uUID);
            this.T.remove(uUID);
            player.setNoDamageTicks(60);
            player.setVelocity(player.getVelocity().zero());
            if (this.K || !this.J.contains(uUID)) {
                e.a((Plugin)this.a, (Entity)player, () -> {
                    if (player.isOnline() && this.H.contains(uUID) && this.a(player.getWorld())) {
                        this.j(player);
                    }
                }, 1L);
                this.b(player, true);
                this.f(player);
            }
            if (this.L != null) {
                this.L.b(player);
            }
        } else if (bl2) {
            // empty if block
        }
        if (this.L != null) {
            n2 = playerTeleportEvent.getFrom().getBlockY() >= this.b ? 1 : 0;
            int n4 = n = n3 >= this.b ? 1 : 0;
            if (n2 != n) {
                if (n != 0) {
                    this.L.e(player);
                } else {
                    this.L.d(player);
                }
            }
        }
        if (this.s > 0) {
            int n5;
            n2 = playerTeleportEvent.getFrom().getBlockY();
            n = n2 < this.s ? 1 : 0;
            int n6 = n5 = n3 < this.s ? 1 : 0;
            if (n != n5) {
                boolean bl4;
                boolean bl5 = this.H.contains(uUID);
                boolean bl6 = bl4 = bl2 != bl;
                if (bl5) {
                    player.setNoDamageTicks(60);
                    player.setVelocity(player.getVelocity().zero());
                }
                if (!bl4) {
                    this.M.remove(uUID);
                }
                Location location2 = location.clone();
                e.a((Plugin)this.a, (Entity)player, () -> {
                    if (player.isOnline() && this.a(player.getWorld())) {
                        this.b(player, location2);
                    }
                }, 1L);
            }
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onLogin(PlayerLoginEvent playerLoginEvent) {
        if (playerLoginEvent.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            return;
        }
        Player player = playerLoginEvent.getPlayer();
        if (this.L != null) {
            this.L.a(player);
        }
        if (this.I.contains(player.getUniqueId())) {
            return;
        }
        if (!this.a(player.getWorld())) {
            return;
        }
        if (player.getLocation().getBlockY() >= this.c) {
            this.H.add(player.getUniqueId());
            this.m(player);
        }
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent playerJoinEvent) {
        Player player = playerJoinEvent.getPlayer();
        if (this.L != null) {
            this.L.a(player);
        }
        this.m(player);
        if (!this.aG) {
            this.a(player, player.getWorld());
        }
        if (this.I.contains(player.getUniqueId())) {
            return;
        }
        if (!this.a(player.getWorld())) {
            this.Q.remove(player.getUniqueId());
            return;
        }
        this.Q.add(player.getUniqueId());
        UUID uUID = player.getUniqueId();
        this.P.put(uUID, player.getLocation().getBlockY());
        if (player.getLocation().getBlockY() >= this.c) {
            this.H.add(uUID);
            if (this.L != null) {
                e.a((Plugin)this.a, (Entity)player, () -> this.L.b(player));
            }
        } else {
            this.H.remove(uUID);
            this.J.add(uUID);
            player.setNoDamageTicks(60);
            player.setVelocity(player.getVelocity().zero());
            Runnable runnable = () -> {
                if (!player.isOnline()) {
                    return;
                }
                if (this.H.contains(uUID)) {
                    return;
                }
                if (!this.a(player.getWorld())) {
                    return;
                }
                this.j(player);
                this.b(player, player.getLocation());
                player.setVelocity(player.getVelocity().zero());
            };
            e.a((Plugin)this.a, (Entity)player, runnable, 10L);
            e.a((Plugin)this.a, (Entity)player, runnable, 40L);
            if (this.L != null) {
                this.L.c(player);
            }
        }
        if (this.L != null && player.getLocation().getBlockY() < this.b) {
            this.L.d(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent playerQuitEvent) {
        UUID uUID = playerQuitEvent.getPlayer().getUniqueId();
        this.H.remove(uUID);
        this.M.remove(uUID);
        this.I.remove(uUID);
        this.J.remove(uUID);
        this.aa.remove(uUID);
        this.ab.remove(uUID);
        this.ac.remove(uUID);
        this.ag.remove(uUID);
        this.P.remove(uUID);
        this.Q.remove(uUID);
        this.S.remove(uUID);
        this.T.remove(uUID);
        if (this.L != null) {
            this.L.a(uUID, playerQuitEvent.getPlayer().getEntityId());
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent playerRespawnEvent) {
        Player player = playerRespawnEvent.getPlayer();
        UUID uUID = player.getUniqueId();
        if (this.L != null) {
            this.L.a(player);
        }
        if (this.I.contains(uUID)) {
            return;
        }
        Location location = playerRespawnEvent.getRespawnLocation();
        if (!player.getWorld().equals((Object)location.getWorld())) {
            return;
        }
        if (!this.a(player.getWorld())) {
            return;
        }
        this.S.remove(uUID);
        this.T.remove(uUID);
        if (this.L != null && location.getBlockY() >= this.b) {
            this.L.e(player);
        }
        if (location.getBlockY() >= this.c) {
            this.H.add(uUID);
            if (this.K || !this.J.contains(uUID)) {
                this.b(player, true);
            }
            if (this.L != null) {
                this.L.b(player);
            }
        } else if (this.H.remove(uUID) && this.L != null) {
            this.L.c(player);
        }
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onGameModeChange(PlayerGameModeChangeEvent playerGameModeChangeEvent) {
        Player player = playerGameModeChangeEvent.getPlayer();
        UUID uUID = player.getUniqueId();
        if (this.I.contains(uUID)) {
            return;
        }
        if (!this.a(player.getWorld())) {
            return;
        }
        GameMode gameMode = playerGameModeChangeEvent.getNewGameMode();
        GameMode gameMode2 = player.getGameMode();
        if (gameMode2 == GameMode.SPECTATOR && gameMode != GameMode.SPECTATOR) {
            Set<Long> set = this.S.remove(uUID);
            this.T.remove(uUID);
            if (set != null && !set.isEmpty() && !this.H.contains(uUID)) {
                e.a((Plugin)this.a, (Entity)player, () -> {
                    if (player.isOnline() && !this.H.contains(uUID) && this.a(player.getWorld())) {
                        this.b(player, player.getLocation());
                    }
                }, 2L);
            }
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent playerChangedWorldEvent) {
        Player player = playerChangedWorldEvent.getPlayer();
        UUID uUID = player.getUniqueId();
        this.M.remove(uUID);
        this.S.remove(uUID);
        this.T.remove(uUID);
        this.J.remove(uUID);
        this.ac.remove(uUID);
        this.ab.remove(uUID);
        this.m(player);
        if (this.L != null) {
            this.L.f(player);
        }
        if (this.I.contains(uUID)) {
            this.Q.remove(uUID);
            return;
        }
        if (!this.a(player.getWorld())) {
            this.Q.remove(uUID);
            if (this.H.remove(uUID)) {
                this.k(player);
                if (this.L != null) {
                    this.L.c(player);
                }
            }
            return;
        }
        this.Q.add(uUID);
        if (player.getLocation().getBlockY() >= this.c) {
            this.H.add(uUID);
            this.b(player, true);
            if (this.L != null) {
                this.L.b(player);
            }
        } else {
            this.H.remove(uUID);
            if (this.L != null) {
                this.L.c(player);
            }
        }
        if (this.L != null && player.getLocation().getBlockY() < this.b) {
            this.L.d(player);
        }
    }

    public void d(Player player) {
        this.m(player);
        if (!this.aG) {
            this.a(player, player.getWorld());
        }
        if (this.I.contains(player.getUniqueId())) {
            return;
        }
        if (!this.a(player.getWorld())) {
            return;
        }
        if (player.getLocation().getBlockY() >= this.c) {
            this.H.add(player.getUniqueId());
            this.b(player, true);
            if (this.L != null) {
                this.L.b(player);
            }
        }
        if (this.L != null && player.getLocation().getBlockY() < this.b) {
            this.L.d(player);
        }
    }

    private void k() {
        Iterator<UUID> iterator = this.Q.iterator();
        while (iterator.hasNext()) {
            UUID uUID = iterator.next();
            Player player = Bukkit.getPlayer((UUID)uUID);
            if (player == null || !player.isOnline()) {
                iterator.remove();
                continue;
            }
            Integer n = this.P.get(uUID);
            int n2 = n != null ? n.intValue() : player.getLocation(this.R).getBlockY();
            e.a((Plugin)this.a, (Entity)player, () -> {
                boolean bl;
                boolean bl2;
                boolean bl3;
                if (!player.isOnline()) {
                    return;
                }
                int n2 = player.getLocation().getBlockY();
                this.P.put(uUID, n2);
                if (n2 == n2) {
                    return;
                }
                boolean bl4 = this.H.contains(uUID);
                int n3 = n2 < n2 ? 5 : 2;
                boolean bl5 = bl3 = n2 >= this.c + n3;
                if (bl4 && !bl3) {
                    this.a(player, n2);
                } else if (!bl4 && bl3) {
                    this.H.add(uUID);
                    this.S.remove(uUID);
                    this.T.remove(uUID);
                    if (this.K || !this.J.contains(uUID)) {
                        this.b(player, true);
                        this.f(player);
                    }
                    if (this.L != null) {
                        this.L.b(player);
                    }
                }
                if (this.L != null) {
                    bl2 = n2 >= this.b;
                    boolean bl6 = bl = n2 >= this.b;
                    if (bl2 != bl) {
                        if (bl) {
                            this.L.e(player);
                        } else {
                            this.L.d(player);
                        }
                    }
                }
                if (this.s > 0) {
                    bl2 = n2 < this.s;
                    boolean bl7 = bl = n2 < this.s;
                    if (bl2 != bl) {
                        boolean bl8 = this.H.contains(uUID);
                        if (bl8) {
                            player.setNoDamageTicks(60);
                            player.setVelocity(player.getVelocity().zero());
                        }
                        this.M.remove(uUID);
                        Location location = player.getLocation().clone();
                        e.a((Plugin)this.a, (Entity)player, () -> {
                            if (player.isOnline() && this.a(player.getWorld())) {
                                this.b(player, location);
                            }
                        }, 1L);
                    }
                }
            });
        }
    }

    private void l() {
        if (this.N != null) {
            return;
        }
        this.N = e.a((Plugin)this.a, this::n, 1L, 1L);
    }

    private void m() {
        if (this.M.isEmpty() && this.N != null) {
            e.a(this.N);
            this.N = null;
        }
    }

    private void n() {
        int n;
        boolean bl;
        if (this.M.isEmpty()) {
            this.m();
            return;
        }
        int n2 = Math.max(1, this.M.size());
        boolean bl2 = bl = this.aG && !this.aH && !this.ay;
        if (bl) {
            n = this.h * 8;
            if (n2 > 20) {
                n = Math.max(64, n / 2);
            }
        } else {
            n = this.h;
            if (n2 > 10) {
                n = Math.max(8, n / 4);
            } else if (n2 > 4) {
                n = Math.max(8, n / 2);
            }
        }
        int n3 = Math.max(this.av, Bukkit.getViewDistance()) + 4;
        this.av = Math.max(this.av, Bukkit.getViewDistance());
        int n4 = n3 * n3;
        int n5 = 0;
        n5 = this.a(n5, n, n4, false);
        n5 = this.a(n5, n, n4, true);
        this.m();
    }

    private int a(int n, int n2, int n3, boolean bl) {
        if (n >= n2) {
            return n;
        }
        int n4 = 0;
        for (Map.Entry<UUID, Queue<a>> entry : this.M.entrySet()) {
            boolean bl2 = this.H.contains(entry.getKey());
            if (bl2 != bl) continue;
            ++n4;
        }
        if (n4 == 0) {
            return n;
        }
        int n5 = n2 - n;
        int n6 = Math.max(1, n5 / n4);
        Iterator<Map.Entry<UUID, Queue<a>>> iterator = this.M.entrySet().iterator();
        while (n < n2 && iterator.hasNext()) {
            a a2;
            Map.Entry<UUID, Queue<a>> entry = iterator.next();
            UUID uUID = entry.getKey();
            Queue<a> queue = entry.getValue();
            if (queue == null || queue.isEmpty()) {
                iterator.remove();
                continue;
            }
            boolean bl3 = this.H.contains(uUID);
            if (bl3 != bl) continue;
            Player player = Bukkit.getPlayer((UUID)uUID);
            if (player == null || !player.isOnline()) {
                iterator.remove();
                continue;
            }
            World world = player.getWorld();
            Location location = player.getLocation();
            int n7 = location.getBlockX() >> 4;
            int n8 = location.getBlockZ() >> 4;
            Object object = null;
            Object object2 = null;
            Object object3 = null;
            if (this.aG && !this.aH) {
                try {
                    object = this.aA.invoke((Object)world, new Object[0]);
                    object2 = this.aC.invoke(object, new Object[0]);
                    Object object4 = this.az.invoke((Object)player, new Object[0]);
                    object3 = this.aE.get(object4);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            int n9 = 0;
            while (n9 < n6 && n < n2 && (a2 = queue.poll()) != null) {
                int n10;
                int n11;
                if (a2.c() != bl3 || (n11 = a2.a() - n7) * n11 + (n10 = a2.b() - n8) * n10 > n3 || !world.isChunkLoaded(a2.a(), a2.b())) continue;
                if (e.a()) {
                    if (object != null) {
                        Object object5 = object;
                        Object object6 = object2;
                        Object object7 = object3;
                        e.a((Plugin)this.a, world, a2.a(), a2.b(), () -> {
                            if (world.isChunkLoaded(a2.a(), a2.b())) {
                                this.a(a2.a(), a2.b(), object5, object6, object7);
                            }
                        });
                    } else {
                        this.a(player, world, a2);
                    }
                } else if (object != null) {
                    this.a(a2.a(), a2.b(), object, object2, object3);
                } else if (!this.a(player, world, a2.a(), a2.b(), null, null, null) && !this.c(player, world, a2.a(), a2.b())) {
                    if (a2.c()) {
                        this.b(player, world, a2.a(), a2.b());
                    } else {
                        this.d(player, world, a2.a(), a2.b());
                    }
                }
                ++n9;
                ++n;
            }
            if (!queue.isEmpty()) continue;
            iterator.remove();
        }
        return n;
    }

    private void a(Player player, World world, a a2) {
        Runnable runnable = () -> {
            if (a2.c()) {
                if (!this.a(player, world, a2.a(), a2.b())) {
                    this.b(player, world, a2.a(), a2.b());
                }
            } else if (!this.a(player, world, a2.a(), a2.b())) {
                this.d(player, world, a2.a(), a2.b());
            }
        };
        if (e.a()) {
            e.a((Plugin)this.a, world, a2.a(), a2.b(), runnable);
        } else {
            runnable.run();
        }
    }

    private static long a(int n, int n2) {
        return (long)n << 32 | (long)n2 & 0xFFFFFFFFL;
    }

    private static long a(int n, int n2, int n3) {
        return (long)(n & 0xFFFFFF) << 40 | (long)(n2 & 0xFFFF) << 24 | (long)(n3 & 0xFFFFFF);
    }

    private void a(Player player, int n) {
        boolean bl;
        UUID uUID = player.getUniqueId();
        this.J.add(uUID);
        this.H.remove(uUID);
        this.M.remove(uUID);
        boolean bl2 = bl = this.s > 0 && n >= this.s;
        if (bl) {
            this.S.remove(uUID);
            this.T.remove(uUID);
            this.j(player);
            this.b(player, false);
        } else {
            this.g(player);
            this.j(player);
            this.h(player);
            this.i(player);
        }
        if (this.L != null) {
            this.L.c(player);
        }
    }

    private void g(Player player) {
        UUID uUID = player.getUniqueId();
        List<int[]> list = this.l(player);
        ConcurrentHashMap.KeySetView keySetView = ConcurrentHashMap.newKeySet(list.size());
        for (int[] nArray : list) {
            keySetView.add(b.a(nArray[0], nArray[1]));
        }
        this.S.put(uUID, keySetView);
    }

    private void h(Player player) {
        Set<Long> set = this.S.get(player.getUniqueId());
        if (set == null) {
            return;
        }
        int n = player.getLocation().getBlockX() >> 4;
        int n2 = player.getLocation().getBlockZ() >> 4;
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                set.remove(b.a(n + i, n2 + j));
            }
        }
    }

    private void a(Player player, boolean bl) {
        UUID uUID = player.getUniqueId();
        Set<Long> set = this.S.get(uUID);
        if (set == null || set.isEmpty()) {
            this.S.remove(uUID);
            this.T.remove(uUID);
            return;
        }
        if (this.aH || !this.aG) {
            World world = player.getWorld();
            for (Long l : set) {
                int n;
                int n2 = (int)(l >> 32);
                if (this.c(player, world, n2, n = (int)(l & 0xFFFFFFFFL))) continue;
                this.d(player, world, n2, n);
            }
            this.S.remove(uUID);
            this.T.remove(uUID);
            return;
        }
        World world = player.getWorld();
        int n = player.getLocation().getBlockX();
        int n3 = player.getLocation().getBlockZ();
        int n4 = n >> 4;
        int n5 = n3 >> 4;
        try {
            Object object = this.aA.invoke((Object)world, new Object[0]);
            Object object2 = this.aC.invoke(object, new Object[0]);
            Object object3 = this.az.invoke((Object)player, new Object[0]);
            Object object4 = this.aE.get(object3);
            int n6 = bl ? this.Z : this.W;
            int n7 = n6 * n6;
            int n8 = bl ? this.X : this.U;
            int n9 = n8 * n8;
            for (int i = -n6; i <= n6; ++i) {
                for (int j = -n6; j <= n6; ++j) {
                    long l;
                    int n10;
                    int n11;
                    if (i * i + j * j > n7) continue;
                    int n12 = n4 + i;
                    int n13 = n5 + j;
                    int n14 = Math.max(n12 << 4, Math.min(n, (n12 << 4) + 15));
                    int n15 = n14 - n;
                    if (n15 * n15 + (n11 = (n10 = Math.max(n13 << 4, Math.min(n3, (n13 << 4) + 15))) - n3) * n11 > n9 || !set.contains(l = b.a(n12, n13))) continue;
                    if (e.a()) {
                        Object object5 = object;
                        Object object6 = object2;
                        Object object7 = object4;
                        e.a((Plugin)this.a, world, n12, n13, () -> {
                            if (world.isChunkLoaded(n12, n13)) {
                                this.a(n12, n13, object5, object6, object7);
                                set.remove(l);
                            }
                        });
                        continue;
                    }
                    if (!world.isChunkLoaded(n12, n13)) continue;
                    this.a(n12, n13, object, object2, object4);
                    set.remove(l);
                }
            }
            if (set.isEmpty()) {
                this.T.remove(uUID);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void i(Player player) {
        UUID uUID = player.getUniqueId();
        Runnable runnable = () -> {
            if (!player.isOnline()) {
                return;
            }
            if (this.H.contains(uUID)) {
                return;
            }
            if (!this.S.containsKey(uUID)) {
                return;
            }
            boolean bl = player.isGliding() || player.getGameMode() == GameMode.SPECTATOR;
            this.T.put(uUID, new long[]{player.getLocation().getBlockX(), player.getLocation().getBlockZ()});
            this.a(player, bl);
        };
        e.a((Plugin)this.a, (Entity)player, runnable, 10L);
        e.a((Plugin)this.a, (Entity)player, runnable, 30L);
    }

    private void j(Player player) {
        if (this.aH || !this.aG) {
            World world = player.getWorld();
            int n = player.getLocation().getBlockX() >> 4;
            int n2 = player.getLocation().getBlockZ() >> 4;
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    int n3 = n + i;
                    int n4 = n2 + j;
                    if (this.c(player, world, n3, n4)) continue;
                    this.d(player, world, n3, n4);
                }
            }
            return;
        }
        World world = player.getWorld();
        Location location = player.getLocation();
        int n = location.getBlockX() >> 4;
        int n5 = location.getBlockZ() >> 4;
        try {
            Object object = this.aA.invoke((Object)world, new Object[0]);
            Object object2 = this.aC.invoke(object, new Object[0]);
            Object object3 = this.az.invoke((Object)player, new Object[0]);
            Object object4 = this.aE.get(object3);
            if (e.a()) {
                Object object5 = object;
                Object object6 = object2;
                Object object7 = object4;
                e.a((Plugin)this.a, world, n, n5, () -> {
                    if (world.isChunkLoaded(n, n5)) {
                        this.a(n, n5, object5, object6, object7);
                    }
                });
            } else if (world.isChunkLoaded(n, n5)) {
                this.a(n, n5, object, object2, object4);
            }
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    if (i == 0 && j == 0) continue;
                    int n6 = n + i;
                    int n7 = n5 + j;
                    if (e.a()) {
                        Object object8 = object;
                        Object object9 = object2;
                        Object object10 = object4;
                        e.a((Plugin)this.a, world, n6, n7, () -> {
                            if (world.isChunkLoaded(n6, n7)) {
                                this.a(n6, n7, object8, object9, object10);
                            }
                        });
                        continue;
                    }
                    if (!world.isChunkLoaded(n6, n7)) continue;
                    this.a(n6, n7, object, object2, object4);
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void b(Player player, Location location) {
        if (this.aH || !this.aG) {
            this.a(player, location, false);
            return;
        }
        World world = player.getWorld();
        int n = location.getBlockX() >> 4;
        int n2 = location.getBlockZ() >> 4;
        List<int[]> list = this.a(world, location);
        list.sort(Comparator.comparingInt(nArray -> (nArray[0] - n) * (nArray[0] - n) + (nArray[1] - n2) * (nArray[1] - n2)));
        try {
            Object object = this.aA.invoke((Object)world, new Object[0]);
            Object object2 = this.aC.invoke(object, new Object[0]);
            Object object3 = this.az.invoke((Object)player, new Object[0]);
            Object object4 = this.aE.get(object3);
            for (int[] nArray2 : list) {
                int n3 = nArray2[0];
                int n4 = nArray2[1];
                if (e.a()) {
                    Object object5 = object;
                    Object object6 = object2;
                    Object object7 = object4;
                    e.a((Plugin)this.a, world, n3, n4, () -> {
                        if (world.isChunkLoaded(n3, n4)) {
                            this.a(n3, n4, object5, object6, object7);
                        }
                    });
                    continue;
                }
                if (!world.isChunkLoaded(n3, n4)) continue;
                this.a(n3, n4, object, object2, object4);
            }
        }
        catch (Exception exception) {
            this.a(player, location, false);
        }
    }

    private void k(Player player) {
        this.c(player, player.getLocation());
    }

    private void c(Player player, Location location) {
        List<int[]> list = this.a(player.getWorld(), location);
        World world = player.getWorld();
        if (e.a()) {
            Object object = null;
            Object object2 = null;
            Object object3 = null;
            if (this.aG && !this.aH) {
                try {
                    object = this.aA.invoke((Object)world, new Object[0]);
                    object2 = this.aC.invoke(object, new Object[0]);
                    Object object4 = this.az.invoke((Object)player, new Object[0]);
                    object3 = this.aE.get(object4);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            for (int[] nArray : list) {
                int n = nArray[0];
                int n2 = nArray[1];
                if (object != null) {
                    Object object5 = object;
                    Object object6 = object2;
                    Object object7 = object3;
                    e.a((Plugin)this.a, world, n, n2, () -> {
                        if (world.isChunkLoaded(n, n2)) {
                            this.a(n, n2, object5, object6, object7);
                        }
                    });
                    continue;
                }
                e.a((Plugin)this.a, world, n, n2, () -> {
                    if (!this.a(player, world, n, n2) && !this.c(player, world, n, n2)) {
                        this.d(player, world, n, n2);
                    }
                });
            }
        } else {
            Object object = null;
            Object object8 = null;
            Object object9 = null;
            if (this.aG && !this.aH) {
                try {
                    object = this.aA.invoke((Object)world, new Object[0]);
                    object8 = this.aC.invoke(object, new Object[0]);
                    Object object10 = this.az.invoke((Object)player, new Object[0]);
                    object9 = this.aE.get(object10);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            for (int[] nArray : list) {
                if (object != null) {
                    this.a(nArray[0], nArray[1], object, object8, object9);
                    continue;
                }
                if (this.a(player, world, nArray[0], nArray[1], null, null, null) || this.c(player, world, nArray[0], nArray[1])) continue;
                this.d(player, world, nArray[0], nArray[1]);
            }
        }
    }

    private void b(Player player, World world, int n, int n2) {
        if (!world.isChunkLoaded(n, n2)) {
            return;
        }
        int n3 = n << 4;
        int n4 = n2 << 4;
        int n5 = world.getMinHeight();
        int n6 = this.b;
        boolean bl = this.g && !b.c(this.e);
        int n7 = Math.floorDiv(n5, 16);
        int n8 = Math.floorDiv(n6 - 1, 16);
        for (int i = n7; i <= n8; ++i) {
            int n9 = i << 4;
            int n10 = Math.max(n9, n5);
            int n11 = Math.min(n9 + 16, n6);
            int n12 = n11 - n10 << 8;
            HashMap<Location, BlockData> hashMap = new HashMap<Location, BlockData>((int)((float)n12 / 0.75f) + 1);
            for (int j = n10; j < n11; ++j) {
                for (int k = n3; k < n3 + 16; ++k) {
                    for (int i2 = n4; i2 < n4 + 16; ++i2) {
                        if (bl && b.c(world.getBlockAt(k, j, i2).getType())) continue;
                        hashMap.put(new Location(world, (double)k, (double)j, (double)i2), this.f);
                    }
                }
            }
            if (hashMap.isEmpty()) continue;
            player.sendMultiBlockChange(hashMap);
        }
    }

    private boolean c(Player player, World world, int n, int n2) {
        if (!this.w) {
            return false;
        }
        if (!world.isChunkLoaded(n, n2)) {
            return false;
        }
        if (e.a()) {
            try {
                e.a((Plugin)this.a, world, n, n2, () -> {
                    if (!world.isChunkLoaded(n, n2)) {
                        return;
                    }
                    try {
                        this.v.invoke((Object)player, n, n2);
                    }
                    catch (Throwable throwable) {
                        // empty catch block
                    }
                });
                return true;
            }
            catch (Throwable throwable) {
                return false;
            }
        }
        try {
            this.v.invoke((Object)player, n, n2);
            return true;
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    private void d(Player player, World world, int n, int n2) {
        ChunkSnapshot chunkSnapshot;
        if (!world.isChunkLoaded(n, n2)) {
            return;
        }
        int n3 = n << 4;
        int n4 = n2 << 4;
        int n5 = world.getMinHeight();
        int n6 = this.b;
        try {
            chunkSnapshot = world.getChunkAt(n, n2).getChunkSnapshot(false, false, false);
        }
        catch (Exception exception) {
            this.a.getLogger().warning("[AntiESP] Failed to snapshot chunk " + n + "," + n2 + ": " + String.valueOf(exception));
            return;
        }
        int n7 = Math.floorDiv(n5, 16);
        int n8 = Math.floorDiv(n6 - 1, 16);
        boolean bl = b.c(this.e);
        for (int i = n7; i <= n8; ++i) {
            HashMap<Location, BlockData> hashMap = new HashMap<Location, BlockData>();
            int n9 = i << 4;
            int n10 = Math.max(n9, n5);
            int n11 = Math.min(n9 + 16, n6);
            for (int j = n10; j < n11; ++j) {
                for (int k = 0; k < 16; ++k) {
                    for (int i2 = 0; i2 < 16; ++i2) {
                        BlockData blockData = chunkSnapshot.getBlockData(k, j, i2);
                        if (bl && b.c(blockData.getMaterial())) continue;
                        hashMap.put(new Location(world, (double)(n3 + k), (double)j, (double)(n4 + i2)), blockData);
                    }
                }
            }
            if (hashMap.isEmpty()) continue;
            player.sendMultiBlockChange(hashMap);
        }
    }

    private void b(Player player, boolean bl) {
        this.a(player, player.getLocation(), bl);
    }

    private void a(Player player, Location location, boolean bl) {
        UUID uUID = player.getUniqueId();
        this.M.remove(uUID);
        List<int[]> list = this.a(player.getWorld(), location);
        int n = location.getBlockX() >> 4;
        int n2 = location.getBlockZ() >> 4;
        list.sort(Comparator.comparingInt(nArray -> (nArray[0] - n) * (nArray[0] - n) + (nArray[1] - n2) * (nArray[1] - n2)));
        ConcurrentLinkedQueue<a> concurrentLinkedQueue = new ConcurrentLinkedQueue<a>();
        for (int[] nArray2 : list) {
            if (!bl) {
                int n3 = nArray2[0] - n;
                int n4 = nArray2[1] - n2;
                if (n3 >= -1 && n3 <= 1 && n4 >= -1 && n4 <= 1) continue;
            }
            concurrentLinkedQueue.add(new a(nArray2[0], nArray2[1], bl));
        }
        if (!concurrentLinkedQueue.isEmpty()) {
            this.M.put(uUID, concurrentLinkedQueue);
            this.l();
        }
    }

    private List<int[]> l(Player player) {
        return this.a(player.getWorld(), player.getLocation());
    }

    private List<int[]> a(World world, Location location) {
        int n;
        int n2 = location.getBlockX() >> 4;
        int n3 = location.getBlockZ() >> 4;
        this.av = n = Math.max(this.av, Bukkit.getViewDistance());
        int n4 = Math.max(this.d, n + 4);
        int n5 = n4 * n4;
        ArrayList<int[]> arrayList = new ArrayList<int[]>((int)(Math.PI * (double)n4 * (double)n4) + 1);
        for (int i = n2 - n4; i <= n2 + n4; ++i) {
            for (int j = n3 - n4; j <= n3 + n4; ++j) {
                int n6 = i - n2;
                int n7 = j - n3;
                if (n6 * n6 + n7 * n7 > n5 || !world.isChunkLoaded(i, j)) continue;
                arrayList.add(new int[]{i, j});
            }
        }
        return arrayList;
    }

    private void m(Player player) {
        int n;
        int n2;
        int n3;
        int n4;
        int n5;
        int n6;
        boolean bl;
        World world = player.getWorld();
        int n7 = world.getMinHeight();
        int n8 = world.getMaxHeight();
        int n9 = (n8 - n7) / 16;
        boolean bl2 = this.j.contains(world.getName());
        boolean bl3 = bl = !bl2 && this.i.contains(world.getEnvironment());
        if (bl && this.b > n7) {
            n6 = this.b - n7;
            n5 = n6 / 16;
            n4 = n6 % 16;
        } else {
            n5 = 0;
            n4 = 0;
        }
        int n10 = n6 = this.k && !bl2 ? Math.max(0, (int)Math.ceil((double)(this.c(world) - n7) / 16.0)) : 0;
        if (this.k && !bl2 && this.s > 0 && this.s > n7) {
            n3 = this.s - n7;
            n2 = n3 / 16;
            n = n3 % 16;
        } else {
            n2 = -1;
            n = 0;
        }
        n3 = world.getEnvironment().ordinal();
        this.aa.put(player.getUniqueId(), new int[]{n9, n5, n6, n7, n3, n2, n4, n});
    }

    private Object b(Object object, Object object2) {
        Object[] objectArray;
        if (this.aL == null) {
            objectArray = this.aD.getParameterTypes();
            Object[] objectArray2 = new Object[objectArray.length];
            for (int i = 2; i < objectArray.length; ++i) {
                Object object3 = objectArray[i];
                objectArray2[i] = object3 == Boolean.TYPE ? Boolean.valueOf(false) : (object3 == Integer.TYPE ? Integer.valueOf(0) : (object3 == Long.TYPE ? Long.valueOf(0L) : (object3 == Byte.TYPE ? Byte.valueOf((byte)0) : (object3 == Short.TYPE ? Short.valueOf((short)0) : (object3 == Float.TYPE ? Float.valueOf(0.0f) : (object3 == Double.TYPE ? (Constable)Double.valueOf(0.0) : (Constable)(object3 == Character.TYPE ? Character.valueOf('\u0000') : null)))))));
            }
            this.aL = objectArray2;
        }
        if ((objectArray = this.aM.get()) == null || objectArray.length != this.aL.length) {
            objectArray = (Object[])this.aL.clone();
            this.aM.set(objectArray);
        }
        objectArray[0] = object;
        objectArray[1] = object2;
        return this.aD.newInstance(objectArray);
    }

    private boolean a(World world) {
        if (this.j.contains(world.getName())) {
            return false;
        }
        return this.i.contains(world.getEnvironment());
    }

    private boolean b(World world) {
        return this.j.contains(world.getName());
    }

    private int c(World world) {
        if (world.getEnvironment() == World.Environment.NETHER) {
            if (!this.n) {
                return world.getMinHeight();
            }
            return this.m;
        }
        return this.l;
    }

    private static Set<Material> a(Set<Material> set) {
        EnumSet<Material> enumSet = EnumSet.noneOf(Material.class);
        for (Material material : set) {
            if (aN.contains(material)) continue;
            enumSet.add(material);
        }
        return enumSet;
    }

    private static boolean c(Material material) {
        return switch (material) {
            case Material.AIR, Material.CAVE_AIR, Material.VOID_AIR -> true;
            default -> false;
        };
    }

    private int[] a(boolean[] blArray, Material material, String string, Material material2, String string2) {
        int[] nArray = new int[blArray.length];
        Arrays.fill(nArray, -1);
        this.a(nArray, blArray, material, string);
        this.a(nArray, blArray, material2, string2);
        return nArray;
    }

    private void a(int[] nArray, boolean[] blArray, Material material, String string) {
        if (string == null) {
            string = "DEFAULT";
        }
        string = string.trim().toUpperCase();
        List<Integer> list = this.b(material);
        if (string.equals("NONE")) {
            for (int n : list) {
                if (n < 0 || n >= blArray.length) continue;
                blArray[n] = false;
            }
            return;
        }
        if (string.equals("DEFAULT")) {
            return;
        }
        Material material2 = Material.matchMaterial((String)string);
        if (material2 == null) {
            return;
        }
        int n = this.a(material2);
        for (int n2 : list) {
            if (n2 < 0 || n2 >= nArray.length) continue;
            nArray[n2] = n;
        }
    }

    private static /* synthetic */ Queue lambda$enqueue$28(UUID uUID) {
        return new ConcurrentLinkedQueue();
    }

    static {
        aK = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
        aN = EnumSet.of(Material.LAVA, Material.PISTON, Material.STICKY_PISTON, Material.PISTON_HEAD);
    }

    record a(int a, int b, boolean c) {
        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{a.class, "cx;cz;mask", "a", "b", "c"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{a.class, "cx;cz;mask", "a", "b", "c"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{a.class, "cx;cz;mask", "a", "b", "c"}, this, object);
        }
    }
}

