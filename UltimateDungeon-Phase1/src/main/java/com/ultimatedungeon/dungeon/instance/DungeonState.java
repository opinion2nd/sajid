package com.ultimatedungeon.dungeon.instance;

/** All possible lifecycle states for a dungeon instance. */
public enum DungeonState {
    /** Generation is in progress. */
    GENERATING,
    /** Generation complete; waiting for players to enter. */
    READY,
    /** Players are actively running the dungeon. */
    ACTIVE,
    /** Boss arena is locked; encounter in progress. */
    BOSS_ENCOUNTER,
    /** Dungeon completed successfully. */
    COMPLETED,
    /** Dungeon failed (all players died or left). */
    FAILED,
    /** Cleanup in progress; instance being torn down. */
    CLEANING_UP
}
