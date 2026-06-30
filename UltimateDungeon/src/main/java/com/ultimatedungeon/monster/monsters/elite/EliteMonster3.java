package com.ultimatedungeon.monster.monsters.elite;

import com.ultimatedungeon.api.monster.IMonster;
import com.ultimatedungeon.api.monster.IMonsterAbility;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/** EliteMonster3 — enhanced dungeon monster. Implemented in Milestone 4. */
public final class EliteMonster3 implements IMonster {
    @Override @NotNull public String getMonsterId() { return "elite_monster_3"; }
    @Override @NotNull public String getDisplayName() { return "Elite Monster 3"; }
    @Override @NotNull public List<IMonsterAbility> getAbilities() { return List.of(); }
    @Override public void spawn() {}
    @Override public void despawn() {}
    @Override public boolean isAlive() { return false; }
}
