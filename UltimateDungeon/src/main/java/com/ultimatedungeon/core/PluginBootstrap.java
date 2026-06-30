package com.ultimatedungeon.core;

import com.ultimatedungeon.UltimateDungeon;
import com.ultimatedungeon.commands.PartyCommand;
import com.ultimatedungeon.compat.VersionDetector;
import com.ultimatedungeon.config.ConfigManager;
import com.ultimatedungeon.database.DatabaseManager;
import com.ultimatedungeon.dungeon.generation.*;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.economy.NoOpEconomyProvider;
import com.ultimatedungeon.economy.VaultEconomyProvider;
import com.ultimatedungeon.listeners.party.PartyPlayerJoinListener;
import com.ultimatedungeon.listeners.party.PartyPlayerQuitListener;
import com.ultimatedungeon.managers.CooldownManager;
import com.ultimatedungeon.managers.PlayerSessionManager;
import com.ultimatedungeon.party.manager.InvitationManager;
import com.ultimatedungeon.party.manager.PartyManager;
import com.ultimatedungeon.party.manager.ReadyCheckManager;
import com.ultimatedungeon.party.service.PartyService;
import com.ultimatedungeon.party.service.PartyValidationService;
import com.ultimatedungeon.room.registry.RoomRegistry;
import com.ultimatedungeon.room.templates.*;
import com.ultimatedungeon.tasks.InvitationExpiryTask;
import com.ultimatedungeon.theme.registry.ThemeRegistry;
import com.ultimatedungeon.theme.themes.*;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

/**
 * Orchestrates plugin startup in strict dependency order.
 *
 * <h3>Phase order</h3>
 * Logger → Version → Registry → Config → Scheduler → Database
 * → Registries (themes, rooms, generator) → Managers (session, cooldown, party,
 * dungeon) → Commands → Listeners → Economy
 */
public final class PluginBootstrap {

    private final UltimateDungeon plugin;

    private PluginLogger         pluginLogger;
    private ServiceRegistry      serviceRegistry;
    private PluginScheduler      pluginScheduler;
    private ConfigManager        configManager;
    private DatabaseManager      databaseManager;
    private VersionDetector      versionDetector;

    // Generation
    private ThemeRegistry        themeRegistry;
    private RoomRegistry         roomRegistry;
    private DungeonGenerator     dungeonGenerator;
    private com.ultimatedungeon.dungeon.world.DungeonWorldManager dungeonWorldManager;
    private DungeonInstanceManager dungeonInstanceManager;
    private GenerationPipeline   generationPipeline;

    // Party
    private PartyManager         partyManager;
    private InvitationManager    invitationManager;
    private ReadyCheckManager    readyCheckManager;

    public PluginBootstrap(@NotNull final UltimateDungeon plugin) {
        this.plugin = plugin;
    }

    public void start() {
        initLogger();
        initVersionDetection();
        initServiceRegistry();
        initConfiguration();
        initScheduler();
        initDatabase();
        initRegistries();
        initManagers();
        initCommands();
        initListeners();
        initEconomy();
        pluginLogger.info("UltimateDungeon v"
                + plugin.getDescription().getVersion() + " enabled successfully.");
    }

    public void shutdown() {
        pluginLogger.info("UltimateDungeon shutdown complete.");
    }

    // ── Init phases ───────────────────────────────────────────────────────────

    private void initLogger() {
        pluginLogger = new PluginLogger(plugin);
        pluginLogger.info("Logger initialised.");
    }

    private void initVersionDetection() {
        versionDetector = new VersionDetector();
        pluginLogger.info("Server: " + versionDetector.getVersionString()
                + (versionDetector.isFolia()     ? " [Folia]"   : "")
                + (versionDetector.isPurpur()    ? " [Purpur]"  : "")
                + (versionDetector.isSpigotOnly()? " [Spigot]"  : " [Paper]"));
    }

