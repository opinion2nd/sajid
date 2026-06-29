package com.ultimatedungeon.config.files;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Typed wrapper for {@code messages.yml}.
 *
 * <p>All text values are stored as raw MiniMessage strings. The
 * {@link com.ultimatedungeon.util.MiniMessageUtil} converts them to
 * Adventure {@code Component}s at send time with placeholder substitution.</p>
 */
public final class MessagesConfig {

    // ── Global ────────────────────────────────────────────────────────────────
    private final String prefix;
    private final String noPermission;
    private final String playerOnly;
    private final String unknownSubCommand;

    // ── Dungeon ───────────────────────────────────────────────────────────────
    private final String dungeonAlreadyIn;
    private final String dungeonNotIn;
    private final String dungeonGenerating;
    private final String dungeonStarting;
    private final String dungeonCompleted;
    private final String dungeonFailed;
    private final String dungeonLeaveSuccess;

    // ── Party ─────────────────────────────────────────────────────────────────
    private final String partyCreated;
    private final String partyDisbanded;
    private final String partyInvited;
    private final String partyInviteReceived;
    private final String partyInviteExpired;
    private final String partyJoined;
    private final String partyLeft;
    private final String partyKicked;
    private final String partyLeaderTransferred;
    private final String partyAlreadyIn;
    private final String partyNotIn;
    private final String partyFull;
    private final String partyNoInvite;
    private final String partyNotLeader;
    private final String partyReadyCheckStarted;
    private final String partyReadyCheckPassed;
    private final String partyReadyCheckFailed;

    // ── Boss ──────────────────────────────────────────────────────────────────
    private final String bossCountdown;
    private final String bossEncounterStart;
    private final String bossPhaseChange;
    private final String bossDefeated;
    private final String bossArenaLocked;

    // ── Rewards ───────────────────────────────────────────────────────────────
    private final String rewardsRewarded;
    private final String rewardsCollectTimeout;

    public MessagesConfig(@NotNull final FileConfiguration cfg) {
        prefix             = cfg.getString("prefix",             "<dark_gray>[<gold><bold>UD</bold></gold><dark_gray>] ");
        noPermission       = cfg.getString("no-permission",      "<red>You don't have permission to do that.");
        playerOnly         = cfg.getString("player-only",        "<red>This command can only be used by players.");
        unknownSubCommand  = cfg.getString("unknown-subcommand", "<red>Unknown command.");

        dungeonAlreadyIn    = cfg.getString("dungeon.already-in-dungeon", "<red>You are already in a dungeon.");
        dungeonNotIn        = cfg.getString("dungeon.not-in-dungeon",     "<red>You are not currently in a dungeon.");
        dungeonGenerating   = cfg.getString("dungeon.generating",         "<yellow>Generating your dungeon, please wait...");
        dungeonStarting     = cfg.getString("dungeon.starting",           "<green>Your dungeon is starting!");
        dungeonCompleted    = cfg.getString("dungeon.completed",          "<gold>Dungeon completed! Well done, <yellow>{player}</yellow>!");
        dungeonFailed       = cfg.getString("dungeon.failed",             "<red>The dungeon has failed. Better luck next time.");
        dungeonLeaveSuccess = cfg.getString("dungeon.leave-success",      "<yellow>You have left the dungeon.");

        partyCreated          = cfg.getString("party.created",            "<green>Party created. You are the leader.");
        partyDisbanded        = cfg.getString("party.disbanded",          "<red>Your party has been disbanded.");
        partyInvited          = cfg.getString("party.invited",            "<yellow>{player} has been invited to your party.");
        partyInviteReceived   = cfg.getString("party.invite-received",    "<yellow>{player} has invited you to their party.");
        partyInviteExpired    = cfg.getString("party.invite-expired",     "<red>Your invitation to {player} has expired.");
        partyJoined           = cfg.getString("party.joined",             "<green>{player} has joined the party.");
        partyLeft             = cfg.getString("party.left",               "<yellow>{player} has left the party.");
        partyKicked           = cfg.getString("party.kicked",             "<red>{player} has been removed from the party.");
        partyLeaderTransferred= cfg.getString("party.leader-transferred", "<yellow>Leadership transferred to {player}.");
        partyAlreadyIn        = cfg.getString("party.already-in-party",   "<red>You are already in a party.");
        partyNotIn            = cfg.getString("party.not-in-party",       "<red>You are not in a party.");
        partyFull             = cfg.getString("party.party-full",         "<red>That party is full.");
        partyNoInvite         = cfg.getString("party.no-invite",          "<red>You have no pending party invitation.");
        partyNotLeader        = cfg.getString("party.not-leader",         "<red>Only the party leader can do that.");
        partyReadyCheckStarted= cfg.getString("party.ready-check-started","<yellow>Ready check started!");
        partyReadyCheckPassed = cfg.getString("party.ready-check-passed", "<green>All members are ready. Starting dungeon...");
        partyReadyCheckFailed = cfg.getString("party.ready-check-failed", "<red>Ready check failed.");

        bossCountdown      = cfg.getString("boss.countdown",       "<yellow>Boss encounter begins in <red>{time}</red>...");
        bossEncounterStart = cfg.getString("boss.encounter-start", "<red>The <bold>{boss-name}</bold> has awakened!");
        bossPhaseChange    = cfg.getString("boss.phase-change",    "<dark_red>Phase change!");
        bossDefeated       = cfg.getString("boss.defeated",        "<gold>The <bold>{boss-name}</bold> has been defeated!");
        bossArenaLocked    = cfg.getString("boss.arena-locked",    "<red>The arena is sealed. There is no escape.");

        rewardsRewarded       = cfg.getString("rewards.rewarded",        "<gold>You have received your rewards!");
        rewardsCollectTimeout = cfg.getString("rewards.collect-timeout", "<red>Your rewards have expired.");
    }

