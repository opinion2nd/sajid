package dev.thewindows.antifreecam.common.license;

import com.google.gson.JsonObject;
import dev.thewindows.antifreecam.common.util.HttpClient;
import dev.thewindows.antifreecam.common.util.JsonUtil;

import java.io.IOException;
import java.time.Instant;

/**
 * Validates license keys against the license-server's {@code /api/v1/validate} endpoint.
 *
 * <p>This class and the rest of the {@code common.util}/{@code common.license} package are
 * product-agnostic — {@code product} is just a string the caller supplies. Future plugins can
 * either depend on this {@code common} Gradle module directly and call {@link #validate} with
 * their own product slug, or copy this file plus {@link HttpClient} and {@link JsonUtil}
 * verbatim into a standalone codebase that has no Gradle dependency on this module.
 */
public class LicenseValidator {

    private final HttpClient httpClient;
    private final String apiBaseUrl;

    public LicenseValidator(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl.replaceAll("/$", "");
        this.httpClient = new HttpClient(10);
    }

    public LicenseResult validate(String licenseKey, String product, String serverId, String pluginVersion) {
        JsonObject body = new JsonObject();
        body.addProperty("key", licenseKey);
        body.addProperty("product", product);
        body.addProperty("serverId", serverId);
        body.addProperty("pluginVersion", pluginVersion);

        HttpClient.Response response;
        try {
            response = httpClient.post(apiBaseUrl + "/api/v1/validate", JsonUtil.toJson(body));
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return LicenseResult.networkError("Cannot reach license server: " + e.getMessage());
        }

        if (response.statusCode() != 200) {
            return LicenseResult.networkError("Unexpected HTTP " + response.statusCode() + " from license server");
        }

        return parseResponse(response.body());
    }

    private LicenseResult parseResponse(String json) {
        try {
            JsonObject obj = JsonUtil.parseObject(json);
            String statusStr = JsonUtil.getString(obj, "status", "INVALID");
            String message = JsonUtil.getString(obj, "message", "");
            String boundServerId = JsonUtil.getString(obj, "boundServerId", null);

            LicenseStatus status;
            try {
                status = LicenseStatus.valueOf(statusStr);
            } catch (IllegalArgumentException e) {
                status = LicenseStatus.INVALID;
            }

            Instant expiresAt = null;
            if (obj.has("expiresAt") && !obj.get("expiresAt").isJsonNull()) {
                expiresAt = Instant.parse(obj.get("expiresAt").getAsString());
            }

            return new LicenseResult(status, message, expiresAt, boundServerId);
        } catch (Exception e) {
            return LicenseResult.networkError("Failed to parse license server response: " + e.getMessage());
        }
    }
}
