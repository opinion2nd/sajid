package dev.opinion2nd.antiespguard.common;

/**
 * Pure, side-effect-free masking decisions shared by every platform.
 *
 * <p>The platform layer is responsible for <em>how</em> blocks/entities are
 * hidden (packet rewriting, {@code hideEntity}, mixins, …). This class only
 * answers the <em>whether</em> questions, so the behaviour is provably
 * identical on Paper, Fabric and NeoForge.</p>
 */
public final class MaskRules {

    private final AntiEspConfig cfg;

    public MaskRules(AntiEspConfig cfg) {
        this.cfg = cfg;
    }

    public AntiEspConfig config() {
        return cfg;
    }

    /** Is the plugin active at all in the given world / dimension? */
    public boolean worldActive(String worldName, String environment) {
        if (worldName != null && cfg.disabledWorlds.contains(worldName)) {
            return false;
        }
        return environment == null || cfg.enabledEnvironments.contains(environment);
    }

    /**
     * Should this viewer have masking applied right now? True when the viewer
     * is at or above {@link AntiEspConfig#revealBelowYWhenUnder} (i.e. a
     * "surface" player who must not see underground content).
     */
    public boolean viewerIsSurface(double viewerY) {
        return viewerY >= cfg.revealBelowYWhenUnder;
    }

    /** Should a block at this Y be hidden from a surface viewer? */
    public boolean shouldMaskBlockY(int y) {
        return y < cfg.hideBelowY;
    }

    /**
     * Should an entity/player at this Y be hidden from a surface viewer at
     * {@code viewerY}? Underground entities and players below the hide line are
     * invisible to surface players when the respective toggles are on.
     */
    public boolean shouldHideEntity(double entityY, double viewerY, boolean isPlayer) {
        if (!viewerIsSurface(viewerY)) {
            return false;
        }
        if (entityY >= cfg.hideBelowY) {
            return false;
        }
        return isPlayer ? cfg.maskUndergroundPlayers : cfg.maskEntities;
    }

    /**
     * Lookahead distance (in blocks) used when progressively revealing chunks
     * as a player descends. Elytra flight uses a longer lookahead.
     */
    public int revealLookahead(boolean elytraFlying) {
        return elytraFlying ? cfg.lazyUnmask.distanceElytra : cfg.lazyUnmask.distance;
    }

    /** Blocks the player must move before a reveal rescan is triggered. */
    public int rescanThreshold(boolean elytraFlying) {
        return elytraFlying ? cfg.lazyUnmask.rescanBlocksElytra : cfg.lazyUnmask.rescanBlocks;
    }

    public String maskBlockName() {
        return cfg.maskBlock;
    }

    public boolean skipMaskIfAlreadyAir() {
        return cfg.skipMaskIfAlreadyAir;
    }
}
