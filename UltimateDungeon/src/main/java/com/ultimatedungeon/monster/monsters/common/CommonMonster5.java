package com.ultimatedungeon.monster.monsters.common;

import com.ultimatedungeon.api.monster.IMonster;
import com.ultimatedungeon.api.monster.IMonsterAbility;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/** CommonMonster5 — standard dungeon monster. Implemented in Milestone 4. */
public final class CommonMonster5 implements IMonster {
    @Override @NotNull public String getMonsterId() { return "common_monster_5"; }
    @Override @NotNull public String getDisplayName() { return "Common Monster 5"; }
    @Override @NotNull public List<IMonsterAbility> getAbilities() { return List.of(); }
    @Override public void spawn() {}
    @Override public void despawn() {}
    @Override public boolean isAlive() { return false; }
}
