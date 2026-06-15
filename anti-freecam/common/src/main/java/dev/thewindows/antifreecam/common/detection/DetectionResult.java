package dev.thewindows.antifreecam.common.detection;

public record DetectionResult(boolean detected, double confidence, String reason) {

    public static DetectionResult notDetected() {
        return new DetectionResult(false, 0.0, "No freecam signals");
    }

    public static DetectionResult insufficient() {
        return new DetectionResult(false, 0.0, "Insufficient data");
    }
}
