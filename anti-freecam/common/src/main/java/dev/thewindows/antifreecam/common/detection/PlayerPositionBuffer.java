package dev.thewindows.antifreecam.common.detection;

import java.util.ArrayDeque;
import java.util.Deque;

public class PlayerPositionBuffer {

    public record PositionSample(
        double x, double y, double z,
        float yaw, float pitch,
        boolean onGround,
        long tick
    ) {}

    private final int maxSize;
    private final Deque<PositionSample> samples;

    public PlayerPositionBuffer(int maxSize) {
        this.maxSize = maxSize;
        this.samples = new ArrayDeque<>(maxSize);
    }

    public void addSample(PositionSample sample) {
        if (samples.size() >= maxSize) {
            samples.pollFirst();
        }
        samples.addLast(sample);
    }

    public void clear() {
        samples.clear();
    }

    public boolean hasEnoughData() {
        return samples.size() >= maxSize / 2;
    }

    public int size() {
        return samples.size();
    }

    /** Fraction of samples where XZ position matches the first sample (frozen ratio). */
    public double getPositionFrozenRatio(double epsilon) {
        if (samples.isEmpty()) return 0.0;
        PositionSample first = samples.peekFirst();
        long frozen = samples.stream().filter(s ->
            Math.abs(s.x() - first.x()) < epsilon &&
            Math.abs(s.z() - first.z()) < epsilon
        ).count();
        return (double) frozen / samples.size();
    }

    /** Sum of absolute yaw+pitch deltas across consecutive samples. */
    public double getTotalLookDelta() {
        double total = 0.0;
        PositionSample prev = null;
        for (PositionSample s : samples) {
            if (prev != null) {
                float dyaw = Math.abs(s.yaw() - prev.yaw());
                float dpitch = Math.abs(s.pitch() - prev.pitch());
                // Wrap yaw difference around 360°
                if (dyaw > 180f) dyaw = 360f - dyaw;
                total += dyaw + dpitch;
            }
            prev = s;
        }
        return total;
    }

    /** Maximum XZ distance from first sample position seen in the buffer. */
    public double getMaxPositionDelta() {
        if (samples.isEmpty()) return 0.0;
        PositionSample first = samples.peekFirst();
        double max = 0.0;
        for (PositionSample s : samples) {
            double dx = s.x() - first.x();
            double dz = s.z() - first.z();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > max) max = dist;
        }
        return max;
    }

    /** Standard deviation of Y values across all samples. */
    public double getYStdDev() {
        if (samples.size() < 2) return 0.0;
        double sum = 0.0;
        for (PositionSample s : samples) sum += s.y();
        double mean = sum / samples.size();
        double variance = 0.0;
        for (PositionSample s : samples) {
            double d = s.y() - mean;
            variance += d * d;
        }
        return Math.sqrt(variance / samples.size());
    }

    /** Fraction of samples where the player is NOT on the ground. */
    public double getAirRatio() {
        if (samples.isEmpty()) return 0.0;
        long airCount = samples.stream().filter(s -> !s.onGround()).count();
        return (double) airCount / samples.size();
    }

    /** Average Y position across all samples. */
    public double getAverageY() {
        if (samples.isEmpty()) return 0.0;
        return samples.stream().mapToDouble(PositionSample::y).average().orElse(0.0);
    }

    /** Most recent sample, or null if empty. */
    public PositionSample getLatest() {
        return samples.peekLast();
    }
}
