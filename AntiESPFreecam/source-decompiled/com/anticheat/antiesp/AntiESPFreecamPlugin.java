/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.retrooper.packetevents.PacketEvents
 *  com.github.retrooper.packetevents.PacketEventsAPI
 *  io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.World$Environment
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.PluginCommand
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.event.HandlerList
 *  org.bukkit.event.Listener
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 */
package com.anticheat.antiesp;

import com.anticheat.antiesp.AntiESPCommand;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiESPFreecamPlugin
extends JavaPlugin {
    private b maskManager;
    private c brandDetector;
    private f signProbe;
    private g updateChecker;

    public b getMaskManager() {
        return this.maskManager;
    }

    public c getBrandDetector() {
        return this.brandDetector;
    }

    public f getSignProbe() {
        return this.signProbe;
    }

    public void onLoad() {
        PacketEvents.setAPI((PacketEventsAPI)SpigotPacketEventsBuilder.build((Plugin)this));
        PacketEvents.getAPI().getSettings().checkForUpdates(false).bStats(false);
        PacketEvents.getAPI().load();
    }

    public void onEnable() {
        PacketEvents.getAPI().init();
        this.logEnvironment();
        this.saveDefaultConfig();
        this.mergeConfigDefaults();
        AntiESPCommand antiESPCommand = new AntiESPCommand(this);
        PluginCommand pluginCommand = this.getCommand("antiesp");
        if (pluginCommand != null) {
            pluginCommand.setExecutor((CommandExecutor)antiESPCommand);
            pluginCommand.setTabCompleter((TabCompleter)antiESPCommand);
        }
        this.applyConfiguration(this.getConfig());
        this.brandDetector = new c(this);
        Bukkit.getPluginManager().registerEvents((Listener)this.brandDetector, (Plugin)this);
        this.brandDetector.a();
        this.signProbe = new f(this);
        Bukkit.getPluginManager().registerEvents((Listener)this.signProbe, (Plugin)this);
        this.signProbe.a();
        boolean bl = this.getConfig().getBoolean("updateChecker.enabled", true);
        if (bl) {
            boolean bl2 = this.getConfig().getBoolean("updateChecker.notifyOps", true);
            this.updateChecker = new g(this, bl2);
            this.updateChecker.a();
        }
    }

    public void onDisable() {
        if (this.signProbe != null) {
            this.signProbe.c();
            this.signProbe = null;
        }
        if (this.brandDetector != null) {
            this.brandDetector.b();
            this.brandDetector = null;
        }
        if (this.maskManager != null) {
            this.maskManager.e();
            this.maskManager = null;
        }
        if (this.updateChecker != null) {
            this.updateChecker.c();
            this.updateChecker = null;
        }
        PacketEvents.getAPI().terminate();
    }

    public void reloadAntiEsp() {
        if (this.maskManager != null) {
            this.maskManager.e();
            this.maskManager = null;
        }
        if (this.signProbe != null) {
            this.signProbe.c();
            HandlerList.unregisterAll((Listener)this.signProbe);
            this.signProbe = null;
        }
        if (this.brandDetector != null) {
            this.brandDetector.b();
            HandlerList.unregisterAll((Listener)this.brandDetector);
            this.brandDetector = null;
        }
        if (this.updateChecker != null) {
            this.updateChecker.c();
            this.updateChecker = null;
        }
        this.reloadConfig();
        this.mergeConfigDefaults();
        this.applyConfiguration(this.getConfig());
        this.brandDetector = new c(this);
        Bukkit.getPluginManager().registerEvents((Listener)this.brandDetector, (Plugin)this);
        this.brandDetector.a();
        this.signProbe = new f(this);
        Bukkit.getPluginManager().registerEvents((Listener)this.signProbe, (Plugin)this);
        this.signProbe.a();
        boolean bl = this.getConfig().getBoolean("updateChecker.enabled", true);
        if (bl) {
            boolean bl2 = this.getConfig().getBoolean("updateChecker.notifyOps", true);
            this.updateChecker = new g(this, bl2);
            this.updateChecker.a();
        }
    }

    private void logEnvironment() {
        String string = Bukkit.getMinecraftVersion();
        String string2 = Bukkit.getName();
        String string3 = Bukkit.getVersion();
        boolean bl = e.a();
        boolean bl2 = e.b();
        boolean bl3 = false;
        try {
            Class.forName("org.purpurmc.purpur.PurpurConfig");
            bl3 = true;
        }
        catch (ClassNotFoundException classNotFoundException) {
            // empty catch block
        }
        String string4 = bl ? "Folia" : (bl2 ? "Leaf" : (bl3 ? "Purpur" : (string2.contains("Paper") ? "Paper" : string2)));
        this.getLogger().info("Running on " + string4 + " / MC " + string + " (" + string3 + ")");
        this.getLogger().info("[AntiESP] Version-adaptive build: no per-MC rebuild needed \u2014 keep the PacketEvents plugin updated to match your server version.");
        if (bl2) {
            boolean bl4 = e.c();
            boolean bl5 = e.d();
            boolean bl6 = e.e();
            if (bl4 || bl5 || bl6) {
                this.getLogger().info("Leaf async features detected:" + (bl4 ? " async-chunk-send" : "") + (bl5 ? " async-entity-tracker" : "") + (bl6 ? " parallel-world-ticking" : ""));
                if (bl4 || bl6) {
                    this.getLogger().info("  -> plugin will keep chunk serialization inline (disables own async executor) to avoid racing Leaf internals.");
                }
                if (bl5) {
                    this.getLogger().info("  -> position watchdog enabled as a safety net for delayed entity-tracker events.");
                }
            }
        }
    }

    private void mergeConfigDefaults() {
        try (InputStream inputStream = this.getResource("config.yml");){
            if (inputStream == null) {
                return;
            }
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            FileConfiguration fileConfiguration = this.getConfig();
            ArrayList<String> arrayList = new ArrayList<String>();
            for (String string : yamlConfiguration.getKeys(true)) {
                if (fileConfiguration.contains(string, true)) continue;
                if (yamlConfiguration.isConfigurationSection(string)) {
                    if (fileConfiguration.getConfigurationSection(string) == null) {
                        fileConfiguration.createSection(string);
                    }
                } else {
                    fileConfiguration.set(string, yamlConfiguration.get(string));
                }
                try {
                    List list;
                    List list2 = yamlConfiguration.getComments(string);
                    if (list2 != null && !list2.isEmpty()) {
                        fileConfiguration.setComments(string, list2);
                    }
                    if ((list = yamlConfiguration.getInlineComments(string)) != null && !list.isEmpty()) {
                        fileConfiguration.setInlineComments(string, list);
                    }
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                arrayList.add(string);
            }
            if (arrayList.isEmpty()) {
                return;
            }
            this.saveConfig();
            this.getLogger().info("[config] Added " + arrayList.size() + " new option(s) from this update (your settings were kept): " + String.valueOf(arrayList));
        }
        catch (Throwable throwable) {
            this.getLogger().warning("[config] Auto-merge of new config keys failed (continuing with defaults): " + String.valueOf(throwable));
        }
    }

    private void validateConfiguration(FileConfiguration fileConfiguration) {
        int n;
        String string;
        ArrayList<String> arrayList = new ArrayList<String>();
        this.expectInt(fileConfiguration, "hideBelowY", arrayList);
        this.expectInt(fileConfiguration, "revealBelowYWhenUnder", arrayList);
        this.expectInt(fileConfiguration, "scanRadiusChunks", arrayList);
        this.expectInt(fileConfiguration, "maxChunksPerTick", arrayList);
        this.expectInt(fileConfiguration, "antiXray.belowY", arrayList);
        this.expectInt(fileConfiguration, "antiXray.netherBelowY", arrayList);
        this.expectInt(fileConfiguration, "antiXray.revealRadius", arrayList);
        this.expectInt(fileConfiguration, "antiXray.fluidRevealRadius", arrayList);
        this.expectInt(fileConfiguration, "antiXray.skybase.aboveY", arrayList);
        this.expectBool(fileConfiguration, "antiXray.enabled", arrayList);
        this.expectBool(fileConfiguration, "antiXray.netherEnabled", arrayList);
        this.expectBool(fileConfiguration, "antiXray.fullMaskSurface", arrayList);
        this.expectBool(fileConfiguration, "antiXray.occlusionCheck", arrayList);
        this.expectBool(fileConfiguration, "antiXray.skybase.enabled", arrayList);
        this.expectBool(fileConfiguration, "maskEntities", arrayList);
        this.expectBool(fileConfiguration, "maskUndergroundPlayers", arrayList);
        this.expectBool(fileConfiguration, "remaskOnReturn", arrayList);
        this.expectBool(fileConfiguration, "antiSeedCracker.hideSeed", arrayList);
        int n2 = fileConfiguration.getInt("hideBelowY", 40);
        int n3 = fileConfiguration.getInt("revealBelowYWhenUnder", 50);
        if (n3 < n2) {
            arrayList.add("revealBelowYWhenUnder (" + n3 + ") is below hideBelowY (" + n2 + ") \u2014 players may flicker at the boundary. Set revealBelowYWhenUnder >= hideBelowY.");
        }
        if ((string = fileConfiguration.getString("maskBlock", "STONE")) != null && Material.matchMaterial((String)string.trim()) == null) {
            arrayList.add("maskBlock '" + string + "' is not a valid Material \u2014 falling back to AIR.");
        }
        for (String string2 : fileConfiguration.getStringList("enabledEnvironments")) {
            try {
                World.Environment.valueOf((String)string2.trim().toUpperCase());
            }
            catch (IllegalArgumentException illegalArgumentException) {
                arrayList.add("enabledEnvironments: '" + string2 + "' is not a valid dimension (use NORMAL, NETHER, THE_END).");
            }
        }
        for (String string2 : fileConfiguration.getStringList("antiXray.hiddenOres")) {
            if (string2 == null || Material.matchMaterial((String)string2.trim()) != null) continue;
            arrayList.add("antiXray.hiddenOres: '" + string2 + "' is not a valid Material \u2014 it will be ignored.");
        }
        this.checkReplacement(fileConfiguration, "antiXray.lavaReplacement", arrayList);
        this.checkReplacement(fileConfiguration, "antiXray.waterReplacement", arrayList);
        if (fileConfiguration.getBoolean("antiXray.skybase.enabled", false) && (n = fileConfiguration.getInt("antiXray.skybase.aboveY", 100)) <= n3) {
            arrayList.add("antiXray.skybase.aboveY (" + n + ") must be greater than revealBelowYWhenUnder (" + n3 + ") \u2014 skybase masking will be disabled.");
        }
        if (arrayList.isEmpty()) {
            return;
        }
        this.getLogger().warning("==================== config.yml issues ====================");
        for (String string2 : arrayList) {
            this.getLogger().warning("  - " + string2);
        }
        this.getLogger().warning("Safe defaults are being used for any invalid value above. Fix config.yml and run /antiesp reload.");
        this.getLogger().warning("===========================================================");
    }

    private void expectInt(FileConfiguration fileConfiguration, String string, List<String> list) {
        if (fileConfiguration.contains(string, true) && !fileConfiguration.isInt(string)) {
            list.add("'" + string + "' should be a whole number but is '" + String.valueOf(fileConfiguration.get(string)) + "'. Using default.");
        }
    }

    private void expectBool(FileConfiguration fileConfiguration, String string, List<String> list) {
        if (fileConfiguration.contains(string, true) && !fileConfiguration.isBoolean(string)) {
            list.add("'" + string + "' should be true or false but is '" + String.valueOf(fileConfiguration.get(string)) + "'. Using default.");
        }
    }

    private void checkReplacement(FileConfiguration fileConfiguration, String string, List<String> list) {
        String string2 = fileConfiguration.getString(string);
        if (string2 == null) {
            return;
        }
        String string3 = string2.trim().toUpperCase();
        if (string3.equals("DEFAULT") || string3.equals("NONE")) {
            return;
        }
        if (Material.matchMaterial((String)string3) == null) {
            list.add("'" + string + "' = '" + string2 + "' is not DEFAULT, NONE, or a valid Material.");
        }
    }

    private void applyConfiguration(FileConfiguration fileConfiguration) {
        String string5;
        try {
            this.validateConfiguration(fileConfiguration);
        }
        catch (Throwable throwable) {
            this.getLogger().warning("[config] Validation pass failed (ignored): " + String.valueOf(throwable));
        }
        int n = fileConfiguration.getInt("hideBelowY", 40);
        int n2 = fileConfiguration.getInt("revealBelowYWhenUnder", 50);
        int n3 = fileConfiguration.getInt("scanRadiusChunks", 4);
        int n4 = fileConfiguration.getInt("maxChunksPerTick", 64);
        int n5 = fileConfiguration.getInt("lazyUnmask.distance", 32);
        int n6 = fileConfiguration.getInt("lazyUnmask.distanceElytra", 64);
        int n7 = fileConfiguration.getInt("lazyUnmask.rescanBlocks", 2);
        int n8 = fileConfiguration.getInt("lazyUnmask.rescanBlocksElytra", 1);
        if (e.c() || e.e()) {
            if (n7 < 2) {
                this.getLogger().info("Leaf async-chunk-send active: bumping lazyUnmask.rescanBlocks " + n7 + " -> 2 to avoid sync chunk queue backlog.");
                n7 = 2;
            }
            if (n8 < 2) {
                this.getLogger().info("Leaf async-chunk-send active: bumping lazyUnmask.rescanBlocksElytra " + n8 + " -> 2.");
                n8 = 2;
            }
        }
        String string2 = fileConfiguration.getString("maskBlock", "AIR");
        boolean bl = fileConfiguration.getBoolean("skipMaskIfAlreadyAir", true);
        boolean bl2 = fileConfiguration.getBoolean("maskEntities", true);
        boolean bl3 = fileConfiguration.getBoolean("maskUndergroundPlayers", true);
        boolean bl4 = fileConfiguration.getBoolean("remaskOnReturn", true);
        boolean bl5 = fileConfiguration.getBoolean("antiXray.enabled", false);
        int n9 = fileConfiguration.getInt("antiXray.belowY", 64);
        int n10 = fileConfiguration.getInt("antiXray.revealRadius", 3);
        int n11 = fileConfiguration.getInt("antiXray.fluidRevealRadius", 5);
        boolean bl6 = fileConfiguration.getBoolean("antiXray.occlusionCheck", false);
        String string3 = fileConfiguration.getString("antiXray.lavaReplacement", "DEFAULT");
        String string4 = fileConfiguration.getString("antiXray.waterReplacement", "DEFAULT");
        boolean bl7 = fileConfiguration.getBoolean("antiXray.skybase.enabled", false);
        int n12 = bl7 ? fileConfiguration.getInt("antiXray.skybase.aboveY", 100) : 0;
        List list = fileConfiguration.getStringList("antiXray.hiddenOres");
        EnumSet<World.Environment> enumSet = EnumSet.noneOf(World.Environment.class);
        List list2 = fileConfiguration.getStringList("enabledEnvironments");
        if (list2.isEmpty()) {
            enumSet.add(World.Environment.NORMAL);
        } else {
            for (String string5 : list2) {
                try {
                    enumSet.add(World.Environment.valueOf((String)string5.trim().toUpperCase()));
                }
                catch (IllegalArgumentException illegalArgumentException) {}
            }
            if (enumSet.isEmpty()) {
                enumSet.add(World.Environment.NORMAL);
            }
        }
        HashSet hashSet = new HashSet(fileConfiguration.getStringList("disabledWorlds"));
        this.getLogger().info("Disabled worlds: " + String.valueOf(hashSet));
        string5 = Material.matchMaterial((String)string2);
        if (string5 == null) {
            string5 = Material.AIR;
        }
        this.maskManager = new b(this, n, n2, n3, (Material)string5, bl, n4, enumSet, bl5, n9, n10, n11, bl6, n12, list, hashSet, n5, n6, n7, n8, string3, string4, bl4);
        if (bl2 || bl3) {
            d d2 = new d(this, n, n2, bl2, bl3, this.maskManager.a(), enumSet, hashSet);
            this.maskManager.a(d2);
            d2.a();
            d2.c();
        }
        Bukkit.getPluginManager().registerEvents((Listener)this.maskManager, (Plugin)this);
        this.maskManager.d();
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.maskManager.d(player);
        }
    }
}

