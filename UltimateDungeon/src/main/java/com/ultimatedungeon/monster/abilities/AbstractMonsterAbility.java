package com.ultimatedungeon.monster.abilities;

import com.ultimatedungeon.api.monster.IMonsterAbility;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for monster ability components, handling id and cooldown bookkeeping
 * so concrete abilities only implement their effect in {@link #perform}.
 */
public abstract class AbstractMonsterAbility implements IMonsterAbility {

    private final String id;
    private final long cooldownMs;
    private long nextReadyAt;

    protected AbstractMonsterAbility(@NotNull final String id, final long cooldownTicks) {
        this.id = id;
        this.cooldownMs = cooldownTicks * 50L;
    }

    @Override @NotNull public String getAbilityId() { return id; }

    @Override public boolean isReady() { return System.currentTimeMillis() >= nextReadyAt; }

    @Override
    public void activate(@NotNull final LivingEntity monster) {
        if (!isReady()) return;
        perform(monster);
        nextReadyAt = System.currentTimeMillis() + cooldownMs;
    }

    /** Concrete effect, executed only when the ability is off cooldown. */
    protected abstract void perform(@NotNull LivingEntity monster);
}
