package dev.thewindows.antifreecam.paper.license;

import dev.thewindows.antifreecam.common.license.LicenseResult;
import dev.thewindows.antifreecam.common.license.LicenseStatus;
import dev.thewindows.antifreecam.common.license.LicenseValidator;
import dev.thewindows.antifreecam.common.license.ServerIdentity;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.nio.file.Path;

public class PaperLicenseBootstrap {

    private final JavaPlugin plugin;
    private LicenseValidator validator;
    private ServerIdentity serverIdentity;
    private String licenseKey;
    private String pluginVersion;
    private BukkitTask revalidateTask;

    public PaperLicenseBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void validate() {
        FileConfiguration config = plugin.getConfig();

        if (!config.getBoolean("license.enabled", false)) {
            plugin.getLogger().info("[AntiFreeam] License check disabled — running standalone.");
            return;
        }

        String apiUrl = config.getString("license.api-url", "");
        licenseKey = config.getString("license.key", "");
        String failBehavior = config.getString("license.fail-behavior", "CLOSE");
        int revalidateHours = config.getInt("license.revalidate-interval-hours", 6);
        pluginVersion = plugin.getDescription().getVersion();

        if (licenseKey.isBlank() || licenseKey.equals("YOUR-AFC-LICENSE-KEY-HERE")) {
            throw new LicenseException(
                "No license key configured. Set 'license.key' in config.yml. " +
                "Contact the plugin author to obtain a license key."
            );
        }

        if (apiUrl.isBlank() || apiUrl.equals("https://license.example.com")) {
            throw new LicenseException(
                "License API URL not configured. Set 'license.api-url' in config.yml."
            );
        }

        validator = new LicenseValidator(apiUrl);
        Path dataFolder = plugin.getDataFolder().toPath();
        serverIdentity = new ServerIdentity(dataFolder);

        String serverId;
        try {
            serverId = serverIdentity.getOrCreate();
        } catch (IOException e) {
            throw new LicenseException("Failed to read/create server identity: " + e.getMessage());
        }

        LicenseResult result = validator.validate(licenseKey, serverId, pluginVersion);
        handleResult(result, failBehavior);

        // Schedule periodic re-validation
        long intervalTicks = revalidateHours * 20L * 60 * 60;
        revalidateTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
            plugin,
            () -> periodicRevalidate(failBehavior),
            intervalTicks, intervalTicks
        );
    }

    private void periodicRevalidate(String failBehavior) {
        try {
            String serverId = serverIdentity.getOrCreate();
            LicenseResult result = validator.validate(licenseKey, serverId, pluginVersion);
            if (result.status() != LicenseStatus.VALID) {
                plugin.getLogger().severe("[AntiFreeam] License re-validation failed: " + result.message());
                if (failBehavior.equalsIgnoreCase("CLOSE")) {
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                        plugin.getServer().getPluginManager().disablePlugin(plugin)
                    );
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("[AntiFreeam] Re-validation IO error: " + e.getMessage());
        }
    }

    private void handleResult(LicenseResult result, String failBehavior) {
        switch (result.status()) {
            case VALID -> plugin.getLogger().info("[AntiFreeam] License validated successfully.");
            case MISMATCH -> throw new LicenseException(
                "License key is bound to a different server (UUID mismatch). " +
                "Purchase a new key or contact the plugin author."
            );
            case EXPIRED -> throw new LicenseException("License key has expired. Please renew your license.");
            case INVALID -> throw new LicenseException("Invalid license key: " + result.message());
            case NETWORK_ERROR -> {
                plugin.getLogger().warning("[AntiFreeam] License server unreachable: " + result.message());
                if (failBehavior.equalsIgnoreCase("CLOSE")) {
                    throw new LicenseException(
                        "Cannot reach license server and fail-behavior is CLOSE. " +
                        "Check your internet connection or set fail-behavior: OPEN to allow offline use."
                    );
                }
                plugin.getLogger().warning("[AntiFreeam] Running in OPEN mode — license not verified!");
            }
        }
    }

    public void shutdown() {
        if (revalidateTask != null) {
            revalidateTask.cancel();
        }
    }
}