    // ── Accessors ─────────────────────────────────────────────────────────────
    @NotNull public String getPrefix()               { return prefix; }
    @NotNull public String getNoPermission()         { return noPermission; }
    @NotNull public String getPlayerOnly()           { return playerOnly; }
    @NotNull public String getUnknownSubCommand()    { return unknownSubCommand; }
    @NotNull public String getDungeonAlreadyIn()     { return dungeonAlreadyIn; }
    @NotNull public String getDungeonNotIn()         { return dungeonNotIn; }
    @NotNull public String getDungeonGenerating()    { return dungeonGenerating; }
    @NotNull public String getDungeonStarting()      { return dungeonStarting; }
    @NotNull public String getDungeonCompleted()     { return dungeonCompleted; }
    @NotNull public String getDungeonFailed()        { return dungeonFailed; }
    @NotNull public String getDungeonLeaveSuccess()  { return dungeonLeaveSuccess; }
    @NotNull public String getPartyCreated()         { return partyCreated; }
    @NotNull public String getPartyDisbanded()       { return partyDisbanded; }
    @NotNull public String getPartyInvited()         { return partyInvited; }
    @NotNull public String getPartyInviteReceived()  { return partyInviteReceived; }
    @NotNull public String getPartyInviteExpired()   { return partyInviteExpired; }
    @NotNull public String getPartyJoined()          { return partyJoined; }
    @NotNull public String getPartyLeft()            { return partyLeft; }
    @NotNull public String getPartyKicked()          { return partyKicked; }
    @NotNull public String getPartyLeaderTransferred(){ return partyLeaderTransferred; }
    @NotNull public String getPartyAlreadyIn()       { return partyAlreadyIn; }
    @NotNull public String getPartyNotIn()           { return partyNotIn; }
    @NotNull public String getPartyFull()            { return partyFull; }
    @NotNull public String getPartyNoInvite()        { return partyNoInvite; }
    @NotNull public String getPartyNotLeader()       { return partyNotLeader; }
    @NotNull public String getPartyReadyCheckStarted(){ return partyReadyCheckStarted; }
    @NotNull public String getPartyReadyCheckPassed() { return partyReadyCheckPassed; }
    @NotNull public String getPartyReadyCheckFailed() { return partyReadyCheckFailed; }
    @NotNull public String getBossCountdown()        { return bossCountdown; }
    @NotNull public String getBossEncounterStart()   { return bossEncounterStart; }
    @NotNull public String getBossPhaseChange()      { return bossPhaseChange; }
    @NotNull public String getBossDefeated()         { return bossDefeated; }
    @NotNull public String getBossArenaLocked()      { return bossArenaLocked; }
    @NotNull public String getRewardsRewarded()      { return rewardsRewarded; }
    @NotNull public String getRewardsCollectTimeout(){ return rewardsCollectTimeout; }
}
