package com.ultimatedungeon.util;

import org.jetbrains.annotations.NotNull;

/**
 * Utility for time formatting and conversion.
 */
public final class TimeUtil {

    private TimeUtil() {}

    /**
     * Formats a millisecond duration as {@code mm:ss}.
     *
     * @param ms duration in milliseconds
     * @return formatted string, e.g. {@code "05:43"}
     */
    @NotNull
    public static String formatMmSs(final long ms) {
        final long totalSeconds = ms / 1_000L;
        final long minutes      = totalSeconds / 60L;
        final long seconds      = totalSeconds % 60L;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Formats a millisecond duration as a human-readable string.
     * E.g. {@code "2h 5m 30s"}, {@code "45s"}.
     *
     * @param ms duration in milliseconds
     * @return human-readable string
     */
    @NotNull
    public static String formatHuman(final long ms) {
        long remaining = ms / 1_000L;
        final long hours   = remaining / 3_600L; remaining %= 3_600L;
        final long minutes = remaining / 60L;    remaining %= 60L;
        final long seconds = remaining;

        final StringBuilder sb = new StringBuilder();
        if (hours > 0)   sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");
        return sb.toString().trim();
    }

    /**
     * Converts seconds to Bukkit ticks (1 second = 20 ticks).
     *
     * @param seconds seconds
     * @return ticks
     */
    public static long secondsToTicks(final long seconds) {
        return seconds * 20L;
    }

    /**
     * Converts Bukkit ticks to milliseconds.
     *
     * @param ticks ticks
     * @return milliseconds
     */
    public static long ticksToMillis(final long ticks) {
        return ticks * 50L;
    }

    /**
     * Returns the current epoch millisecond timestamp.
     *
     * @return {@link System#currentTimeMillis()}
     */
    public static long now() {
        return System.currentTimeMillis();
    }

    /**
     * Returns {@code true} if the given timestamp (epoch ms) is in the past.
     *
     * @param epochMs the timestamp to check
     * @return {@code true} if expired
     */
    public static boolean isExpired(final long epochMs) {
        return System.currentTimeMillis() >= epochMs;
    }

    /**
     * Returns remaining seconds until {@code expiryEpochMs}, floored to 0.
     *
     * @param expiryEpochMs the expiry timestamp
     * @return remaining seconds, minimum 0
     */
    public static long remainingSeconds(final long expiryEpochMs) {
        return Math.max(0L, (expiryEpochMs - System.currentTimeMillis()) / 1_000L);
    }
}
