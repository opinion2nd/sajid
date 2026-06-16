package dev.thewindows.antifreecam.common.detection;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FreecamDetector {

    private final DetectionConfig config;
    private final Map<UUID, PlayerPositionBuffer> buffers = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastPacketAt = new ConcurrentHashMap<>();
    private final Set<UUID> whitelisted = ConcurrentHashMap.newKeySet();

    public FreecamDetector(DetectionConfig config) {
        this.config = config;
    }

    public void initPlayer(UUID player) {
        buffers.put(player, new PlayerPositionBuffer(config.getBufferSize()));
        lastPacketAt.put(player, System.currentTimeMillis());
    }

    public void removePlayer(UUID player) {
        buffers.remove(player);
        lastPacketAt.remove(player);
    }

    public void resetPlayer(UUID player) {
        PlayerPositionBuffer buf = buffers.get(player);
        if (buf != null) buf.clear();
        lastPacketAt.put(player, System.currentTimeMillis());
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
        lastPacketAt.put(player, System.currentTimeMillis());
    }

    public DetectionResult evaluate(UUID player) {
        if (whitelisted.contains(player)) return DetectionResult.notDetected();
        return evaluateRaw(player);
    }

    /** Same as evaluate() but ignores the whitelist — used for live debugging. */
    public DetectionResult evaluateRaw(UUID player) {
        PlayerPositionBuffer buf = buffers.get(player);
        if (buf == null || !buf.hasEnoughData()) return DetectionResult.insufficient();

        double confidence = 0.0;
        StringBuilder reason = new StringBuilder();

        double frozenRatio = buf.getPositionFrozenRatio(config.getFrozenPositionEpsilon());
        double totalLookDelta = buf.getTotalLookDelta();
        double lookThreshold = config.getLookDeltaThresholdPerTick() * buf.size();

        // Signal 1: frozen body + moving look (strongest signal — sufficient alone)
        if (frozenRatio >= config.getFrozenRatioThreshold() && totalLookDelta > lookThreshold) {
            confidence += 0.70;
            reason.append("frozen-body+moving-look ");
        }

        // Signal 2: hovering mid-air without valid cause
        double yStdDev = buf.getYStdDev();
        double airRatio = buf.getAirRatio();
        if (yStdDev < 0.01 && airRatio > 0.5) {
            confidence += 0.15;
            reason.append("hovering-mid-air ");
        }

        // Signal 3: aggressive look rotation while body is high up
        double avgY = buf.getAverageY();
        if (avgY > config.getTriggerY() && totalLookDelta > lookThreshold * 2) {
            confidence += 0.10;
            reason.append("aggressive-look-rotation ");
        }

        // Signal 4: position outside expected render distance
        double maxDelta = buf.getMaxPositionDelta();
        double renderLimitBlocks = (config.getRenderDistanceChunks() * 16.0) + 32.0;
        if (maxDelta > renderLimitBlocks) {
            confidence += 0.35;
            reason.append("outside-render-distance ");
        }

        // Signal 5: client has gone completely silent (no movement/look packets at all).
        // Vanilla clients resend a position packet at least once a second even when idle;
        // most freecam mods suppress every outgoing packet while the camera is detached,
        // so total silence beyond that window is a strong standalone signal.
        long silenceMs = getPacketSilenceMs(player);
        if (silenceMs >= config.getPacketSilenceMs()) {
            confidence += 0.75;
            reason.append("packet-silence(").append(silenceMs).append("ms) ");
        }

        confidence = Math.min(confidence, 1.0);
        boolean detected = confidence >= config.getFlagConfidenceThreshold();
        return new DetectionResult(detected, confidence, reason.toString().trim());
    }

    public long getPacketSilenceMs(UUID player) {
        Long last = lastPacketAt.get(player);
        return last == null ? 0L : System.currentTimeMillis() - last;
    }

    /** Live snapshot of all signal inputs for a player, for the /antifreecam debug command. */
    public String debugSnapshot(UUID player) {
        PlayerPositionBuffer buf = buffers.get(player);
        if (buf == null) return "no buffer (player not tracked)";
        if (!buf.hasEnoughData()) {
            return "insufficient data (" + buf.size() + "/" + config.getBufferSize() +
                " samples, need " + (config.getBufferSize() / 2) + ") — silenceMs=" + getPacketSilenceMs(player);
        }
        double frozenRatio = buf.getPositionFrozenRatio(config.getFrozenPositionEpsilon());
        double totalLookDelta = buf.getTotalLookDelta();
        double lookThreshold = config.getLookDeltaThresholdPerTick() * buf.size();
        double yStdDev = buf.getYStdDev();
        double airRatio = buf.getAirRatio();
        double avgY = buf.getAverageY();
        double maxDelta = buf.getMaxPositionDelta();
        long silenceMs = getPacketSilenceMs(player);
        DetectionResult result = evaluateRaw(player);
        boolean whitelistedFlag = whitelisted.contains(player);

        return String.format(
            "samples=%d/%d whitelisted=%b frozenRatio=%.2f(need %.2f) lookDelta=%.1f/%.1f yStdDev=%.4f airRatio=%.2f avgY=%.1f maxXZDelta=%.1f silenceMs=%d(need %d) => confidence=%.2f detected=%b [%s]",
            buf.size(), config.getBufferSize(), whitelistedFlag,
            frozenRatio, config.getFrozenRatioThreshold(),
            totalLookDelta, lookThreshold,
            yStdDev, airRatio, avgY, maxDelta,
            silenceMs, config.getPacketSilenceMs(),
            result.confidence(), result.detected(), result.reason());
    }
}
