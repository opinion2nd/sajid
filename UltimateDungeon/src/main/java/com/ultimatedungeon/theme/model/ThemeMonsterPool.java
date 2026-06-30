package com.ultimatedungeon.theme.model;

import com.ultimatedungeon.util.RandomUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Immutable monster and boss pool for a dungeon theme.
 * Provides random selection helpers used by the spawn system.
 */
public final class ThemeMonsterPool {

    private final List<String> monsterIds;
    private final List<String> bossIds;

    public ThemeMonsterPool(
            @NotNull final List<String> monsterIds,
            @NotNull final List<String> bossIds
    ) {
        this.monsterIds = List.copyOf(monsterIds);
        this.bossIds    = List.copyOf(bossIds);
    }

    @NotNull public List<String> getMonsterIds() { return monsterIds; }
    @NotNull public List<String> getBossIds()    { return bossIds;    }

    /** Returns a randomly selected monster ID from this pool. */
    @NotNull
    public String randomMonster() {
        if (monsterIds.isEmpty()) throw new IllegalStateException("Monster pool is empty.");
        return RandomUtil.randomElement(monsterIds);
    }

    /** Returns a randomly selected boss ID from this pool. */
    @NotNull
    public String randomBoss() {
        if (bossIds.isEmpty()) throw new IllegalStateException("Boss pool is empty.");
        return RandomUtil.randomElement(bossIds);
    }
}
