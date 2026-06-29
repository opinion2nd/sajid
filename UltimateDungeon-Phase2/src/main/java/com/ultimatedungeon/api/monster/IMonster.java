package com.ultimatedungeon.api.monster;

import org.jetbrains.annotations.NotNull;
import java.util.List;

/** Contract for a dungeon monster implementation. */
public interface IMonster {
    @NotNull String getMonsterId();
    @NotNull String getDisplayName();
    @NotNull List<IMonsterAbility> getAbilities();
    void spawn();
    void despawn();
    boolean isAlive();
}
