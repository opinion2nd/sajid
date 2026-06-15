package dev.thewindows.antifreecam.common.license;

import java.time.Instant;

public record LicenseResult(
    LicenseStatus status,
    String message,
    Instant expiresAt,
    String boundServerId
) {
    public static LicenseResult networkError(String message) {
        return new LicenseResult(LicenseStatus.NETWORK_ERROR, message, null, null);
    }

    public static LicenseResult invalid(String message) {
        return new LicenseResult(LicenseStatus.INVALID, message, null, null);
    }
}
