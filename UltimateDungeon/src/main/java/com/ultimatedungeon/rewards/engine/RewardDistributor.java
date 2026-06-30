package com.ultimatedungeon.rewards.engine;

import com.ultimatedungeon.api.economy.IEconomyProvider;
import com.ultimatedungeon.api.reward.IReward;
import com.ultimatedungeon.api.reward.IRewardDistributor;
import com.ultimatedungeon.config.files.MessagesConfig;
import com.ultimatedungeon.config.files.RewardsConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.loot.engine.LootGenerator;
import com.ultimatedungeon.rewards.model.RewardEvent;
import com.ultimatedungeon.rewards.providers.*;
import com.ultimatedungeon.services.NotificationService;
import com.ultimatedungeon.services.StatisticsService;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Calculates and delivers each player's personal reward bundle for a given event.
 *
 * <p>Rewards are read per-event from {@code rewards.yml} (money, experience, a
 * loot table, console commands and tokens). Delivery is per-player so loot rolls
 * are isolated — no shared pool to contest. {@link RewardValidator} guards
 * against duplicate delivery and every grant is recorded to player statistics.</p>
 */
public final class RewardDistributor implements IRewardDistributor {

    private final RewardsConfig rewardsConfig;
    private final IEconomyProvider economy;
    private final LootGenerator lootGenerator;
    private final NotificationService notifications;
    private final MessagesConfig messages;
    private final StatisticsService statistics;
    private final RewardValidator validator;
    private final PluginLogger logger;

    public RewardDistributor(@NotNull final RewardsConfig rewardsConfig,
                             @NotNull final IEconomyProvider economy,
                             @NotNull final LootGenerator lootGenerator,
                             @NotNull final NotificationService notifications,
                             @NotNull final MessagesConfig messages,
                             @NotNull final StatisticsService statistics,
                             @NotNull final RewardValidator validator,
                             @NotNull final PluginLogger logger) {
        this.rewardsConfig = rewardsConfig;
        this.economy = economy;
        this.lootGenerator = lootGenerator;
        this.notifications = notifications;
        this.messages = messages;
        this.statistics = statistics;
        this.validator = validator;
        this.logger = logger;
    }

    @Override
    public void distribute(@NotNull final Player player, @NotNull final RewardEvent event) {
        if (!validator.canDeliver(player.getUniqueId(), event)) return;

        final ConfigurationSection section = rewardsConfig.raw()
                .getConfigurationSection("reward-events." + event.name());
        if (section == null) {
            logger.debug("No reward config for event: " + event);
            return;
        }

        for (final IReward reward : build(section)) {
            try {
                reward.deliver(player);
            } catch (final Exception e) {
                logger.severe("Reward delivery failed (" + reward.getRewardType() + ") for "
                        + player.getName(), e);
            }
        }
        notifications.chat(player, messages.getRewardsRewarded());
        statistics.increment(player.getUniqueId(), "rewards_earned", 1);
    }

    @Override
    public void distributeAll(@NotNull final Collection<Player> players, @NotNull final RewardEvent event) {
        for (final Player p : players) distribute(p, event);
    }

    @NotNull
    private List<IReward> build(@NotNull final ConfigurationSection section) {
        final List<IReward> rewards = new ArrayList<>();
        rewards.add(new MoneyRewardProvider(section.getDouble("money", 0), economy));
        rewards.add(new ExperienceRewardProvider(section.getInt("experience", 0)));

        final String lootTable = section.getString("loot-table");
        if (lootTable != null && !lootTable.isBlank()) {
            rewards.add(new ItemRewardProvider(lootGenerator.generate(lootTable)));
        }
        final List<String> commands = section.getStringList("commands");
        if (!commands.isEmpty()) {
            rewards.add(new CommandRewardProvider(commands));
        }
        final int tokens = section.getInt("tokens", 0);
        if (tokens > 0) {
            rewards.add(new TokenRewardProvider(tokens));
        }
        return rewards;
    }
}
