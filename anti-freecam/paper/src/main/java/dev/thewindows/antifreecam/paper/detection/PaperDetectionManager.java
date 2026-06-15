package dev.thewindows.antifreecam.paper.detection;

import dev.thewindows.antifreecam.common.detection.DetectionConfig;
import dev.thewindows.antifreecam.common.detection.DetectionResult;
import dev.thewindows.antifreecam.common.detection.FreecamDetector;
import dev.thewindows.antifreecam.paper.effect.VoidChunkInjector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class PaperDetectionManager {

    private final Plugin plugin;
    private final FreecamDetector detector;
    private final VoidChunkInjector injector;
    private final DetectionConfig config;
    private final boolean notifyAdmins;
    private final String notifyPermission;
    private BukkitTask evaluationTask;
    private BukkitTask voidRecheckTask;

    public PaperDetectionManager(Plugin plugin, FreecamDetector detector,
                                  VoidChunkInjector injector, DetectionConfig config,
                                  boolean notifyAdmins, String notifyPermission) {
        this.plugin = plugin;
        this.detector = detector;
        this.injector = injector;
        this.config = config;
        this.notifyAdmins = notifyAdmins;
        this.notifyPermission = notifyPermission;
    }

    public void start() {
        int evalInterval = config.getEvaluationIntervalTicks();
        evaluationTask = Bukkit.getScheduler().runTaskTimer(plugin, this::evaluateAll, evalInterval, evalInterval);

        int recheckInterval = config.getVoidRecheckIntervalTicks();
        voidRecheckTask = Bukkit.getScheduler().runTaskTimer(plugin, this::recheckVoid, recheckInterval, recheckInterval);
    }

    public void stop() {
        if (evaluationTask != null) evaluationTask.cancel();
        if (voidRecheckTask != null) voidRecheckTask.cancel();
    }

    private void evaluateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            DetectionResult result = detector.evaluate(uuid);

            if (result.detected()) {
                if (!injector.hasVoidEffect(uuid)) {
                    injector.applyVoidEffect(player);
                    plugin.getLogger().info("[AntiFreeam] Flagged " + player.getName() +
                        " as freecam user (confidence=" + String.format("%.2f", result.confidence()) + ")");

                    if (notifyAdmins && result.confidence() >= config.getAdminNotifyConfidenceThreshold()) {  // both double, no cast needed
                        notifyAdmins(player, result);
                    }
                }
            } else if (injector.hasVoidEffect(uuid)) {
                injector.removeVoidEffect(player);
                plugin.getLogger().info("[AntiFreeam] Cleared freecam flag for " + player.getName());
            }
        }
    }

    private void recheckVoid() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            injector.recheckActive(player);
        }
    }

    private void notifyAdmins(Player flagged, DetectionResult result) {
        Component msg = Component.text("[AntiFreeam] ", NamedTextColor.RED)
            .append(Component.text(flagged.getName(), NamedTextColor.YELLOW))
            .append(Component.text(" may be using freecam (confidence=" +
                String.format("%.0f%%", result.confidence() * 100) + ")", NamedTextColor.RED));

        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission(notifyPermission)) {
                admin.sendMessage(msg);
            }
        }
    }
}
