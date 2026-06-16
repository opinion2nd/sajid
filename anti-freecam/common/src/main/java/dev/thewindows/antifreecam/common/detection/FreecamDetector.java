package dev.thewindows.antifreecam.common.detection;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FreecamDetector {

    private final DetectionConfig config;
    private final Map<UUID, PlayerPositionBuffer> buffers = new ConcurrentHashMap<>();
    private final Set<UUID> whitelisted = ConcurrentHashMap.newKeySet();

    public FreecamDetector(DetectionConfig config) {
        this.config = config;
    }

    public void initPlayer(UUID player) {
        buffers.put(player, new PlayerPositionBuffer(config.getBufferSize()));
    }

    public void removePlayer(UUID player) {
        buffers.remove(player);
    }

    public void resetPlayer(UUID player) {
        PlayerPositionBuffer buf = buffers.get(player);
        if (buf != null) buf.clear();
    }

    public void whitelistPlayer(UUID player) {
        whitelisted.add(player);
    }

    public void unwhitelistPlayer(UUID player) {
        whitelisted.remove(player);
    }

    public boolean isWhitelisted(UUID player) {
        return whitelisted.contains(player);
    }

    public void recordMovement(UUID player,
                                double x, double y, double z,
                                float yaw, float pitch,
                                boolean onGround,
                                long tick) {
        PlayerPositionBuffer buf = buffers.get(player);
        if (buf == null) return;
        buf.addSample(new PlayerPositionBuffer.PositionSample(x, y, z, yaw, pitch, onGround, tick));
    }

    public DetectionResult evaluate(UUID player) {
        if (whitelisted.contains(player)) return DetectionResult.notDetected();

        PlayerPositionBuffer buf = buffers.get(player);
        if (buf == null || !buf.hasEnoughData()) return DetectionResult.insufficient();

        double confidence = 0.0;
        StringBuilder reason = new StringBuilder();

        double frozenRatio = buf.getPositionFrozenRatio(config.getFrozenPositionEpsilon());
        double totalLookDelta = buf.getTotalLookDelta();
        double lookThreshold = config.getLookDeltaThresholdPerTick() * buf.size();

        // Signal 1: frozen body + moving look (strongest signal). This is the canonical
        // freecam signature, so on its own it is enough to reach the flag threshold.
        if (frozenRatio >= config.getFrozenRatioThreshold() && totalLookDelta > lookThreshold) {
            confidence += 0.7;
            reason.append("frozen-body+moving-look ");
        }

        // Signal 2: hovering mid-air without valid cause
        double yStdDev = buf.getYStdDev();
        double airRatio = buf.getAirRatio();
        if (yStdDev < 0.01 && airRatio > 0.5) {
            confidence += 0.2;
            reason.append("hovering-mid-air ");
        }

        // Signal 3: aggressive look rotation while body is high up
        double avgY = buf.getAverageY();
        if (avgY > config.getTriggerY() && totalLookDelta > lookThreshold * 2) {
            confidence += 0.2;
            reason.append("aggressive-look-rotation ");
        }

        // Signal 4: position outside expected render distance
        double maxDelta = buf.getMaxPositionDelta();
        double renderLimitBlocks = (config.getRenderDistanceChunks() * 16.0) + 32.0;
        if (maxDelta > renderLimitBlocks) {
            confidence += 0.4;
            reason.append("outside-render-distance ");
        }

        confidence = Math.min(confidence, 1.0);
        boolean detected = confidence >= config.getFlagConfidenceThreshold();
        return new DetectionResult(detected, (float) confidence, reason.toString().trim());
    }
}
