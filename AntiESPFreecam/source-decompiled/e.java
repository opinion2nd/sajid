/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.World
 *  org.bukkit.entity.Entity
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitTask
 */
import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.runtime.ObjectMethods;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class e {
    private static final boolean a;
    private static final boolean b;
    private static final boolean c;
    private static final boolean d;
    private static final boolean e;
    private static volatile boolean f;
    private static Object g;
    private static Object h;
    private static Method i;
    private static Method j;
    private static Method k;
    private static Method l;
    private static Method m;
    private static Method n;
    private static Method o;

    private static boolean f() {
        String[] stringArray = new String[]{"org.dreeam.leaf.config.LeafGlobalConfig", "org.dreeam.leaf.LeafConfig", "org.dreeam.leaf.LeafBootstrap"};
        for (String string : stringArray) {
            try {
                Class.forName(string);
                return true;
            }
            catch (ClassNotFoundException classNotFoundException) {
            }
        }
        try {
            String string = Bukkit.getName();
            String string2 = Bukkit.getVersion();
            if (string != null && string.toLowerCase().contains("leaf") || string2 != null && string2.toLowerCase().contains("leaf")) {
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    private static a g() {
        File file = new File("config" + File.separator + "leaf-global.yml");
        if (!file.exists() || !file.isFile()) {
            return new a(false, false, false);
        }
        boolean bl = false;
        boolean bl2 = false;
        boolean bl3 = false;
        try {
            List<String> list = Files.readAllLines(file.toPath());
            String string = "";
            String string2 = "";
            for (String string3 : list) {
                int n;
                String string4 = string3;
                int n2 = string4.indexOf(35);
                if (n2 >= 0) {
                    string4 = string4.substring(0, n2);
                }
                if (string4.isBlank()) continue;
                for (n = 0; n < string4.length() && string4.charAt(n) == ' '; ++n) {
                }
                String string5 = string4.trim();
                int n3 = string5.indexOf(58);
                if (n3 < 0) continue;
                String string6 = string5.substring(0, n3).trim();
                String string7 = string5.substring(n3 + 1).trim();
                if (n == 0) {
                    string = string6;
                    string2 = "";
                    continue;
                }
                if (n == 2) {
                    string2 = string6;
                    continue;
                }
                if (n < 4 || !"async".equals(string) || !"enabled".equals(string6)) continue;
                boolean bl4 = "true".equalsIgnoreCase(string7);
                switch (string2) {
                    case "async-chunk-send": {
                        bl = bl4;
                        break;
                    }
                    case "async-entity-tracker": {
                        bl2 = bl4;
                        break;
                    }
                    case "parallel-world-ticking": {
                        bl3 = bl4;
                        break;
                    }
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return new a(bl, bl2, bl3);
    }

    private e() {
    }

    public static boolean a() {
        return a;
    }

    public static boolean b() {
        return b;
    }

    public static boolean c() {
        return b && c;
    }

    public static boolean d() {
        return b && d;
    }

    public static boolean e() {
        return b && e;
    }

    public static Object a(Plugin plugin, Runnable runnable, long l, long l2) {
        if (a) {
            e.h();
            if (i != null) {
                try {
                    Consumer<Object> consumer = object -> runnable.run();
                    return i.invoke(g, plugin, consumer, l, l2);
                }
                catch (Exception exception) {
                    throw new RuntimeException("Folia runAtFixedRate failed", exception);
                }
            }
        }
        return Bukkit.getScheduler().runTaskTimer(plugin, runnable, l, l2);
    }

    public static void a(Object object) {
        if (object == null) {
            return;
        }
        if (object instanceof BukkitTask) {
            BukkitTask bukkitTask = (BukkitTask)object;
            bukkitTask.cancel();
            return;
        }
        if (a) {
            e.h();
            if (o != null) {
                try {
                    o.invoke(object, new Object[0]);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
    }

    public static void a(Plugin plugin, World world, int n, int n2, Runnable runnable) {
        if (a) {
            e.h();
            if (k != null) {
                try {
                    k.invoke(h, plugin, world, n, n2, runnable);
                    return;
                }
                catch (Exception exception) {
                    throw new RuntimeException("Folia region execute failed", exception);
                }
            }
        }
        runnable.run();
    }

    public static void a(Plugin plugin, Entity entity, Runnable runnable) {
        if (a) {
            e.h();
            if (l != null && m != null) {
                try {
                    Object object2 = l.invoke((Object)entity, new Object[0]);
                    Consumer<Object> consumer = object -> runnable.run();
                    m.invoke(object2, plugin, consumer, null);
                    return;
                }
                catch (Exception exception) {
                    throw new RuntimeException("Folia entity scheduler failed", exception);
                }
            }
        }
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public static void a(Plugin plugin, Entity entity, Runnable runnable, long l) {
        if (a) {
            e.h();
            if (e.l != null && n != null) {
                try {
                    Object object2 = e.l.invoke((Object)entity, new Object[0]);
                    Consumer<Object> consumer = object -> runnable.run();
                    n.invoke(object2, plugin, consumer, null, l);
                    return;
                }
                catch (Exception exception) {
                    throw new RuntimeException("Folia entity scheduler runDelayed failed", exception);
                }
            }
        }
        Bukkit.getScheduler().runTaskLater(plugin, runnable, l);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void h() {
        if (f) {
            return;
        }
        Class<e> clazz = e.class;
        synchronized (e.class) {
            if (f) {
                // ** MonitorExit[var0] (shouldn't be in output)
                return;
            }
            f = true;
            try {
                Method method = Bukkit.class.getMethod("getGlobalRegionScheduler", new Class[0]);
                g = method.invoke(null, new Object[0]);
                for (Method method2 : g.getClass().getMethods()) {
                    if (method2.getName().equals("runAtFixedRate") && method2.getParameterCount() == 4) {
                        i = method2;
                        continue;
                    }
                    if (!method2.getName().equals("execute") || method2.getParameterCount() != 2 || method2.getParameterTypes()[1] != Runnable.class) continue;
                    j = method2;
                }
                Method method3 = Bukkit.class.getMethod("getRegionScheduler", new Class[0]);
                h = method3.invoke(null, new Object[0]);
                for (Method method4 : h.getClass().getMethods()) {
                    if (!method4.getName().equals("execute") || method4.getParameterCount() != 5 || method4.getParameterTypes()[1] != World.class || method4.getParameterTypes()[2] != Integer.TYPE) continue;
                    k = method4;
                    break;
                }
                l = Entity.class.getMethod("getScheduler", new Class[0]);
                Class<?> clazz2 = l.getReturnType();
                for (Method method5 : clazz2.getMethods()) {
                    if (method5.getName().equals("run") && method5.getParameterCount() == 3) {
                        m = method5;
                        continue;
                    }
                    if (!method5.getName().equals("runDelayed") || method5.getParameterCount() != 4) continue;
                    n = method5;
                }
                Class<?> clazz3 = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
                o = clazz3.getMethod("cancel", new Class[0]);
            }
            catch (Exception exception) {
                // empty catch block
            }
            return;
        }
    }

    static {
        boolean bl;
        boolean bl2;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            bl2 = true;
        }
        catch (ClassNotFoundException classNotFoundException) {
            bl2 = false;
        }
        a = bl2;
        b = bl = e.f();
        boolean bl3 = false;
        boolean bl4 = false;
        boolean bl5 = false;
        if (bl) {
            a a2 = e.g();
            bl3 = a2.a;
            bl4 = a2.b;
            bl5 = a2.c;
        }
        c = bl3;
        d = bl4;
        e = bl5;
    }

    static final class a
    extends Record {
        final boolean a;
        final boolean b;
        final boolean c;

        a(boolean bl, boolean bl2, boolean bl3) {
            this.a = bl;
            this.b = bl2;
            this.c = bl3;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{a.class, "asyncChunkSend;asyncEntityTracker;parallelWorldTicking", "a", "b", "c"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{a.class, "asyncChunkSend;asyncEntityTracker;parallelWorldTicking", "a", "b", "c"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{a.class, "asyncChunkSend;asyncEntityTracker;parallelWorldTicking", "a", "b", "c"}, this, object);
        }
    }
}

