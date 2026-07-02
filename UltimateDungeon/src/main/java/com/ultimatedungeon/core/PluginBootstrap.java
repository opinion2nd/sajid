package com.ultimatedungeon.core;

import com.ultimatedungeon.UltimateDungeon;
import com.ultimatedungeon.api.economy.IEconomyProvider;
import com.ultimatedungeon.boss.arena.ArenaCleanupService;
import com.ultimatedungeon.boss.arena.ArenaLockdownManager;
import com.ultimatedungeon.boss.engine.BossEngine;
import com.ultimatedungeon.commands.DungeonCommand;
import com.ultimatedungeon.commands.PartyCommand;
import com.ultimatedungeon.commands.framework.CommandPermissionChecker;
import com.ultimatedungeon.compat.VersionDetector;
import com.ultimatedungeon.config.ConfigManager;
import com.ultimatedungeon.database.DatabaseManager;
import com.ultimatedungeon.dungeon.generation.*;
import com.ultimatedungeon.dungeon.instance.DungeonCleanupService;
import com.ultimatedungeon.dungeon.instance.DungeonInstance;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.dungeon.lifecycle.DungeonEndHandler;
import com.ultimatedungeon.dungeon.lifecycle.DungeonFailureHandler;
import com.ultimatedungeon.dungeon.lifecycle.DungeonLauncher;
import com.ultimatedungeon.dungeon.world.DungeonWorldFactory;
import com.ultimatedungeon.dungeon.world.DungeonWorldManager;
import com.ultimatedungeon.dungeon.world.IsolatedWorldProvider;
import com.ultimatedungeon.economy.NoOpEconomyProvider;
import com.ultimatedungeon.economy.VaultEconomyProvider;
import com.ultimatedungeon.gui.framework.GuiManager;
import com.ultimatedungeon.listeners.party.PartyPlayerJoinListener;
import com.ultimatedungeon.listeners.party.PartyPlayerQuitListener;
import com.ultimatedungeon.loot.engine.LootGenerator;
import com.ultimatedungeon.loot.engine.RarityRoller;
import com.ultimatedungeon.loot.model.LootRarity;
import com.ultimatedungeon.loot.model.LootTable;
import com.ultimatedungeon.loot.registry.LootTableRegistry;
import com.ultimatedungeon.managers.CooldownManager;
import com.ultimatedungeon.managers.PlayerSessionManager;
import com.ultimatedungeon.monster.engine.WaveManager;
import com.ultimatedungeon.party.manager.InvitationManager;
import com.ultimatedungeon.party.manager.PartyManager;
import com.ultimatedungeon.party.manager.ReadyCheckManager;
import com.ultimatedungeon.party.service.PartyService;
import com.ultimatedungeon.party.service.PartyValidationService;
import com.ultimatedungeon.puzzle.engine.PuzzleEngine;
import com.ultimatedungeon.rewards.engine.RewardDistributor;
import com.ultimatedungeon.rewards.engine.RewardRoomService;
import com.ultimatedungeon.rewards.engine.RewardValidator;
import com.ultimatedungeon.rewards.model.RewardEvent;
import com.ultimatedungeon.room.registry.RoomRegistry;
import com.ultimatedungeon.room.templates.*;
import com.ultimatedungeon.services.DifficultyService;
import com.ultimatedungeon.services.DungeonLaunchService;
import com.ultimatedungeon.services.NotificationService;
import com.ultimatedungeon.services.PlayerTeleportService;
import com.ultimatedungeon.services.StatisticsService;
import com.ultimatedungeon.tasks.BossAITickTask;
import com.ultimatedungeon.tasks.DungeonTickTask;
import com.ultimatedungeon.tasks.InvitationExpiryTask;
import com.ultimatedungeon.tasks.TrapTickTask;
import com.ultimatedungeon.theme.registry.ThemeRegistry;
import com.ultimatedungeon.theme.themes.*;
import com.ultimatedungeon.trap.engine.TrapEngine;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

