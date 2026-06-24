package dev.opinion2nd.antiespguard.common;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Detection signatures for the cheat mods AntiESPGuard knows about.
 *
 * <p>Two complementary techniques are supported, mirroring the reference
 * plugin:</p>
 * <ul>
 *   <li><b>Brand strings</b> — the client's {@code minecraft:brand} payload or
 *       declared plugin/mod channels often leak the client name.</li>
 *   <li><b>Sign probe</b> — a fake sign editor is opened and the lines the
 *       client auto-fills (or the channels it advertises) betray the mod.</li>
 * </ul>
 *
 * <p>All matching is lower-cased and substring based so version suffixes and
 * casing don't matter.</p>
 */
public final class ModSignatures {

    private ModSignatures() {
    }

    /** Lower-cased substrings that, if seen in a brand/channel, flag the mod. */
    private static final Map<String, List<String>> SIGNATURES = Map.of(
            "Freecam", List.of("freecam"),
            "Meteor Client", List.of("meteor", "meteorclient"),
            "Wurst", List.of("wurst"),
            "Xaero's Minimap", List.of("xaero", "xaerominimap", "xaeros")
    );

    /**
     * Returns the display name of the first enabled mod whose signature matches
     * the given haystack (brand string, channel name, or sign-probe payload),
     * or {@code null} if nothing matches.
     *
     * @param haystack       text to scan (any casing)
     * @param enabledMods    config's modDetection.detect map (name -> enabled)
     */
    public static String match(String haystack, Map<String, Boolean> enabledMods) {
        if (haystack == null || haystack.isEmpty()) {
            return null;
        }
        String hay = haystack.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, List<String>> e : SIGNATURES.entrySet()) {
            String mod = e.getKey();
            if (!Boolean.TRUE.equals(enabledMods.get(mod))) {
                continue;
            }
            for (String needle : e.getValue()) {
                if (hay.contains(needle)) {
                    return mod;
                }
            }
        }
        return null;
    }

    /** True if the brand is a plain vanilla/standard client brand. */
    public static boolean isVanillaBrand(String brand) {
        if (brand == null) {
            return true;
        }
        String b = brand.toLowerCase(Locale.ROOT);
        return b.equals("vanilla") || b.equals("fabric") || b.equals("forge")
                || b.equals("neoforge") || b.equals("paper") || b.equals("quilt");
    }
}
