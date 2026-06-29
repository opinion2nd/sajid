package com.ultimatedungeon.rewards.model;

import org.jetbrains.annotations.NotNull;
import java.util.List;

/** A bundle of rewards to deliver to a single player for a single event. */
public final class RewardBundle {

    private final RewardEvent event;
    private final List<RewardType> rewardTypes;

    public RewardBundle(
            @NotNull final RewardEvent event,
            @NotNull final List<RewardType> rewardTypes
    ) {
        this.event = event;
        this.rewardTypes = List.copyOf(rewardTypes);
    }

    @NotNull public RewardEvent getEvent() { return event; }
    @NotNull public List<RewardType> getRewardTypes() { return rewardTypes; }
}
