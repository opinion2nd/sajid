package com.ultimatedungeon.api.monster;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;

/** Contract for monster type registration and lookup. */
public interface IMonsterRegistry {
    void register(@NotNull IMonster monster);
    @Nullable IMonster getMonster(@NotNull String monsterId);
    @NotNull Collection<IMonster> getAllMonsters();
}
