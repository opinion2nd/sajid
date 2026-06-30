package com.ultimatedungeon.config.files;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

/** Typed wrapper for {@code performance.yml}. */
public final class PerformanceConfig {

    private final int  maxEntitiesPerDungeon;
    private final int  generationThreadCount;
    private final long playerStatsCacheTtlSeconds;
    private final long instanceCacheTtlSeconds;
    private final long dungeonHeartbeatTicks;
    private final long bossAiTicks;
    private final long monsterAiTicks;
    private final long trapTickTicks;
    private final long invitationExpiryTicks;
    private final long readyCheckExpiryTicks;

    public PerformanceConfig(@NotNull final FileConfiguration cfg) {
        maxEntitiesPerDungeon      = cfg.getInt("max-entities-per-dungeon", 80);
        generationThreadCount      = cfg.getInt("generation-thread-count", 2);
        playerStatsCacheTtlSeconds = cfg.getLong("cache.player-stats-ttl", 300L);
        instanceCacheTtlSeconds    = cfg.getLong("cache.instance-cache-ttl", 60L);
        dungeonHeartbeatTicks      = cfg.getLong("tick-intervals.dungeon-heartbeat", 20L);
        bossAiTicks                = cfg.getLong("tick-intervals.boss-ai", 10L);
        monsterAiTicks             = cfg.getLong("tick-intervals.monster-ai", 10L);
        trapTickTicks              = cfg.getLong("tick-intervals.trap-tick", 20L);
        invitationExpiryTicks      = cfg.getLong("tick-intervals.invitation-expiry", 20L);
        readyCheckExpiryTicks      = cfg.getLong("tick-intervals.ready-check-expiry", 20L);
    }

    public int  getMaxEntitiesPerDungeon()       { return maxEntitiesPerDungeon; }
    public int  getGenerationThreadCount()        { return generationThreadCount; }
    public long getPlayerStatsCacheTtlSeconds()   { return playerStatsCacheTtlSeconds; }
    public long getInstanceCacheTtlSeconds()      { return instanceCacheTtlSeconds; }
    public long getDungeonHeartbeatTicks()        { return dungeonHeartbeatTicks; }
    public long getBossAiTicks()                  { return bossAiTicks; }
    public long getMonsterAiTicks()               { return monsterAiTicks; }
    public long getTrapTickTicks()                { return trapTickTicks; }
    public long getInvitationExpiryTicks()        { return invitationExpiryTicks; }
    public long getReadyCheckExpiryTicks()        { return readyCheckExpiryTicks; }
}
