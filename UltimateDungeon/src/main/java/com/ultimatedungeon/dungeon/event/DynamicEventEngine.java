package com.ultimatedungeon.dungeon.event;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.monster.engine.WaveManager;
import com.ultimatedungeon.rewards.engine.RewardRoomService;
import com.ultimatedungeon.room.model.RoomData;
import com.ultimatedungeon.services.NotificationService;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Fires a weighted-random dynamic event when a player first enters an EVENT room.
 *
 * <p>All events and their parameters live in {@code dungeon.yml}; this engine
 * only interprets the chosen {@link DynamicEventSpec}. When no events are
 * configured (or the feature is disabled) it falls back to a plain combat wave so
 * an EVENT room is never empty.</p>
 */
public final class DynamicEventEngine {

    private final DynamicEventSettings settings;
    private final WaveManager waveManager;
    private final RewardRoomService rewardRoomService;
    private final NotificationService notifications;
    private final PluginLogger logger;

    public DynamicEventEngine(@NotNull final DynamicEventSettings settings,
                              @NotNull final WaveManager waveManager,
                              @NotNull final RewardRoomService rewardRoomService,
                              @NotNull final NotificationService notifications,
                              @NotNull final PluginLogger logger) {
        this.settings = settings;
        this.waveManager = waveManager;
        this.rewardRoomService = rewardRoomService;
        this.notifications = notifications;
        this.logger = logger;
    }

    /**
     * Triggers one dynamic event for an EVENT room.
     *
     * @return {@code true} if a bespoke event fired; {@code false} if the caller
     *         should fall back to its default combat behaviour.
     */
    public boolean trigger(@NotNull final UUID instanceId, @NotNull final RoomData room,
                           @NotNull final List<Player> players, @NotNull final List<String> monsterPool,
                           @NotNull final String difficultyId) {
        if (!settings.isEnabled() || !settings.hasEvents() || settings.getTotalWeight() <= 0) return false;
        final DynamicEventSpec spec = pick();
        if (spec == null) return false;

        logger.debug("Dynamic event '" + spec.id() + "' (" + spec.kind() + ") firing in room " + room.getRoomId());
        switch (spec.kind()) {
            case AMBUSH   -> ambush(instanceId, room, monsterPool, difficultyId, spec, players);
            case BLESSING -> applyEffects(players, spec, "<green><bold>Blessing", "<gray>Fortune favours you.");
            case CURSE    -> applyEffects(players, spec, "<dark_red><bold>Curse", "<gray>A dark aura clings to you.");
            case TREASURE -> treasure(room, players, spec);
        }
        return true;
    }

    private DynamicEventSpec pick() {
        int roll = ThreadLocalRandom.current().nextInt(settings.getTotalWeight());
        for (final DynamicEventSpec spec : settings.getEvents()) {
            roll -= spec.weight();
            if (roll < 0) return spec;
        }
        return null;
    }

    private void ambush(@NotNull final UUID instanceId, @NotNull final RoomData room,
                        @NotNull final List<String> monsterPool, @NotNull final String difficultyId,
                        @NotNull final DynamicEventSpec spec, @NotNull final List<Player> players) {
        for (final Player p : players) {
            notifications.title(p, "<red><bold>Ambush!", "<gray>Enemies close in.");
            notifications.sound(p, Sound.ENTITY_ENDERMAN_SCREAM, 1.0f, 0.7f);
        }
        waveManager.start(instanceId, room, monsterPool,
                Math.max(1, spec.extraWaves()), Math.max(1, spec.perWave()), difficultyId, room::setCleared);
    }

    private void applyEffects(@NotNull final List<Player> players, @NotNull final DynamicEventSpec spec,
                              @NotNull final String title, @NotNull final String subtitle) {
        for (final Player p : players) {
            for (final PotionEffectType type : spec.effects()) {
                p.addPotionEffect(new PotionEffect(type, spec.effectTicks(), spec.amplifier(), false, true));
            }
            notifications.title(p, title, subtitle);
            notifications.sound(p,
                    spec.kind() == DynamicEventSpec.DynamicEventKind.BLESSING
                            ? Sound.BLOCK_BEACON_ACTIVATE : Sound.ENTITY_WITHER_AMBIENT, 1.0f, 1.0f);
        }
    }

    private void treasure(@NotNull final RoomData room, @NotNull final List<Player> players,
                          @NotNull final DynamicEventSpec spec) {
        for (final Player p : players) {
            notifications.title(p, "<gold><bold>Hidden Cache", "<gray>You uncover a stash of loot.");
            notifications.sound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        }
        if (spec.lootTable() != null && !spec.lootTable().isBlank()) {
            rewardRoomService.grant(players, spec.lootTable());
        }
        room.setCleared();
    }
}
