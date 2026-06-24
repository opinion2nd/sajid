package dev.opinion2nd.antiespguard.common;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

/**
 * Platform-agnostic configuration model for AntiESPGuard.
 *
 * <p>Mirrors the feature set of the reference AntiESPFreecam plugin with the
 * anti-xray system intentionally removed. Parses {@code config.yml} into this
 * object so the masking decision logic in {@link MaskRules} stays
 * side-effect-free and testable.</p>
 */
public final class AntiEspConfig {

    // ---- Core masking ------------------------------------------------------
    /** Dimensions where masking is active: NORMAL, NETHER, THE_END. */
    public Set<String> enabledEnvironments = new LinkedHashSet<>(List.of("NORMAL", "NETHER"));
    /** Worlds (exact folder name) where the plugin is fully inactive. */
    public Set<String> disabledWorlds = new LinkedHashSet<>();
    /** Hide every block below this Y from surface players. */
    public int hideBelowY = 20;
    /** Masking activates when a player's Y is at or above this value. */
    public int revealBelowYWhenUnder = 30;
    /** Max chunks processed per tick for Y-threshold transitions. */
    public int maxChunksPerTick = 128;
    /** What hidden blocks read as. AIR = empty/void (freecam sees nothing). */
    public String maskBlock = "AIR";
    /** If true, blocks that are already air are left as air instead of masked. */
    public boolean skipMaskIfAlreadyAir = false;
    /** Re-mask previously revealed chunks when a player returns to the surface. */
    public boolean remaskOnReturn = true;

    // ---- Progressive reveal (lazy unmask) ----------------------------------
    // No tunables: the revealed radius is simply the player's own view
    // distance, and it refreshes on chunk-cross — exactly how vanilla
    // chunk loading already behaves. Nothing here to configure.

    // ---- Entity / player masking -------------------------------------------
    /** Hide entities below hideBelowY from surface players (anti entity-radar). */
    public boolean maskEntities = true;
    /** Hide underground players (nametags/hitboxes) from surface players. */
    public boolean maskUndergroundPlayers = true;

    // ---- TAB prefix --------------------------------------------------------
    public final TabPrefix tabPrefix = new TabPrefix();

    public static final class TabPrefix {
        public List<String> placeholderFormats = new ArrayList<>(List.of(
                "%luckperms_prefix%%player_name%%luckperms_suffix%",
                "%vault_prefix%%player_name%%vault_suffix%"));
        public boolean useVault = true;
    }

    // ---- Anti Seed Cracker -------------------------------------------------
    public final AntiSeedCracker antiSeedCracker = new AntiSeedCracker();

    public static final class AntiSeedCracker {
        public boolean hideSeed = true;
    }

    // ---- Brand detection ---------------------------------------------------
    public final BrandDetection brandDetection = new BrandDetection();

    public static final class BrandDetection {
        public boolean notifyAdmins = true;
    }

    // ---- Mod detection -----------------------------------------------------
    public final ModDetection modDetection = new ModDetection();

    public static final class ModDetection {
        public boolean probeOnJoin = true;
        public boolean autoKick = true;
        public String kickMessage =
                "§cYou are using a cheat mod that is not allowed on this server ({mod})! "
                        + "Please remove it and rejoin.";
        public boolean notifyAdmins = true;
        public String discordWebhook = "";
        public int discordColor = 16711680;
        /** Mod display-name -> enabled. */
        public java.util.LinkedHashMap<String, Boolean> detect = defaultDetect();

        private static java.util.LinkedHashMap<String, Boolean> defaultDetect() {
            java.util.LinkedHashMap<String, Boolean> m = new java.util.LinkedHashMap<>();
            m.put("Freecam", true);
            m.put("Meteor Client", true);
            m.put("Wurst", true);
            return m;
        }
    }

    // ---- Update checker ----------------------------------------------------
    public final UpdateChecker updateChecker = new UpdateChecker();

    public static final class UpdateChecker {
        public boolean enabled = true;
        public boolean notifyOps = true;
    }

    // ========================================================================
    // Loading
    // ========================================================================

