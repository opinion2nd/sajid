/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.HandlerList
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 */
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class g
implements Listener {
    private final JavaPlugin a;
    private final String b;
    private final boolean c;
    private final HttpClient d;
    private volatile String e = null;
    private volatile boolean f = false;

    public g(JavaPlugin javaPlugin, boolean bl) {
        this.a = javaPlugin;
        this.b = javaPlugin.getDescription().getVersion();
        this.c = bl;
        this.d = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();
    }

    public void a() {
        if ("eH8CnDKCeGXFrh+VhNEf1j4rqfuMuwsy".isEmpty()) {
            return;
        }
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)this.a);
        this.b();
    }

    public void b() {
        CompletableFuture.runAsync(this::d);
    }

    public void c() {
        try {
            HandlerList.unregisterAll((Listener)this);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            this.d.close();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void d() {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create("https://api.builtbybit.com/v1/resources/98233/versions/latest")).header("Authorization", "Shared eH8CnDKCeGXFrh+VhNEf1j4rqfuMuwsy").GET().timeout(Duration.ofSeconds(15L)).build();
            HttpResponse<String> httpResponse = this.d.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (httpResponse.statusCode() != 200) {
                return;
            }
            String string = httpResponse.body();
            String string2 = g.a(string);
            if (string2 == null) {
                return;
            }
            String string3 = g.b(string2);
            String string4 = g.b(this.b);
            this.e = string3;
            this.f = !string4.equals(string3);
        }
        catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
        if (!this.f || !this.c) {
            return;
        }
        Player player = playerJoinEvent.getPlayer();
        if (!player.hasPermission("antiesp.reload")) {
            return;
        }
        e.a((Plugin)this.a, (Entity)player, () -> {
            if (player.isOnline() && this.f) {
                player.sendMessage("\u00a77[\u00a76AntiESPFreecam\u00a77] \u00a7eA new version is available: \u00a7f" + this.e + " \u00a77(current: \u00a7f" + this.b + "\u00a77)");
                player.sendMessage("\u00a77[\u00a76AntiESPFreecam\u00a77] \u00a7eDownload: \u00a7fhttps://builtbybit.com/resources/98233");
            }
        }, 60L);
    }

    private static String a(String string) {
        int n = string.indexOf("\"data\"");
        if (n == -1) {
            return null;
        }
        int n2 = string.indexOf("\"name\"", n);
        if (n2 == -1) {
            return null;
        }
        int n3 = string.indexOf(58, n2);
        if (n3 == -1) {
            return null;
        }
        int n4 = string.indexOf(34, n3 + 1);
        if (n4 == -1) {
            return null;
        }
        int n5 = string.indexOf(34, n4 + 1);
        if (n5 == -1) {
            return null;
        }
        return string.substring(n4 + 1, n5);
    }

    private static String b(String string) {
        String string2 = string.strip().toLowerCase();
        if (string2.startsWith("v.")) {
            string2 = string2.substring(2);
        } else if (string2.startsWith("v")) {
            string2 = string2.substring(1);
        }
        while (string2.endsWith(".")) {
            string2 = string2.substring(0, string2.length() - 1);
        }
        return string2;
    }
}

