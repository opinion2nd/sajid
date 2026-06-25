package dev.opinion2nd.antifreecam.mask;

/**
 * Per-player masking state. Written on the main thread (join / world-change),
 * read from the async packet listeners — every field is therefore volatile.
 *
 * <p>The occlusion model means the masking decision depends only on world
 * geometry, not on the player's position, so the only per-player state we need
 * is "is masking switched off for this player?" ({@link #bypass}) and "is the
 * player's current world an enabled world?" ({@link #worldActive}).
 */
public final class PlayerMaskData {

    /** True when this player should never be masked (staff/bypass). */
    public volatile boolean bypass = false;

    /** True while the player's current world is an enabled masking world. */
    public volatile boolean worldActive = false;
}
