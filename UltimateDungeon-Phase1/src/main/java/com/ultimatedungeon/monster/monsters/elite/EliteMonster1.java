package com.ultimatedungeon.monster.monsters.elite;

import com.ultimatedungeon.api.monster.IMonster;
import com.ultimatedungeon.api.monster.IMonsterAbility;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/** EliteMonster1 — enhanced dungeon monster. Implemented in Milestone 4. */
public final class EliteMonster1 implements IMonster {
    @Override @NotNull public String getMonsterId() { return "elite_monster_1"; }
    @Override @NotNull public String getDisplayName() { return "Elite Monster 1"; }
    @Override @NotNull public List<IMonsterAbility> getAbilities() { return List.of(); }
    @Override public void spawn() {}
    @Override public void despawn() {}
    @Override public boolean isAlive() { return false; }
}
