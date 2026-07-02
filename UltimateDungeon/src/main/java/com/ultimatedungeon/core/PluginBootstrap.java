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
import com.ultimatedungeon.monster.engine.MonsterEngine;
import com.ultimatedungeon.monster.engine.MonsterScaler;
import com.ultimatedungeon.monster.engine.MonsterSpawner;
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
import com.ultimatedungeon.tasks.MonsterAITickTask;
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
    private MonsterEngine        monsterEngine;
    private WaveManager          waveManager;
    private TrapEngine           trapEngine;
    private PuzzleEngine         puzzleEngine;
    private BossEngine           bossEngine;
    private com.ultimatedungeon.dungeon.hazard.HazardEngine hazardEngine;
    private com.ultimatedungeon.dungeon.event.DynamicEventEngine dynamicEventEngine;
    private com.ultimatedungeon.dungeon.lifecycle.WaveResetManager waveResetManager;
    private com.ultimatedungeon.dungeon.instance.RoomSealer roomSealer;
    private com.ultimatedungeon.dungeon.instance.EncounterCountdownManager encounterCountdown;
    private ArenaLockdownManager arenaLockdown;
    private com.ultimatedungeon.boss.arena.ArenaCountdownManager arenaCountdown;
    private ArenaCleanupService  arenaCleanup;
    private DungeonLauncher      dungeonLauncher;
    private DungeonEndHandler    dungeonEndHandler;
    private DungeonFailureHandler dungeonFailureHandler;
    private com.ultimatedungeon.dungeon.lifecycle.DungeonScoreService scoreService;
    private com.ultimatedungeon.dungeon.lifecycle.ReviveManager reviveManager;
    private com.ultimatedungeon.services.DungeonKeyService keyService;
    private com.ultimatedungeon.listeners.player.DungeonCompassListener compassListener;
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
                if (monsterEngine != null) monsterEngine.despawnAll(i.getInstanceId());
                if (bossEngine != null) bossEngine.cleanup(i.getInstanceId());
            });
        }
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
        roomRegistry.register(new TrapRoomTemplate());
        roomRegistry.register(new ParkourRoomTemplate());
        roomRegistry.register(new NormalRoomTemplate());
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

        difficultyService = new DifficultyService(configManager.getDifficultyConfig(), pluginLogger);
        dungeonGenerator = new DungeonGenerator(
                configManager.getDungeonConfig(), themeRegistry,
                roomRegistry, pluginScheduler, difficultyService, pluginLogger);
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
        if (difficultyService == null) {
            difficultyService = new DifficultyService(configManager.getDifficultyConfig(), pluginLogger);
        }
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

        // Combat engines
        final MonsterSpawner spawner = new MonsterSpawner(plugin, pluginLogger);
        monsterEngine = new MonsterEngine(configManager.getMonstersConfig(), spawner,
                new MonsterScaler(), difficultyService, pluginLogger);
        waveManager   = new WaveManager(monsterEngine, configManager.getWavesConfig(), pluginLogger);
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
        roomSealer    = new com.ultimatedungeon.dungeon.instance.RoomSealer();
        encounterCountdown = new com.ultimatedungeon.dungeon.instance.EncounterCountdownManager(pluginScheduler);

        // Lifecycle
        scoreService = new com.ultimatedungeon.dungeon.lifecycle.DungeonScoreService();
        final DungeonCleanupService cleanupService = new DungeonCleanupService(
                arenaCleanup, monsterEngine, waveManager, roomSealer, encounterCountdown,
                scoreService, pluginLogger);
        dungeonLauncher = new DungeonLauncher(generationPipeline, dungeonInstanceManager, sessionManager,
                teleportService, notificationService, statisticsService, cleanupService,
                dungeonWorldManager, configManager.getMessagesConfig(), pluginLogger);
        dungeonEndHandler     = new DungeonEndHandler(dungeonLauncher);
        dungeonFailureHandler = new DungeonFailureHandler(dungeonLauncher);

        // Down-and-revive: created after the launcher, then injected back into the
        // cleanup service (which runs first) to break the dependency cycle.
        reviveManager = new com.ultimatedungeon.dungeon.lifecycle.ReviveManager(
                dungeonLauncher, notificationService);
        cleanupService.setReviveManager(reviveManager);

        // Victory lap: completion waits a few seconds before teardown so the
        // rank screen and fireworks play out inside the cleared dungeon.
        dungeonLauncher.setDelayedRunner((task, ticks) -> pluginScheduler.runSyncDelayed(task, ticks));

        // Entry kit: every player gets a Dungeon Tracker compass on the way in.
        compassListener = new com.ultimatedungeon.listeners.player.DungeonCompassListener(
                plugin, dungeonInstanceManager);
        dungeonLauncher.setStartHook((instance, players) -> {
            for (final org.bukkit.entity.Player p : players) {
                if (p.isOnline()) p.getInventory().addItem(compassListener.createTracker());
            }
        });

        // Completion → rewards + graded end screen
        dungeonLauncher.setCompletionHook((instance, players) -> {
            rewardDistributor.distributeAll(players, RewardEvent.DUNGEON_COMPLETION);
            rewardDistributor.distributeAll(players, RewardEvent.BOSS_KILL);
            rewardRoomService.grant(players, "completion_bonus_loot");

            final var score = scoreService.finish(instance);
            // An S or A run earns one extra bonus-loot roll.
            if ("S".equals(score.rank()) || "A".equals(score.rank())) {
                rewardRoomService.grant(players, "completion_bonus_loot");
            }
            // Progression keys (if enabled): grant the key for the next level.
            keyService.grantNextLevelKey(players,
                    difficultyService.level(instance.getContext().getRequest().getDifficultyId()));
            for (final org.bukkit.entity.Player p : players) {
                com.ultimatedungeon.util.MiniMessageUtil.send(p,
                        "<gray>─────── <gold><bold>DUNGEON CLEARED</bold></gold> <gray>───────");
                com.ultimatedungeon.util.MiniMessageUtil.send(p,
                        "<yellow>Rank: " + rankColor(score.rank()) + "<bold>" + score.rank() + "</bold>"
                                + " <dark_gray>(" + score.points() + " pts)");
                com.ultimatedungeon.util.MiniMessageUtil.send(p,
                        "<yellow>Time: <white>" + score.formattedTime()
                                + " <yellow>Deaths: <white>" + score.deaths()
                                + " <yellow>Secrets: <white>" + score.secretsFound());
                com.ultimatedungeon.util.MiniMessageUtil.send(p,
                        "<yellow>Rooms cleared: <white>" + score.roomsCleared() + "/" + score.totalRooms());
                com.ultimatedungeon.util.MiniMessageUtil.send(p,
                        "<gray>──────────────────────────────");
            }
            // Shown after the launcher's "Victory!" title so the grade lands last.
            pluginScheduler.runSyncDelayed(() -> {
                for (final org.bukkit.entity.Player p : players) {
                    if (!p.isOnline()) continue;
                    notificationService.title(p,
                            rankColor(score.rank()) + "<bold>RANK " + score.rank() + "</bold>",
                            "<gray>" + score.formattedTime() + " · " + score.deaths() + " deaths · "
                                    + score.secretsFound() + " secrets");
                    p.playSound(p.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                }
            }, 50L);
            // Firework bursts around each player through the victory lap.
            for (int wave = 0; wave < 3; wave++) {
                pluginScheduler.runSyncDelayed(() -> {
                    for (final org.bukkit.entity.Player p : players) {
                        if (p.isOnline()) celebrationFirework(p.getLocation());
                    }
                }, 20L + wave * 40L);
            }
        });

        // Boss death → open the room; complete the dungeon only when EVERY boss
        // room has been cleared (dungeons can have up to four separate bosses).
        final java.util.Map<java.util.UUID, Integer> bossKills = new java.util.concurrent.ConcurrentHashMap<>();
        bossEngine.setDeathHook((instanceId, bossId) -> {
            arenaLockdown.unlock(instanceId);
            roomSealer.unsealInstance(instanceId); // open this boss room's bedrock exits
            final var instance = dungeonInstanceManager.getInstance(instanceId);
            if (!(instance instanceof final DungeonInstance di)) return;
            final int totalBossRooms = di.getRoomGraph() != null
                    ? Math.max(1, di.getRoomGraph().getBossRoomIds().size()) : 1;
            final int killed = bossKills.merge(instanceId, 1, Integer::sum);
            if (killed >= totalBossRooms) {
                bossKills.remove(instanceId);
                dungeonEndHandler.onComplete(di, bossId);
            }
        });

        keyService = new com.ultimatedungeon.services.DungeonKeyService(
                plugin, configManager.getMainConfig(), notificationService);
        dungeonLaunchService = new DungeonLaunchService(dungeonLauncher, dungeonInstanceManager,
                cooldownManager, difficultyService, themeRegistry, notificationService,
                configManager.getMessagesConfig(), keyService, pluginLogger);

        guiManager = new GuiManager(pluginLogger);
        guiServices = new com.ultimatedungeon.gui.framework.GuiServices(
                guiManager, themeRegistry, configManager.getDifficultyConfig(), partyManager,
                dungeonInstanceManager, invitationManager, readyCheckManager);

        serviceRegistry.register(DungeonLauncher.class,      dungeonLauncher);
        serviceRegistry.register(DungeonLaunchService.class, dungeonLaunchService);
        serviceRegistry.register(MonsterEngine.class,        monsterEngine);
        serviceRegistry.register(TrapEngine.class,           trapEngine);
        serviceRegistry.register(BossEngine.class,           bossEngine);
        serviceRegistry.register(GuiManager.class,           guiManager);

        waveResetManager = new com.ultimatedungeon.dungeon.lifecycle.WaveResetManager(
                dungeonInstanceManager, pluginLogger);

        // Tick tasks
        pluginScheduler.runSyncRepeating(new MonsterAITickTask(monsterEngine, dungeonInstanceManager)::run, 20L, 10L);
        pluginScheduler.runSyncRepeating(new BossAITickTask(bossEngine, dungeonInstanceManager)::run, 20L, 10L);
        pluginScheduler.runSyncRepeating(new TrapTickTask(trapEngine, dungeonInstanceManager)::run, 20L, 20L);
        pluginScheduler.runSyncRepeating(new DungeonTickTask(waveManager, dungeonInstanceManager)::run, 20L, 20L);
        pluginScheduler.runSyncRepeating(waveResetManager::tick, 20L, 20L);
        pluginScheduler.runSyncRepeating(
                new com.ultimatedungeon.tasks.BossProximityTask(dungeonInstanceManager, notificationService)::run,
                40L, 20L);
        if (reviveManager != null) {
            pluginScheduler.runSyncRepeating(reviveManager::tick, 20L, 20L);
        }
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
                arenaLockdown, arenaCountdown, dynamicEventEngine, rewardDistributor,
                difficultyService, waveResetManager,
                configManager.getDungeonConfig().getWaveResetSeconds(),
                roomSealer, encounterCountdown, scoreService), plugin);
        pm.registerEvents(new com.ultimatedungeon.listeners.protection.DungeonProtectionListener(
                dungeonWorldManager), plugin);
        pm.registerEvents(new com.ultimatedungeon.listeners.trap.TrapTriggerListener(
                trapEngine, dungeonInstanceManager), plugin);
        pm.registerEvents(new com.ultimatedungeon.listeners.player.PlayerDeathInDungeonListener(
                plugin, statisticsService, dungeonInstanceManager, scoreService, dungeonFailureHandler), plugin);
        pm.registerEvents(new com.ultimatedungeon.listeners.player.DungeonDownListener(
                dungeonInstanceManager, reviveManager), plugin);
        pm.registerEvents(compassListener, plugin);
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

        // PlaceholderAPI expansion — registered only when the plugin is present.
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                new com.ultimatedungeon.compat.UltimateDungeonExpansion(plugin, statisticsService).register();
                pluginLogger.info("PlaceholderAPI expansion registered.");
            } catch (final Throwable t) {
                pluginLogger.warning("Failed to register PlaceholderAPI expansion: " + t.getMessage());
            }
        }
    }

    /** Launches one celebratory firework near a location. */
    private static void celebrationFirework(@NotNull final org.bukkit.Location loc) {
        if (loc.getWorld() == null) return;
        final org.bukkit.entity.Entity e = loc.getWorld().spawnEntity(
                loc.clone().add(Math.random() * 6 - 3, 1, Math.random() * 6 - 3),
                org.bukkit.entity.EntityType.FIREWORK_ROCKET);
        if (!(e instanceof final org.bukkit.entity.Firework fw)) return;
        final org.bukkit.inventory.meta.FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(org.bukkit.FireworkEffect.builder()
                .withColor(org.bukkit.Color.AQUA, org.bukkit.Color.LIME, org.bukkit.Color.YELLOW)
                .withFade(org.bukkit.Color.WHITE)
                .with(org.bukkit.FireworkEffect.Type.STAR)
                .flicker(true).trail(true).build());
        meta.setPower(1);
        fw.setFireworkMeta(meta);
    }

    /** MiniMessage colour tag for a dungeon rank letter. */
    @NotNull
    private static String rankColor(@NotNull final String rank) {
        return switch (rank) {
            case "S" -> "<gradient:#FFD700:#FFA500>";
            case "A" -> "<green>";
            case "B" -> "<aqua>";
            case "C" -> "<yellow>";
            default  -> "<red>";
        };
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
