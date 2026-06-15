package dev.thewindows.antifreecam.common.detection;

public record DetectionResult(boolean detected, float confidence, String reason) {

    public static DetectionResult notDetected() {
        return new DetectionResult(false, 0f, "No freecam signals");
    }

    public static DetectionResult insufficient() {
        return new DetectionResult(false, 0f, "Insufficient data");
    }
}
