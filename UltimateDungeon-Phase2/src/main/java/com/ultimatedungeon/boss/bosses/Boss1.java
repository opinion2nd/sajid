package com.ultimatedungeon.boss.bosses;

import com.ultimatedungeon.api.boss.IBoss;
import com.ultimatedungeon.api.boss.IBossAbility;
import com.ultimatedungeon.api.boss.IBossPhase;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/** Boss 1 — unique boss implementation. Name, abilities, and arena defined in Milestone 4. */
public final class Boss1 implements IBoss {
    @Override @NotNull public String getBossId() { return "boss_1"; }
    @Override @NotNull public String getDisplayName() { return "Boss 1"; }
    @Override public void spawn() {}
    @Override public void despawn() {}
    @Override public void onPhaseChange(@NotNull final IBossPhase phase) {}
    @Override @NotNull public List<IBossAbility> getAbilities() { return List.of(); }
    @Override @NotNull public List<IBossPhase> getPhases() { return List.of(); }
    @Override public boolean isAlive() { return false; }
}
