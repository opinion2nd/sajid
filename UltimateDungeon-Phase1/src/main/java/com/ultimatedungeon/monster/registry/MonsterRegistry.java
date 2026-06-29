package com.ultimatedungeon.monster.registry;

import com.ultimatedungeon.api.monster.IMonster;
import com.ultimatedungeon.api.monster.IMonsterRegistry;
import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

/** Holds all registered monster type definitions. */
public final class MonsterRegistry implements IMonsterRegistry {

    private final PluginLogger logger;
    private final Map<String, IMonster> monsters = new LinkedHashMap<>();

    public MonsterRegistry(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    @Override
    public void register(@NotNull final IMonster monster) {
        monsters.put(monster.getMonsterId(), monster);
        logger.debug("Registered monster: " + monster.getMonsterId());
    }

    @Override
    @Nullable
    public IMonster getMonster(@NotNull final String monsterId) {
        return monsters.get(monsterId);
    }

    @Override
    @NotNull
    public Collection<IMonster> getAllMonsters() {
        return Collections.unmodifiableCollection(monsters.values());
    }
}
