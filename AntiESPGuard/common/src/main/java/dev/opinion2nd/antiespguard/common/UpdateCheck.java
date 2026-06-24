package dev.opinion2nd.antiespguard.common;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Platform-agnostic update check. Fetches the latest release tag from a
 * GitHub-style releases API and compares it to the running version.
 */
public final class UpdateCheck {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Matches "tag_name":"v1.2.3" (and without the leading v).
    private static final Pattern TAG = Pattern.compile("\"tag_name\"\\s*:\\s*\"v?([0-9][0-9.]*)\"");

    private UpdateCheck() {
    }

    /** Result of a check; {@code latest} is null when the lookup failed. */
    public record Result(String current, String latest, boolean updateAvailable) {
    }

    /**
     * Synchronously query {@code apiUrl} (a GitHub releases/latest endpoint) and
     * compare against {@code currentVersion}. Never throws — returns a result
     * with {@code latest == null} on any error.
     */
    public static Result check(String apiUrl, String currentVersion) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "AntiESPGuard")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                return new Result(currentVersion, null, false);
            }
            Matcher m = TAG.matcher(resp.body());
            if (!m.find()) {
                return new Result(currentVersion, null, false);
            }
            String latest = m.group(1);
            return new Result(currentVersion, latest, isNewer(latest, currentVersion));
        } catch (Exception e) {
            return new Result(currentVersion, null, false);
        }
    }

    /** True if {@code a} is a strictly newer dotted version than {@code b}. */
    public static boolean isNewer(String a, String b) {
        String[] pa = a.split("\\.");
        String[] pb = b.split("\\.");
        int n = Math.max(pa.length, pb.length);
        for (int i = 0; i < n; i++) {
            int x = i < pa.length ? parse(pa[i]) : 0;
            int y = i < pb.length ? parse(pb[i]) : 0;
            if (x != y) {
                return x > y;
            }
        }
        return false;
    }

    private static int parse(String s) {
        try {
            return Integer.parseInt(s.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
