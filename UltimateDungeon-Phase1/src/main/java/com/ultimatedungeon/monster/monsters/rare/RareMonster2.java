package com.ultimatedungeon.monster.monsters.rare;

import com.ultimatedungeon.api.monster.IMonster;
import com.ultimatedungeon.api.monster.IMonsterAbility;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/** RareMonster2 — rare dungeon monster with unique drops. Milestone 4. */
public final class RareMonster2 implements IMonster {
    @Override @NotNull public String getMonsterId() { return "rare_monster_2"; }
    @Override @NotNull public String getDisplayName() { return "Rare Monster 2"; }
    @Override @NotNull public List<IMonsterAbility> getAbilities() { return List.of(); }
    @Override public void spawn() {}
    @Override public void despawn() {}
    @Override public boolean isAlive() { return false; }
}