/**
 * Orchestrates plugin startup in strict dependency order.
 *
 * <h3>Phase order</h3>
 * Logger → Version → Registry → Config → Scheduler → Database → Registries
 * → Managers → Economy → Gameplay (loot, rewards, combat engines, lifecycle)
 * → Commands → Listeners.
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
    private DungeonWorldManager  dungeonWorldManager;
    private DungeonInstanceManager dungeonInstanceManager;
    private GenerationPipeline   generationPipeline;

    // Managers
    private PlayerSessionManager sessionManager;
    private CooldownManager      cooldownManager;
    private PartyManager         partyManager;
    private InvitationManager    invitationManager;
    private ReadyCheckManager    readyCheckManager;

    // Economy
    private IEconomyProvider     economyProvider;

    // Gameplay services / engines
    private DifficultyService    difficultyService;
    private NotificationService  notificationService;
    private PlayerTeleportService teleportService;
    private StatisticsService    statisticsService;
    private LootTableRegistry    lootTableRegistry;
    private LootGenerator        lootGenerator;
    private RewardDistributor    rewardDistributor;
    private RewardRoomService    rewardRoomService;
    private WaveManager          waveManager;
    private com.ultimatedungeon.services.DungeonScoreboardManager scoreboardManager;
    private com.ultimatedungeon.services.ReviveManager reviveManager;
    private TrapEngine           trapEngine;
    private PuzzleEngine         puzzleEngine;
    private BossEngine           bossEngine;
    private com.ultimatedungeon.dungeon.hazard.HazardEngine hazardEngine;
    private com.ultimatedungeon.dungeon.event.DynamicEventEngine dynamicEventEngine;
    private ArenaLockdownManager arenaLockdown;
    private com.ultimatedungeon.boss.arena.ArenaCountdownManager arenaCountdown;
    private ArenaCleanupService  arenaCleanup;
    private DungeonLauncher      dungeonLauncher;
    private DungeonEndHandler    dungeonEndHandler;
    private DungeonFailureHandler dungeonFailureHandler;
    private DungeonLaunchService dungeonLaunchService;
    private GuiManager           guiManager;
    private com.ultimatedungeon.gui.framework.GuiServices guiServices;

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
        initEconomy();
        initGameplay();
        initCommands();
        initListeners();
        pluginLogger.info("UltimateDungeon v"
                + plugin.getDescription().getVersion() + " enabled successfully.");
    }

    public void shutdown() {
        // Scheduler and database are torn down by PluginShutdownHandler; here we
        // just remove any spawned dungeon entities so none are orphaned.
        if (dungeonInstanceManager != null) {
            dungeonInstanceManager.getActiveInstances().forEach(i -> {
                if (waveManager != null) waveManager.despawnAll(i.getInstanceId());
                if (bossEngine != null) bossEngine.cleanup(i.getInstanceId());
                if (dungeonGenerator != null) dungeonGenerator.releaseOrigin(i.getInstanceId());
            });
        }
        if (scoreboardManager != null) scoreboardManager.restoreAll();
        if (reviveManager != null) reviveManager.restoreAll();
        if (pluginLogger != null) pluginLogger.info("UltimateDungeon shutdown complete.");
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
        // Construct compat adapters for their startup detection/logging side effects.
        new com.ultimatedungeon.compat.FoliaCompatAdapter(versionDetector, pluginLogger);
        new com.ultimatedungeon.compat.PurpurCompatAdapter(versionDetector, pluginLogger);
        new com.ultimatedungeon.compat.SpigotCompatAdapter(versionDetector, pluginLogger);
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
        themeRegistry = new ThemeRegistry(pluginLogger);
        themeRegistry.register(new AncientRuinsTheme());
        themeRegistry.register(new FrozenCavernTheme());
        themeRegistry.register(new CorruptedTempleTheme());
        themeRegistry.register(new VolcanicFortressTheme());
        themeRegistry.register(new ForgottenCatacombsTheme());
        serviceRegistry.register(ThemeRegistry.class, themeRegistry);
        pluginLogger.info("Themes registered: " + themeRegistry.getAllThemes().size());

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

        // Isolated dungeon world
        final IsolatedWorldProvider worldProvider = new IsolatedWorldProvider(pluginLogger);
        final DungeonWorldFactory worldFactory = new DungeonWorldFactory(worldProvider, pluginLogger);
        dungeonWorldManager = new DungeonWorldManager(worldFactory, pluginLogger);
        dungeonWorldManager.initialise();
        serviceRegistry.register(DungeonWorldManager.class, dungeonWorldManager);

        dungeonGenerator = new DungeonGenerator(
                configManager.getDungeonConfig(), configManager.getDifficultyConfig(),
                themeRegistry, roomRegistry, pluginScheduler, pluginLogger);
        dungeonGenerator.setWorldManager(dungeonWorldManager);
        serviceRegistry.register(DungeonGenerator.class, dungeonGenerator);

        dungeonInstanceManager = new DungeonInstanceManager(pluginLogger);
        serviceRegistry.register(DungeonInstanceManager.class, dungeonInstanceManager);

        generationPipeline = new GenerationPipeline(
                configManager.getDungeonConfig(), dungeonGenerator,
                dungeonInstanceManager, pluginLogger);
        serviceRegistry.register(GenerationPipeline.class, generationPipeline);

        pluginLogger.info("Generation pipeline ready.");
    }

    private void initManagers() {
        sessionManager  = new PlayerSessionManager(pluginLogger);
        cooldownManager = new CooldownManager();
        serviceRegistry.register(PlayerSessionManager.class, sessionManager);
        serviceRegistry.register(CooldownManager.class,      cooldownManager);

        final PartyService           partyService      = new PartyService(
                configManager.getMessagesConfig(), pluginLogger, plugin.getServer());
        final PartyValidationService validationService = new PartyValidationService(
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

        pluginScheduler.runSyncRepeating(
                new InvitationExpiryTask(invitationManager)::run,
                20L, configManager.getPerformanceConfig().getInvitationExpiryTicks());

        pluginLogger.info("All managers initialised.");
    }

    private void initEconomy() {
        final VaultEconomyProvider vault = new VaultEconomyProvider(plugin);
        economyProvider = vault.isAvailable() ? vault : new NoOpEconomyProvider();
        serviceRegistry.register(IEconomyProvider.class, economyProvider);
        pluginLogger.info("Economy: " + (vault.isAvailable() ? "Vault hooked." : "Vault absent (no-op)."));
    }

    private void initGameplay() {
        difficultyService   = new DifficultyService(configManager.getDifficultyConfig(), pluginLogger);
        notificationService = new NotificationService(pluginLogger);
        teleportService     = new PlayerTeleportService(pluginLogger);
        statisticsService   = new StatisticsService(databaseManager, pluginScheduler, pluginLogger);

        // Loot
        lootTableRegistry = new LootTableRegistry(pluginLogger);
        loadLootTables();
        lootGenerator = new LootGenerator(lootTableRegistry, new RarityRoller(pluginLogger), pluginLogger);

        // Rewards
        final RewardValidator rewardValidator = new RewardValidator(pluginLogger);
        rewardDistributor = new RewardDistributor(configManager.getRewardsConfig(), economyProvider,
                lootGenerator, notificationService, configManager.getMessagesConfig(),
                statisticsService, rewardValidator, pluginLogger);
        rewardRoomService = new RewardRoomService(lootGenerator, notificationService, pluginLogger);

        // Combat engines — waves are fully driven by waves.yml
        waveManager   = new WaveManager(configManager.getWavesConfig(), pluginLogger);
        trapEngine    = new TrapEngine(configManager.getTrapsConfig(), difficultyService, pluginLogger);
        puzzleEngine  = new PuzzleEngine(pluginLogger);
        bossEngine    = new BossEngine(plugin, configManager.getBossesConfig(), difficultyService, pluginLogger);

        // Environmental hazards & dynamic events (config-driven)
        hazardEngine = new com.ultimatedungeon.dungeon.hazard.HazardEngine(
                configManager.getDungeonConfig().getHazardSettings());
        dynamicEventEngine = new com.ultimatedungeon.dungeon.event.DynamicEventEngine(
                configManager.getDungeonConfig().getDynamicEventSettings(),
                waveManager, rewardRoomService, notificationService, pluginLogger);

        // Arena
        arenaLockdown = new ArenaLockdownManager(pluginLogger);
        arenaCountdown = new com.ultimatedungeon.boss.arena.ArenaCountdownManager(pluginScheduler, pluginLogger);
        arenaCleanup  = new ArenaCleanupService(arenaLockdown, bossEngine, pluginLogger);

        // Party revive: downed players wait at their body for a teammate.
        reviveManager = new com.ultimatedungeon.services.ReviveManager(
                dungeonInstanceManager, configManager.getPartyConfig(), pluginLogger);
        pluginScheduler.runSyncRepeating(reviveManager::tick, 20L, 10L);

        // Lifecycle — every engine contributes its own instance-cleanup action
        final DungeonCleanupService cleanupService = new DungeonCleanupService(pluginLogger);
        cleanupService.registerAction(reviveManager::clearInstance);
        cleanupService.registerAction(waveManager::despawnAll);
        cleanupService.registerAction(bossEngine::cleanup);
        cleanupService.registerAction(arenaLockdown::unlock);
        // Despawn the dungeon's blocks and free its map slot afterwards.
        cleanupService.registerInstanceAction(dungeonGenerator::clearInstanceBlocks);
        dungeonLauncher = new DungeonLauncher(generationPipeline, dungeonInstanceManager, sessionManager,
                teleportService, notificationService, statisticsService, cleanupService,
                configManager.getMessagesConfig(), pluginLogger);
        dungeonEndHandler     = new DungeonEndHandler(dungeonLauncher);
        dungeonFailureHandler = new DungeonFailureHandler(dungeonLauncher);

        // Completion → rewards
        dungeonLauncher.setCompletionHook((instance, players) -> {
            rewardDistributor.distributeAll(players, RewardEvent.DUNGEON_COMPLETION);
            rewardDistributor.distributeAll(players, RewardEvent.BOSS_KILL);
            rewardRoomService.grant(players, "completion_bonus_loot");
        });

        // A downed player nobody saved leaves; an empty dungeon fails.
        reviveManager.setTimeoutHook(player -> {
            final var raw = dungeonInstanceManager.getInstanceForPlayer(player);
            dungeonLauncher.leave(player);
            if (raw instanceof final DungeonInstance di && di.isActive()
                    && dungeonLauncher.getPlayers(di.getInstanceId()).isEmpty()) {
                dungeonFailureHandler.onFailure(di);
            }
        });

        // Boss death → unseal THAT boss room, count the kill; the run completes
        // only when EVERY boss the level demands has been defeated.
        bossEngine.setDeathHook((instanceId, bossId, roomId) -> {
            if (roomId != null) arenaLockdown.unlock(instanceId, roomId);
            final var instance = dungeonInstanceManager.getInstance(instanceId);
            if (instance instanceof final DungeonInstance di) {
                final int defeated = di.addBossKill();
                if (di.allBossesDefeated()) {
                    arenaCleanup.cleanup(instanceId);
                    dungeonEndHandler.onComplete(di, bossId);
                } else {
                    final int left = di.getTotalBosses() - defeated;
                    dungeonLauncher.getPlayers(instanceId).forEach(p ->
                            notificationService.title(p, "<gold>Boss defeated!",
                                    "<gray>" + left + " boss(es) remaining"));
                }
            }
        });

        dungeonLaunchService = new DungeonLaunchService(dungeonLauncher, dungeonInstanceManager,
                cooldownManager, difficultyService, themeRegistry, notificationService,
                configManager.getMessagesConfig(), pluginLogger);

        guiManager = new GuiManager(pluginLogger);
        guiServices = new com.ultimatedungeon.gui.framework.GuiServices(
                guiManager, themeRegistry, configManager.getDifficultyConfig(), partyManager,
                dungeonInstanceManager, invitationManager, readyCheckManager);

        serviceRegistry.register(DungeonLauncher.class,      dungeonLauncher);
        serviceRegistry.register(DungeonLaunchService.class, dungeonLaunchService);
        serviceRegistry.register(TrapEngine.class,           trapEngine);
        serviceRegistry.register(BossEngine.class,           bossEngine);
        serviceRegistry.register(GuiManager.class,           guiManager);

        // Tick tasks
        pluginScheduler.runSyncRepeating(new BossAITickTask(bossEngine, dungeonInstanceManager)::run, 20L, 10L);
        pluginScheduler.runSyncRepeating(new TrapTickTask(trapEngine, dungeonInstanceManager)::run, 20L, 20L);
        pluginScheduler.runSyncRepeating(new DungeonTickTask(waveManager, dungeonInstanceManager)::run, 20L, 20L);

        // In-dungeon scoreboard (scoreboards.yml) — only dungeon players see it
        scoreboardManager = new com.ultimatedungeon.services.DungeonScoreboardManager(
                configManager.getScoreboardsConfig(), dungeonInstanceManager, waveManager);
        pluginScheduler.runSyncRepeating(scoreboardManager::updateAll, 20L,
                configManager.getScoreboardsConfig().getUpdateIntervalTicks());
        if (hazardEngine.isActive()) {
            pluginScheduler.runSyncRepeating(
                    new com.ultimatedungeon.tasks.HazardTickTask(hazardEngine, dungeonInstanceManager)::run,
                    20L, hazardEngine.getTickIntervalTicks());
        }

        pluginLogger.info("Gameplay systems initialised.");
    }

    private void loadLootTables() {
        final ConfigurationSection raw = configManager.getLootConfig().raw();
        final Map<LootRarity, Double> rarityChances = new EnumMap<>(LootRarity.class);
        final ConfigurationSection rc = raw.getConfigurationSection("rarity-chances");
        if (rc != null) {
            for (final String key : rc.getKeys(false)) {
                try {
                    rarityChances.put(LootRarity.valueOf(key.toUpperCase()), rc.getDouble(key));
                } catch (final IllegalArgumentException ignored) {
                    // Unknown rarity key — skip.
                }
            }
        }
        final ConfigurationSection tables = raw.getConfigurationSection("loot-tables");
        if (tables != null) {
            for (final String id : tables.getKeys(false)) {
                final ConfigurationSection s = tables.getConfigurationSection(id);
                if (s != null) lootTableRegistry.register(LootTable.fromSection(id, s, rarityChances));
            }
        }
        serviceRegistry.register(LootTableRegistry.class, lootTableRegistry);
        pluginLogger.info("Loot tables registered: " + lootTableRegistry.getAllTables().size());
    }

    private void initCommands() {
        final PartyCommand partyCommand = new PartyCommand(
                partyManager, configManager.getMessagesConfig());
        final var partyCmd = plugin.getCommand("party");
        if (partyCmd != null) { partyCmd.setExecutor(partyCommand); partyCmd.setTabCompleter(partyCommand); }

        final DungeonCommand dungeonCommand = new DungeonCommand(
                new CommandPermissionChecker(pluginLogger), dungeonLaunchService, dungeonLauncher,
                dungeonInstanceManager, statisticsService, partyManager, themeRegistry, configManager, guiServices);
        final var dungeonCmd = plugin.getCommand("dungeon");
        if (dungeonCmd != null) { dungeonCmd.setExecutor(dungeonCommand); dungeonCmd.setTabCompleter(dungeonCommand); }

        pluginLogger.info("Commands registered.");
    }

    private void initListeners() {
        final PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new PartyPlayerQuitListener(partyManager), plugin);
        pm.registerEvents(new PartyPlayerJoinListener(invitationManager), plugin);

        // Gameplay activation & safety listeners
        pm.registerEvents(new com.ultimatedungeon.listeners.room.RoomEnterListener(
                dungeonInstanceManager, waveManager, trapEngine, puzzleEngine, bossEngine,
                arenaLockdown, arenaCountdown, dynamicEventEngine, rewardDistributor), plugin);
        pm.registerEvents(new com.ultimatedungeon.listeners.trap.TrapTriggerListener(
                trapEngine, dungeonInstanceManager), plugin);
        pm.registerEvents(new com.ultimatedungeon.listeners.player.PlayerDeathInDungeonListener(
                plugin, statisticsService, dungeonInstanceManager,
                dungeonLauncher, dungeonFailureHandler, reviveManager), plugin);
        pm.registerEvents(new com.ultimatedungeon.listeners.dungeon.DungeonBlockProtectionListener(
                dungeonWorldManager), plugin);
        pm.registerEvents(new com.ultimatedungeon.listeners.room.FateChestListener(
                dungeonInstanceManager, arenaLockdown, waveManager, rewardRoomService), plugin);
        pm.registerEvents(new com.ultimatedungeon.listeners.arena.ArenaEscapeListener(
                arenaLockdown, new com.ultimatedungeon.boss.arena.ArenaEscapeBlocker(), dungeonInstanceManager), plugin);
        pm.registerEvents(new com.ultimatedungeon.listeners.gui.GuiClickListener(guiManager), plugin);
        pm.registerEvents(new com.ultimatedungeon.listeners.puzzle.PuzzleInteractListener(
                puzzleEngine, dungeonInstanceManager), plugin);
        pm.registerEvents(new com.ultimatedungeon.listeners.arena.ArenaPortalBlockListener(
                arenaLockdown, dungeonInstanceManager), plugin);
        pm.registerEvents(new com.ultimatedungeon.listeners.arena.ArenaTeleportBlockListener(
                arenaLockdown, dungeonInstanceManager), plugin);
        pm.registerEvents(new com.ultimatedungeon.listeners.arena.ArenaCommandBlockListener(
                arenaLockdown, dungeonInstanceManager), plugin);

        pluginLogger.info("Listeners registered.");
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