    /**
     * Parse a YAML document into a config object. Missing keys keep their
     * defaults, so this is forward/backward compatible with partial files.
     */
    @SuppressWarnings("unchecked")
    public static AntiEspConfig load(InputStream yaml) {
        AntiEspConfig c = new AntiEspConfig();
        if (yaml == null) {
            return c;
        }
        Object parsed = new Yaml().load(yaml);
        if (!(parsed instanceof Map)) {
            return c;
        }
        Map<String, Object> root = (Map<String, Object>) parsed;

        c.enabledEnvironments = upperSet(root.get("enabledEnvironments"), c.enabledEnvironments);
        c.disabledWorlds = stringSet(root.get("disabledWorlds"), c.disabledWorlds);
        c.hideBelowY = asInt(root.get("hideBelowY"), c.hideBelowY);
        c.revealBelowYWhenUnder = asInt(root.get("revealBelowYWhenUnder"), c.revealBelowYWhenUnder);
        c.maxChunksPerTick = asInt(root.get("maxChunksPerTick"), c.maxChunksPerTick);
        c.maskBlock = asString(root.get("maskBlock"), c.maskBlock).toUpperCase(Locale.ROOT);
        c.skipMaskIfAlreadyAir = asBool(root.get("skipMaskIfAlreadyAir"), c.skipMaskIfAlreadyAir);
        c.remaskOnReturn = asBool(root.get("remaskOnReturn"), c.remaskOnReturn);
        c.maskEntities = asBool(root.get("maskEntities"), c.maskEntities);
        c.maskUndergroundPlayers = asBool(root.get("maskUndergroundPlayers"), c.maskUndergroundPlayers);

        Map<String, Object> tab = asMap(root.get("tabPrefix"));
        if (tab != null) {
            Object pf = tab.get("placeholderFormats");
            if (pf instanceof List<?> list) {
                List<String> out = new ArrayList<>();
                for (Object o : list) {
                    out.add(String.valueOf(o));
                }
                c.tabPrefix.placeholderFormats = out;
            }
            c.tabPrefix.useVault = asBool(tab.get("useVault"), c.tabPrefix.useVault);
        }

        Map<String, Object> seed = asMap(root.get("antiSeedCracker"));
        if (seed != null) {
            c.antiSeedCracker.hideSeed = asBool(seed.get("hideSeed"), c.antiSeedCracker.hideSeed);
        }

        Map<String, Object> brand = asMap(root.get("brandDetection"));
        if (brand != null) {
            c.brandDetection.notifyAdmins = asBool(brand.get("notifyAdmins"), c.brandDetection.notifyAdmins);
        }

        Map<String, Object> mod = asMap(root.get("modDetection"));
        if (mod != null) {
            c.modDetection.probeOnJoin = asBool(mod.get("probeOnJoin"), c.modDetection.probeOnJoin);
            c.modDetection.autoKick = asBool(mod.get("autoKick"), c.modDetection.autoKick);
            c.modDetection.kickMessage = asString(mod.get("kickMessage"), c.modDetection.kickMessage);
            c.modDetection.notifyAdmins = asBool(mod.get("notifyAdmins"), c.modDetection.notifyAdmins);
            c.modDetection.discordWebhook = asString(mod.get("discordWebhook"), c.modDetection.discordWebhook);
            c.modDetection.discordColor = asInt(mod.get("discordColor"), c.modDetection.discordColor);
            Map<String, Object> detect = asMap(mod.get("detect"));
            if (detect != null) {
                java.util.LinkedHashMap<String, Boolean> dm = new java.util.LinkedHashMap<>();
                for (Map.Entry<String, Object> e : detect.entrySet()) {
                    dm.put(e.getKey(), asBool(e.getValue(), true));
                }
                c.modDetection.detect = dm;
            }
        }

        Map<String, Object> upd = asMap(root.get("updateChecker"));
        if (upd != null) {
            c.updateChecker.enabled = asBool(upd.get("enabled"), c.updateChecker.enabled);
            c.updateChecker.notifyOps = asBool(upd.get("notifyOps"), c.updateChecker.notifyOps);
        }

        return c;
    }

    /**
     * Clamp / sanity-check values. Returns a list of human-readable warnings
     * about anything that was auto-corrected (empty list = clean config).
     */
    public List<String> validateAndClamp() {
        List<String> warnings = new ArrayList<>();
        if (maxChunksPerTick < 1) {
            warnings.add("maxChunksPerTick must be >= 1; raised to 1.");
            maxChunksPerTick = 1;
        }
        if (revealBelowYWhenUnder < hideBelowY) {
            warnings.add("revealBelowYWhenUnder (" + revealBelowYWhenUnder
                    + ") < hideBelowY (" + hideBelowY + "); masking would never engage. "
                    + "Raised revealBelowYWhenUnder to hideBelowY.");
            revealBelowYWhenUnder = hideBelowY;
        }
        if (maskBlock == null || maskBlock.isBlank()) {
            warnings.add("maskBlock was empty; defaulted to STONE.");
            maskBlock = "STONE";
        }
        // Keep at most four active mod detections, matching the reference plugin.
        int active = 0;
        for (Map.Entry<String, Boolean> e : modDetection.detect.entrySet()) {
            if (Boolean.TRUE.equals(e.getValue())) {
                active++;
                if (active > 4) {
                    e.setValue(false);
                    warnings.add("modDetection: more than 4 mods enabled; '" + e.getKey()
                            + "' disabled (max 4 active).");
                }
            }
        }
        return warnings;
    }

    // ========================================================================
    // Coercion helpers
    // ========================================================================

    private static int asInt(Object o, int def) {
        if (o instanceof Number n) {
            return n.intValue();
        }
        if (o instanceof String s) {
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException ignored) {
                return def;
            }
        }
        return def;
    }

    private static boolean asBool(Object o, boolean def) {
        if (o instanceof Boolean b) {
            return b;
        }
        if (o instanceof String s) {
            return Boolean.parseBoolean(s.trim());
        }
        return def;
    }

    private static String asString(Object o, String def) {
        return o == null ? def : String.valueOf(o);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o) {
        return o instanceof Map ? (Map<String, Object>) o : null;
    }

    private static Set<String> upperSet(Object o, Set<String> def) {
        if (!(o instanceof List<?> list)) {
            return def;
        }
        Set<String> out = new LinkedHashSet<>();
        for (Object item : list) {
            out.add(String.valueOf(item).trim().toUpperCase(Locale.ROOT));
        }
        return out;
    }

    private static Set<String> stringSet(Object o, Set<String> def) {
        if (!(o instanceof List<?> list)) {
            return def;
        }
        Set<String> out = new LinkedHashSet<>();
        for (Object item : list) {
            out.add(String.valueOf(item).trim());
        }
        return out;
    }
}
