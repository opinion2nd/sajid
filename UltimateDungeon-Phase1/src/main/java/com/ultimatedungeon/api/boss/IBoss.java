package com.ultimatedungeon.api.boss;

import org.jetbrains.annotations.NotNull;
import java.util.List;

/** Contract for a boss implementation. */
public interface IBoss {
    @NotNull String getBossId();
    @NotNull String getDisplayName();
    void spawn();
    void despawn();
    void onPhaseChange(@NotNull IBossPhase newPhase);
    @NotNull List<IBossAbility> getAbilities();
    @NotNull List<IBossPhase> getPhases();
    boolean isAlive();
}