    private void initServiceRegistry() {
        serviceRegistry = new ServiceRegistry(pluginLogger);
        serviceRegistry.register(PluginLogger.class,    pluginLogger);
        serviceRegistry.register(VersionDetector.class, versionDetector);
        pluginLogger.info("Service registry initialised.");
    }

    private void initConfiguration() {
        configManager = new ConfigManager(plugin, pluginLogger);
        configManager.load();
        pluginLogger.setDebugMode(configManager.getMainConfig().isDebug());
        serviceRegistry.register(ConfigManager.class, configManager);
        pluginLogger.info("Configuration loaded ("
                + (pluginLogger.isDebugMode() ? "DEBUG ON" : "debug off") + ").");
    }

    private void initScheduler() {
        pluginScheduler = new PluginScheduler(plugin, pluginLogger);
        serviceRegistry.register(PluginScheduler.class, pluginScheduler);
        pluginLogger.info("Scheduler initialised.");
    }

    private void initDatabase() {
        databaseManager = new DatabaseManager(
                configManager.getDatabaseConfig(), pluginLogger, plugin.getDataFolder());
        databaseManager.initialise();
        serviceRegistry.register(DatabaseManager.class, databaseManager);
    }

    private void initRegistries() {
        // ── Theme registry ─────────────────────────────────────────────────────
        themeRegistry = new ThemeRegistry(pluginLogger);
        themeRegistry.register(new AncientRuinsTheme());
        themeRegistry.register(new FrozenCavernTheme());
        themeRegistry.register(new CorruptedTempleTheme());
        themeRegistry.register(new VolcanicFortressTheme());
        themeRegistry.register(new ForgottenCatacombsTheme());
        serviceRegistry.register(ThemeRegistry.class, themeRegistry);
        pluginLogger.info("Themes registered: " + themeRegistry.getAllThemes().size());

        // ── Room template registry ─────────────────────────────────────────────
        roomRegistry = new RoomRegistry(pluginLogger);
        roomRegistry.register(new SpawnRoomTemplate());
        roomRegistry.register(new CombatRoomTemplate());
        roomRegistry.register(new EliteCombatRoomTemplate());
        roomRegistry.register(new TreasureRoomTemplate());
        roomRegistry.register(new PuzzleRoomTemplate());
        roomRegistry.register(new ParkourRoomTemplate());
        roomRegistry.register(new TrapRoomTemplate());
        roomRegistry.register(new SecretRoomTemplate());
        roomRegistry.register(new MerchantRoomTemplate());
        roomRegistry.register(new EventRoomTemplate());
        roomRegistry.register(new MiniBossRoomTemplate());
        roomRegistry.register(new BossRoomTemplate());
        roomRegistry.register(new RewardRoomTemplate());
        serviceRegistry.register(RoomRegistry.class, roomRegistry);
        pluginLogger.info("Room templates registered: " + roomRegistry.getTemplateCount());

        // ── Isolated dungeon world ─────────────────────────────────────────────
        final var worldProvider = new com.ultimatedungeon.dungeon.world.IsolatedWorldProvider(pluginLogger);
        final var worldFactory  = new com.ultimatedungeon.dungeon.world.DungeonWorldFactory(worldProvider, pluginLogger);
        dungeonWorldManager     = new com.ultimatedungeon.dungeon.world.DungeonWorldManager(worldFactory, pluginLogger);
        dungeonWorldManager.initialise();
        serviceRegistry.register(com.ultimatedungeon.dungeon.world.DungeonWorldManager.class, dungeonWorldManager);

        // ── Dungeon generator ──────────────────────────────────────────────────
        dungeonGenerator = new DungeonGenerator(
                configManager.getDungeonConfig(), themeRegistry,
                roomRegistry, pluginScheduler, pluginLogger);
        dungeonGenerator.setWorldManager(dungeonWorldManager);
        serviceRegistry.register(DungeonGenerator.class, dungeonGenerator);

        // ── Dungeon instance manager ───────────────────────────────────────────
        dungeonInstanceManager = new DungeonInstanceManager(pluginLogger);
        serviceRegistry.register(DungeonInstanceManager.class, dungeonInstanceManager);

        // ── Generation pipeline ────────────────────────────────────────────────
        generationPipeline = new GenerationPipeline(
                configManager.getDungeonConfig(), dungeonGenerator,
                dungeonInstanceManager, pluginLogger);
        serviceRegistry.register(GenerationPipeline.class, generationPipeline);

        pluginLogger.info("Generation pipeline ready.");
    }

