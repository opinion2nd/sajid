package dev.thewindows.antifreecam.common.detection;

public class DetectionConfig {

    private int bufferSize = 40;
    private double frozenPositionEpsilon = 0.001;
    private double frozenRatioThreshold = 0.95;
    private double lookDeltaThresholdPerTick = 5.0;
    private double flagConfidenceThreshold = 0.70;
    private double adminNotifyConfidenceThreshold = 0.90;
    private double triggerY = 10.0;
    private int blockRadius = 5;
    private int evaluationIntervalTicks = 10;
    private int voidRecheckIntervalTicks = 20;
    private int renderDistanceChunks = 8;

    public DetectionConfig() {}

    public int getBufferSize() { return bufferSize; }
    public void setBufferSize(int bufferSize) { this.bufferSize = bufferSize; }

    public double getFrozenPositionEpsilon() { return frozenPositionEpsilon; }
    public void setFrozenPositionEpsilon(double frozenPositionEpsilon) { this.frozenPositionEpsilon = frozenPositionEpsilon; }

    public double getFrozenRatioThreshold() { return frozenRatioThreshold; }
    public void setFrozenRatioThreshold(double frozenRatioThreshold) { this.frozenRatioThreshold = frozenRatioThreshold; }

    public double getLookDeltaThresholdPerTick() { return lookDeltaThresholdPerTick; }
    public void setLookDeltaThresholdPerTick(double v) { this.lookDeltaThresholdPerTick = v; }

    public double getFlagConfidenceThreshold() { return flagConfidenceThreshold; }
    public void setFlagConfidenceThreshold(double v) { this.flagConfidenceThreshold = v; }

    public double getAdminNotifyConfidenceThreshold() { return adminNotifyConfidenceThreshold; }
    public void setAdminNotifyConfidenceThreshold(double v) { this.adminNotifyConfidenceThreshold = v; }

    public double getTriggerY() { return triggerY; }
    public void setTriggerY(double triggerY) { this.triggerY = triggerY; }

    public int getBlockRadius() { return blockRadius; }
    public void setBlockRadius(int blockRadius) { this.blockRadius = blockRadius; }

    public int getEvaluationIntervalTicks() { return evaluationIntervalTicks; }
    public void setEvaluationIntervalTicks(int v) { this.evaluationIntervalTicks = v; }

    public int getVoidRecheckIntervalTicks() { return voidRecheckIntervalTicks; }
    public void setVoidRecheckIntervalTicks(int v) { this.voidRecheckIntervalTicks = v; }

    public int getRenderDistanceChunks() { return renderDistanceChunks; }
    public void setRenderDistanceChunks(int v) { this.renderDistanceChunks = v; }
}
