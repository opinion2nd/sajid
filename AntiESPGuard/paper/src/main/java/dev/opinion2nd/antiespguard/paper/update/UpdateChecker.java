package dev.opinion2nd.antiespguard.paper.update;

import dev.opinion2nd.antiespguard.common.UpdateCheck;
import dev.opinion2nd.antiespguard.paper.util.Schedulers;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

/**
 * Asynchronously checks for a newer release on startup and (optionally) notifies
 * ops when they join. Backed by the platform-agnostic {@link UpdateCheck}.
 */
public final class UpdateChecker implements Listener {

    private static final String RELEASES_API =
            "https://api.github.com/repos/opinion2nd/AntiESPGuard/releases/latest";

    private final Plugin plugin;
    private final boolean notifyOps;
    private volatile String latestVersion; // non-null only when an update exists

    public UpdateChecker(Plugin plugin, boolean notifyOps) {
        this.plugin = plugin;
        this.notifyOps = notifyOps;
    }

    /** Kick off the check off the main thread. */
    public void start() {
        String current = plugin.getDescription().getVersion();
        Schedulers.runAsyncRepeating(plugin, () -> {
            UpdateCheck.Result result = UpdateCheck.check(RELEASES_API, current);
            if (result.updateAvailable()) {
                latestVersion = result.latest();
                plugin.getLogger().info("A new version is available: "
                        + result.latest() + " (running " + current + ").");
            }
        }, 20L * 5, 20L * 60 * 60 * 12); // 5s after start, then every 12h
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!notifyOps) {
            return;
        }
        String latest = latestVersion;
        if (latest == null) {
            return;
        }
        Player player = event.getPlayer();
        if (player.isOp() || player.hasPermission("antiespguard.reload")) {
            player.sendMessage(ChatColor.AQUA + "[AntiESPGuard] " + ChatColor.YELLOW
                    + "Update available: " + latest
                    + " (running " + plugin.getDescription().getVersion() + ").");
        }
    }
}
