package dev.thewindows.antifreecam.common.detection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class FreecamDetectorTest {

    private FreecamDetector detector;
    private DetectionConfig config;
    private UUID player;

    @BeforeEach
    void setup() {
        config = new DetectionConfig();
        config.setBufferSize(40);
        detector = new FreecamDetector(config);
        player = UUID.randomUUID();
        detector.initPlayer(player);
    }

    @Test
    void insufficientDataReturnsNotDetected() {
        // Feed only 5 samples (less than bufferSize/2 = 20)
        for (int i = 0; i < 5; i++) {
            detector.recordMovement(player, 0, 64, 0, 0, 0, true, i);
        }
        DetectionResult result = detector.evaluate(player);
        assertFalse(result.detected());
        assertEquals("Insufficient data", result.reason());
    }

    @Test
    void normalMovingPlayerNotDetected() {
        // Player walking forward — position changes, look stable
        for (int i = 0; i < 40; i++) {
            detector.recordMovement(player, i * 0.3, 64, 0, 90f, 0f, true, i);
        }
        DetectionResult result = detector.evaluate(player);
        assertFalse(result.detected());
    }

    @Test
    void frozenBodyWithSpinningLookDetected() {
        // Player body frozen at (100, 64, 200), yaw spinning rapidly
        for (int i = 0; i < 40; i++) {
            detector.recordMovement(player, 100, 64, 200, i * 10f, 0f, true, i);
        }
        DetectionResult result = detector.evaluate(player);
        assertTrue(result.detected(), "Frozen body + spinning look should be detected");
        assertTrue(result.confidence() >= 0.5f);
        assertTrue(result.reason().contains("frozen-body"));
    }

    @Test
    void whitelistedPlayerNotDetected() {
        detector.whitelistPlayer(player);
        for (int i = 0; i < 40; i++) {
            detector.recordMovement(player, 100, 64, 200, i * 10f, 0f, true, i);
        }
        DetectionResult result = detector.evaluate(player);
        assertFalse(result.detected());
    }

    @Test
    void removedPlayerReturnsInsufficient() {
        detector.removePlayer(player);
        DetectionResult result = detector.evaluate(player);
        assertFalse(result.detected());
        assertEquals("Insufficient data", result.reason());
    }

    @Test
    void resetClearsBuffer() {
        for (int i = 0; i < 40; i++) {
            detector.recordMovement(player, 100, 64, 200, i * 10f, 0f, true, i);
        }
        detector.resetPlayer(player);
        DetectionResult result = detector.evaluate(player);
        assertEquals("Insufficient data", result.reason());
    }

    @Test
    void hoveringMidAirIncreasesConfidence() {
        // Frozen + hovering mid-air + spinning
        for (int i = 0; i < 40; i++) {
            detector.recordMovement(player, 100, 64.0005, 200, i * 10f, 0f, false, i);
        }
        DetectionResult result = detector.evaluate(player);
        assertTrue(result.confidence() >= 0.7f, "Hovering+spinning should push confidence to flag threshold");
    }
}
