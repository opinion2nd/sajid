package com.ultimatedungeon.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * General-purpose input validation helpers.
 *
 * <p>Every method either throws a descriptive {@link IllegalArgumentException}
 * or returns a boolean — callers choose the appropriate form. These utilities
 * are used throughout config loading, command parsing, and DAO operations to
 * catch bad values as early as possible.</p>
 */
public final class ValidationUtil {

    private ValidationUtil() {}

    // ── Null / blank ──────────────────────────────────────────────────────────

    /**
     * Asserts that {@code value} is not {@code null} or blank.
     *
     * @param value     the string to check
     * @param fieldName human-readable field name for the error message
     * @throws IllegalArgumentException if the value is null or blank
     */
    public static void requireNonBlank(
            @Nullable final String value,
            @NotNull  final String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank.");
        }
    }

    // ── Numeric ranges ────────────────────────────────────────────────────────

    /**
     * Asserts that {@code value} is strictly positive (> 0).
     *
     * @param value     the value to check
     * @param fieldName field name for the error message
     * @throws IllegalArgumentException if the value is ≤ 0
     */
    public static void requirePositive(final int value, @NotNull final String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be > 0, got: " + value);
        }
    }

    /** Asserts that {@code value} is strictly positive (> 0). */
    public static void requirePositive(final double value, @NotNull final String fieldName) {
        if (value <= 0.0) {
            throw new IllegalArgumentException(fieldName + " must be > 0, got: " + value);
        }
    }

    /**
     * Asserts that {@code value} is non-negative (≥ 0).
     *
     * @param value     the value to check
     * @param fieldName field name for the error message
     */
    public static void requireNonNegative(final int value, @NotNull final String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must be ≥ 0, got: " + value);
        }
    }

    /**
     * Asserts that {@code value} falls within {@code [min, max]} inclusive.
     *
     * @param value     the value to check
     * @param min       inclusive lower bound
     * @param max       inclusive upper bound
     * @param fieldName field name for the error message
     * @throws IllegalArgumentException if out of range
     */
    public static void requireInRange(
            final double value,
            final double min,
            final double max,
            @NotNull final String fieldName
    ) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                fieldName + " must be in [" + min + ", " + max + "], got: " + value
            );
        }
    }

    // ── Probability checks ────────────────────────────────────────────────────

    /**
     * Asserts that {@code chance} is a valid probability value in {@code [0.0, 1.0]}.
     *
     * @param chance    the value to validate
     * @param fieldName field name for the error message
     */
    public static void requireProbability(final double chance, @NotNull final String fieldName) {
        requireInRange(chance, 0.0, 1.0, fieldName);
    }

    // ── String length ─────────────────────────────────────────────────────────

    /**
     * Asserts that {@code value} does not exceed {@code maxLength} characters.
     *
     * @param value     the string to check
     * @param maxLength the maximum permitted length
     * @param fieldName field name for the error message
     */
    public static void requireMaxLength(
            @NotNull  final String value,
            final int              maxLength,
            @NotNull  final String fieldName
    ) {
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(
                fieldName + " exceeds maximum length of " + maxLength
                + " characters (got " + value.length() + ")."
            );
        }
    }

    // ── Boolean helpers ───────────────────────────────────────────────────────

    /**
     * Returns {@code true} if {@code value} is non-null and non-blank.
     *
     * @param value the string to test
     * @return {@code true} if valid
     */
    public static boolean isNonBlank(@Nullable final String value) {
        return value != null && !value.isBlank();
    }

    /**
     * Clamps {@code value} to the range {@code [min, max]}.
     *
     * @param value the value to clamp
     * @param min   lower bound
     * @param max   upper bound
     * @return the clamped value
     */
    public static int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Clamps {@code value} to {@code [min, max]}. */
    public static double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }
}
