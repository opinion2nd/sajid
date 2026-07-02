package com.ultimatedungeon.config.files;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

/** Typed wrapper for {@code party.yml}. */
public final class PartyConfig {

    public enum LeaderTransferMode { AUTO, MANUAL, DISBAND }

    private final int                maxPartySize;
    private final int                inviteTimeoutSeconds;
    private final int                readyCheckDurationSeconds;
    private final int                reconnectTimeoutSeconds;
    private final LeaderTransferMode leaderTransferMode;
    private final boolean            friendlyFire;
    private final boolean            lateJoin;
    private final boolean            reviveEnabled;
    private final int                reviveHoldSeconds;
    private final int                reviveTimeoutSeconds;

    public PartyConfig(@NotNull final FileConfiguration cfg) {
        maxPartySize               = cfg.getInt("max-party-size", 6);
        inviteTimeoutSeconds       = cfg.getInt("invite-timeout", 60);
        readyCheckDurationSeconds  = cfg.getInt("ready-check-duration", 30);
        reconnectTimeoutSeconds    = cfg.getInt("reconnect-timeout", 120);
        friendlyFire               = cfg.getBoolean("friendly-fire", false);
        lateJoin                   = cfg.getBoolean("late-join", false);
        reviveEnabled              = cfg.getBoolean("revive.enabled", true);
        reviveHoldSeconds          = Math.max(1, cfg.getInt("revive.hold-seconds", 5));
        reviveTimeoutSeconds       = Math.max(10, cfg.getInt("revive.timeout-seconds", 60));

        final String modeStr = cfg.getString("leader-transfer-mode", "auto");
        leaderTransferMode = switch (modeStr.toLowerCase()) {
            case "manual"  -> LeaderTransferMode.MANUAL;
            case "disband" -> LeaderTransferMode.DISBAND;
            default        -> LeaderTransferMode.AUTO;
        };
    }

    public int                getMaxPartySize()               { return maxPartySize; }
    public int                getInviteTimeoutSeconds()        { return inviteTimeoutSeconds; }
    public int                getReadyCheckDurationSeconds()   { return readyCheckDurationSeconds; }
    public int                getReconnectTimeoutSeconds()     { return reconnectTimeoutSeconds; }
    @NotNull public LeaderTransferMode getLeaderTransferMode() { return leaderTransferMode; }
    public boolean            isFriendlyFire()                 { return friendlyFire; }
    public boolean            isLateJoin()                     { return lateJoin; }
    public boolean            isReviveEnabled()                { return reviveEnabled; }
    public int                getReviveHoldSeconds()           { return reviveHoldSeconds; }
    public int                getReviveTimeoutSeconds()        { return reviveTimeoutSeconds; }

    /** Invite timeout in ticks (for Bukkit scheduler). */
    public long getInviteTimeoutTicks()       { return (long) inviteTimeoutSeconds * 20L; }
    /** Ready-check duration in ticks. */
    public long getReadyCheckDurationTicks()  { return (long) readyCheckDurationSeconds * 20L; }
    /** Reconnect timeout in ticks. */
    public long getReconnectTimeoutTicks()    { return (long) reconnectTimeoutSeconds * 20L; }
}
