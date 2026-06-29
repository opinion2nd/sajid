package com.ultimatedungeon.boss.registry;

import com.ultimatedungeon.api.boss.IBoss;
import com.ultimatedungeon.api.boss.IBossRegistry;
import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

/** Holds all registered boss type definitions. */
public final class BossRegistry implements IBossRegistry {

    private final PluginLogger logger;
    private final Map<String, IBoss> bosses = new LinkedHashMap<>();

    public BossRegistry(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    @Override
    public void register(@NotNull final IBoss boss) {
        bosses.put(boss.getBossId(), boss);
        logger.debug("Registered boss: " + boss.getBossId());
    }

    @Override
    @Nullable
    public IBoss getBoss(@NotNull final String bossId) {
        return bosses.get(bossId);
    }

    @Override
    @NotNull
    public Collection<IBoss> getAllBosses() {
        return Collections.unmodifiableCollection(bosses.values());
    }
}
