package com.ultimatedungeon.api.boss;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/** Contract for a single boss ability. */
public interface IBossAbility {
    @NotNull String getAbilityId();
    boolean isReady();
    void activate(@NotNull LivingEntity boss);
    void onCooldownEnd();
}
