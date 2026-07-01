package com.ultimatedungeon.dungeon.instance;

import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.api.dungeon.IDungeonManager;
import com.ultimatedungeon.core.PluginLogger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stateful manager tracking all active {@link DungeonInstance} objects.
 *
 * <p>Thread-safe: uses {@link ConcurrentHashMap} for concurrent access
 * from async generation tasks and sync gameplay tasks.</p>
 */
public final class DungeonInstanceManager implements IDungeonManager {

    private final PluginLogger logger;
    private final Map<UUID, IDungeonInstance> instances = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerToInstance = new ConcurrentHashMap<>();

    public DungeonInstanceManager(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    @Override
    public void registerInstance(@NotNull final IDungeonInstance instance) {
        instances.put(instance.getInstanceId(), instance);
        logger.debug("Registered dungeon instance: " + instance.getInstanceId());
    }

    @Override
    public void removeInstance(@NotNull final UUID instanceId) {
        instances.remove(instanceId);
        playerToInstance.values().remove(instanceId);
        logger.debug("Removed dungeon instance: " + instanceId);
    }

    @Override
    @Nullable
    public IDungeonInstance getInstance(@NotNull final UUID instanceId) {
        return instances.get(instanceId);
    }

    @Override
    @Nullable
    public IDungeonInstance getInstanceForPlayer(@NotNull final Player player) {
        final UUID instanceId = playerToInstance.get(player.getUniqueId());
        return instanceId != null ? instances.get(instanceId) : null;
    }

    @Override
    @NotNull
    public Collection<IDungeonInstance> getActiveInstances() {
        return Collections.unmodifiableCollection(instances.values());
    }

    @Override
    public boolean isPlayerInDungeon(@NotNull final Player player) {
        return playerToInstance.containsKey(player.getUniqueId());
    }

    public void associatePlayer(@NotNull final Player player, @NotNull final UUID instanceId) {
        playerToInstance.put(player.getUniqueId(), instanceId);
    }

    public void disassociatePlayer(@NotNull final Player player) {
        playerToInstance.remove(player.getUniqueId());
    }

    public int getActiveCount() {
        return instances.size();
    }
}