    private void initManagers() {
        // Session & cooldown
        final PlayerSessionManager sessionManager  = new PlayerSessionManager(pluginLogger);
        final CooldownManager      cooldownManager = new CooldownManager();
        serviceRegistry.register(PlayerSessionManager.class, sessionManager);
        serviceRegistry.register(CooldownManager.class,      cooldownManager);

        // Party services
        final PartyService           partyService       = new PartyService(
                configManager.getMessagesConfig(), pluginLogger, plugin.getServer());
        final PartyValidationService validationService  = new PartyValidationService(
                configManager.getPartyConfig(), sessionManager);
        invitationManager = new InvitationManager(
                configManager.getPartyConfig(), partyService, pluginLogger);
        readyCheckManager = new ReadyCheckManager(
                configManager.getPartyConfig(), configManager.getMessagesConfig(),
                pluginScheduler, pluginLogger, plugin.getServer());
        partyManager = new PartyManager(
                configManager.getPartyConfig(), configManager.getMessagesConfig(),
                partyService, validationService, invitationManager, readyCheckManager,
                pluginLogger);
        serviceRegistry.register(PartyManager.class, partyManager);

        // Invitation expiry task
        pluginScheduler.runSyncRepeating(
                new InvitationExpiryTask(invitationManager)::run,
                20L, configManager.getPerformanceConfig().getInvitationExpiryTicks());

        pluginLogger.info("All managers initialised.");
    }

    private void initCommands() {
        final PartyCommand partyCommand = new PartyCommand(
                partyManager, configManager.getMessagesConfig());
        final var cmd = plugin.getCommand("party");
        if (cmd != null) { cmd.setExecutor(partyCommand); cmd.setTabCompleter(partyCommand); }
        pluginLogger.debug("Dungeon command — Milestone 5.");
    }

    private void initListeners() {
        final PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new PartyPlayerQuitListener(partyManager), plugin);
        pm.registerEvents(new PartyPlayerJoinListener(invitationManager), plugin);
        pluginLogger.info("Listeners registered.");
    }

    private void initEconomy() {
        final VaultEconomyProvider vault = new VaultEconomyProvider(plugin);
        if (vault.isAvailable()) {
            serviceRegistry.register(com.ultimatedungeon.api.economy.IEconomyProvider.class, vault);
            pluginLogger.info("Economy: Vault hooked.");
        } else {
            serviceRegistry.register(com.ultimatedungeon.api.economy.IEconomyProvider.class,
                    new NoOpEconomyProvider());
            pluginLogger.info("Economy: Vault absent.");
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    @NotNull public PluginLogger         getPluginLogger()         { return pluginLogger;         }
    @NotNull public ServiceRegistry      getServiceRegistry()      { return serviceRegistry;      }
    @NotNull public PluginScheduler      getPluginScheduler()      { return pluginScheduler;      }
    @NotNull public ConfigManager        getConfigManager()        { return configManager;        }
    @NotNull public DatabaseManager      getDatabaseManager()      { return databaseManager;      }
    @NotNull public PartyManager         getPartyManager()         { return partyManager;         }
    @NotNull public DungeonGenerator     getDungeonGenerator()     { return dungeonGenerator;     }
    @NotNull public DungeonInstanceManager getDungeonInstanceManager(){ return dungeonInstanceManager; }
    @NotNull public GenerationPipeline   getGenerationPipeline()   { return generationPipeline;   }
}
