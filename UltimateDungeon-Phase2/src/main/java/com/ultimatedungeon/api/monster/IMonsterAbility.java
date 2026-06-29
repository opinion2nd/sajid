package com.ultimatedungeon.api.monster;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/** Contract for a monster ability component. */
public interface IMonsterAbility {
    @NotNull String getAbilityId();
    boolean isReady();
    void activate(@NotNull LivingEntity monster);
}
