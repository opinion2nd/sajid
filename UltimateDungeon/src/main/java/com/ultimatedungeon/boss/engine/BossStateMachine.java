package com.ultimatedungeon.boss.engine;

import com.ultimatedungeon.boss.model.BossPhaseData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Finite state machine that selects the active boss phase from the boss's health
 * ratio. Phases are ordered from highest threshold (full health) to lowest; the
 * active phase is the deepest one whose threshold is still at or above the
 * current ratio.
 */
public final class BossStateMachine {

    private final List<BossPhaseData> phases;
    private int currentIndex = -1;

    public BossStateMachine(@NotNull final List<BossPhaseData> phases) {
        this.phases = phases;
    }

    /**
     * Updates the machine for the given health ratio.
     *
     * @return the newly entered phase if a transition occurred, otherwise {@code null}
     */
    @Nullable
    public BossPhaseData update(final double ratio) {
        int target = 0;
        for (int i = 0; i < phases.size(); i++) {
            if (phases.get(i).threshold() >= ratio) {
                target = i;
            }
        }
        if (target != currentIndex) {
            currentIndex = target;
            return phases.get(target);
        }
        return null;
    }

    public int getCurrentPhaseIndex() {
        return currentIndex;
    }
}
