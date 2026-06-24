package dev.opinion2nd.antiespguard.common;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Minimal Discord webhook poster used to report cheat-mod detections.
 * Platform-agnostic: only the JDK HTTP client is used.
 */
public final class WebhookClient {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private WebhookClient() {
    }

    /**
     * Fire a Discord embed describing a detection. Returns silently on any
     * failure (network/HTTP) — detection reporting must never break gameplay.
     *
     * @param webhookUrl Discord webhook URL ({@code ""} disables)
     * @param title      embed title
     * @param description embed description
     * @param color      embed colour (decimal RGB)
     */
    public static void postEmbed(String webhookUrl, String title, String description, int color) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }
        String json = "{\"embeds\":[{"
                + "\"title\":" + jsonString(title) + ","
                + "\"description\":" + jsonString(description) + ","
                + "\"color\":" + color
                + "}]}";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HTTP.sendAsync(req, HttpResponse.BodyHandlers.discarding());
    }

    private static String jsonString(String s) {
        if (s == null) {
            return "\"\"";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }
}
