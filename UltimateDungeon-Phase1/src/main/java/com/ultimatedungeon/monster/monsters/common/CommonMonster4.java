package com.ultimatedungeon.monster.monsters.common;

import com.ultimatedungeon.api.monster.IMonster;
import com.ultimatedungeon.api.monster.IMonsterAbility;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/** CommonMonster4 — standard dungeon monster. Implemented in Milestone 4. */
public final class CommonMonster4 implements IMonster {
    @Override @NotNull public String getMonsterId() { return "common_monster_4"; }
    @Override @NotNull public String getDisplayName() { return "Common Monster 4"; }
    @Override @NotNull public List<IMonsterAbility> getAbilities() { return List.of(); }
    @Override public void spawn() {}
    @Override public void despawn() {}
    @Override public boolean isAlive() { return false; }
}
