package dev.opinion2nd.antiespguard.paper;

import dev.opinion2nd.antiespguard.common.AntiEspConfig;
import dev.opinion2nd.antiespguard.common.MaskRules;
import org.bukkit.Material;
import org.bukkit.World;

/**
 * Bukkit-flavoured view of the shared {@link AntiEspConfig}. Resolves the
 * string {@code maskBlock} to a Bukkit {@link Material} once, and exposes the
 * platform-agnostic {@link MaskRules} for decisions. Rebuilt on every reload so
 * the async packet listeners read an immutable snapshot.
 */
public final class PaperConfig {

    private final AntiEspConfig raw;
    private final MaskRules rules;
    private final Material maskMaterial;

    public PaperConfig(AntiEspConfig raw) {
        this.raw = raw;
        this.rules = new MaskRules(raw);
        Material mat = Material.matchMaterial(raw.maskBlock);
        this.maskMaterial = (mat != null && mat.isBlock()) ? mat : Material.STONE;
    }

    public AntiEspConfig raw() {
        return raw;
    }

    public MaskRules rules() {
        return rules;
    }

    public Material maskMaterial() {
        return maskMaterial;
    }

    /** True if masking should run for a player currently in this world. */
    public boolean isWorldActive(World world) {
        if (world == null) {
            return false;
        }
        return rules.worldActive(world.getName(), world.getEnvironment().name());
    }
}
