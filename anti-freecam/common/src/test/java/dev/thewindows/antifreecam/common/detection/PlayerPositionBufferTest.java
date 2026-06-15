package dev.thewindows.antifreecam.common.detection;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerPositionBufferTest {

    @Test
    void ringBufferEvictsOldSamples() {
        PlayerPositionBuffer buf = new PlayerPositionBuffer(5);
        for (int i = 0; i < 7; i++) {
            buf.addSample(new PlayerPositionBuffer.PositionSample(i, 64, i, 0, 0, true, i));
        }
        assertEquals(5, buf.size());
    }

    @Test
    void frozenRatioIsOneForStaticPlayer() {
        PlayerPositionBuffer buf = new PlayerPositionBuffer(20);
        for (int i = 0; i < 20; i++) {
            buf.addSample(new PlayerPositionBuffer.PositionSample(100, 64, 200, 0, 0, true, i));
        }
        assertEquals(1.0, buf.getPositionFrozenRatio(0.001), 0.001);
    }

    @Test
    void frozenRatioIsLowForMovingPlayer() {
        PlayerPositionBuffer buf = new PlayerPositionBuffer(20);
        for (int i = 0; i < 20; i++) {
            buf.addSample(new PlayerPositionBuffer.PositionSample(i * 0.5, 64, 0, 0, 0, true, i));
        }
        assertTrue(buf.getPositionFrozenRatio(0.001) < 0.2);
    }

    @Test
    void lookDeltaIsHighForSpinningCamera() {
        PlayerPositionBuffer buf = new PlayerPositionBuffer(20);
        for (int i = 0; i < 20; i++) {
            buf.addSample(new PlayerPositionBuffer.PositionSample(0, 64, 0, i * 10f, 0, true, i));
        }
        assertTrue(buf.getTotalLookDelta() > 100);
    }

    @Test
    void lookDeltaIsZeroForStillLook() {
        PlayerPositionBuffer buf = new PlayerPositionBuffer(20);
        for (int i = 0; i < 20; i++) {
            buf.addSample(new PlayerPositionBuffer.PositionSample(0, 64, 0, 90f, 0, true, i));
        }
        assertEquals(0.0, buf.getTotalLookDelta(), 0.001);
    }

    @Test
    void yStdDevIsZeroForConstantY() {
        PlayerPositionBuffer buf = new PlayerPositionBuffer(10);
        for (int i = 0; i < 10; i++) {
            buf.addSample(new PlayerPositionBuffer.PositionSample(0, 64, 0, 0, 0, false, i));
        }
        assertEquals(0.0, buf.getYStdDev(), 0.001);
    }

    @Test
    void hasEnoughDataRequiresHalfCapacity() {
        PlayerPositionBuffer buf = new PlayerPositionBuffer(20);
        assertFalse(buf.hasEnoughData());
        for (int i = 0; i < 9; i++) {
            buf.addSample(new PlayerPositionBuffer.PositionSample(0, 64, 0, 0, 0, true, i));
        }
        assertFalse(buf.hasEnoughData());
        buf.addSample(new PlayerPositionBuffer.PositionSample(0, 64, 0, 0, 0, true, 9));
        assertTrue(buf.hasEnoughData());
    }

    @Test
    void clearResetsBuffer() {
        PlayerPositionBuffer buf = new PlayerPositionBuffer(10);
        for (int i = 0; i < 10; i++) {
            buf.addSample(new PlayerPositionBuffer.PositionSample(0, 64, 0, 0, 0, true, i));
        }
        buf.clear();
        assertEquals(0, buf.size());
        assertFalse(buf.hasEnoughData());
    }
}
