package dev.opinion2nd.antifreecam.mask;

/**
 * Per-player masking state. Written on the main thread (move/join/teleport),
 * read from the async packet listeners — so every field is volatile.
 */
public final class PlayerMaskData {

    /** True while the player's OWN body is below revealBelowYWhenUnder. */
    public volatile boolean underground = false;

    /** True when this player should never be masked (staff/bypass). */
    public volatile boolean bypass = false;

    /** True while the player's current world is an enabled masking world. */
    public volatile boolean worldActive = false;
}
