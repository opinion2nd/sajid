package com.ultimatedungeon.api.boss;

import org.jetbrains.annotations.NotNull;

/** Contract for a single boss phase. */
public interface IBossPhase {
    @NotNull String getPhaseId();
    double getHealthThreshold();
    void onEnter();
    void onExit();
    void tick();
}
