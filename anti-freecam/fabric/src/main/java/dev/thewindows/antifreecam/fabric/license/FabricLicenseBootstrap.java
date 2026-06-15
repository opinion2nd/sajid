package dev.thewindows.antifreecam.fabric.license;

import dev.thewindows.antifreecam.common.license.LicenseResult;
import dev.thewindows.antifreecam.common.license.LicenseStatus;
import dev.thewindows.antifreecam.common.license.LicenseValidator;
import dev.thewindows.antifreecam.common.license.ServerIdentity;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class FabricLicenseBootstrap {

    private static final String CONFIG_FILE = "antifreecam.properties";

    public void validate() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("antifreecam");
        Path configFile = configDir.resolve(CONFIG_FILE);

        Properties props = loadOrCreateConfig(configDir, configFile);
        String licenseKey = props.getProperty("license.key", "");
        String apiUrl = props.getProperty("license.api-url", "");
        String failBehavior = props.getProperty("license.fail-behavior", "CLOSE");

        if (licenseKey.isBlank() || licenseKey.equals("YOUR-AFC-LICENSE-KEY-HERE")) {
            throw new IllegalStateException(
                "[AntiFreeam] No license key configured. Edit config/antifreecam/" + CONFIG_FILE
            );
        }

        ServerIdentity identity = new ServerIdentity(configDir);
        String serverId;
        try {
            serverId = identity.getOrCreate();
        } catch (IOException e) {
            throw new IllegalStateException("[AntiFreeam] Failed to read server identity: " + e.getMessage());
        }

        LicenseValidator validator = new LicenseValidator(apiUrl);
        LicenseResult result = validator.validate(licenseKey, serverId, "1.0.0");
        handleResult(result, failBehavior);
    }

    private void handleResult(LicenseResult result, String failBehavior) {
        switch (result.status()) {
            case VALID -> System.out.println("[AntiFreeam] License validated successfully.");
            case MISMATCH -> throw new IllegalStateException(
                "[AntiFreeam] License key is bound to a different server UUID."
            );
            case EXPIRED -> throw new IllegalStateException("[AntiFreeam] License key has expired.");
            case INVALID -> throw new IllegalStateException("[AntiFreeam] Invalid license key: " + result.message());
            case NETWORK_ERROR -> {
                System.out.println("[AntiFreeam] WARNING: Cannot reach license server: " + result.message());
                if (failBehavior.equalsIgnoreCase("CLOSE")) {
                    throw new IllegalStateException(
                        "[AntiFreeam] License server unreachable and fail-behavior is CLOSE."
                    );
                }
            }
        }
    }

    private Properties loadOrCreateConfig(Path dir, Path file) {
        Properties props = new Properties();
        if (!Files.exists(file)) {
            try {
                Files.createDirectories(dir);
                props.setProperty("license.key", "YOUR-AFC-LICENSE-KEY-HERE");
                props.setProperty("license.api-url", "https://license.example.com");
                props.setProperty("license.fail-behavior", "CLOSE");
                try (var out = Files.newOutputStream(file)) {
                    props.store(out, "AntiFreeam License Configuration");
                }
            } catch (IOException e) {
                throw new IllegalStateException("[AntiFreeam] Failed to create config: " + e.getMessage());
            }
            return props;
        }
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("[AntiFreeam] Failed to read config: " + e.getMessage());
        }
        return props;
    }
}
