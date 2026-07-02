package com.ultimatedungeon.dungeon.generation;

import com.ultimatedungeon.api.dungeon.DungeonGenerationRequest;
import com.ultimatedungeon.api.dungeon.IDungeonGenerator;
import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.config.files.DifficultyConfig;
import com.ultimatedungeon.config.files.DungeonConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.core.PluginScheduler;
import com.ultimatedungeon.dungeon.instance.DungeonContext;
import com.ultimatedungeon.dungeon.instance.DungeonInstance;
import com.ultimatedungeon.dungeon.world.DungeonWorldManager;
import com.ultimatedungeon.room.model.RoomGraph;
import com.ultimatedungeon.room.registry.RoomRegistry;
import com.ultimatedungeon.theme.model.ThemeDefinition;
import com.ultimatedungeon.theme.registry.ThemeRegistry;
import com.ultimatedungeon.util.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Top-level dungeon generator — implements {@link IDungeonGenerator}.
 *
 * <h3>Pipeline</h3>
 * All CPU-heavy steps (layout planning, corridor routing) run on an async
 * thread via {@link PluginScheduler#runAsync}. Block placement is then
 * dispatched back to the main thread via {@link PluginScheduler#runSync}.
 *
 * <h3>Retry on invalid layout</h3>
 * If {@link GenerationValidator} rejects a layout the generator retries
 * automatically up to {@value #MAX_RETRIES} times before returning a
 * failed future.
 */
public final class DungeonGenerator implements IDungeonGenerator {

    private static final int MAX_RETRIES = 5;

    /** Distance between instance origins in the shared dungeon world. */
    private static final int INSTANCE_SPACING = 4096;
    /** Maximum origin slots on an 8×8 grid — far above max-concurrent-instances. */
    private static final int ORIGIN_SLOTS = 64;

    /** Origin slot currently claimed by each live instance. */
    private final Map<UUID, Integer> claimedSlots = new java.util.concurrent.ConcurrentHashMap<>();

    private final DungeonConfig        dungeonConfig;
    private final DifficultyConfig     difficultyConfig;
    private final ThemeRegistry        themeRegistry;
    private final RoomRegistry         roomRegistry;
    private final LayoutPlanner        layoutPlanner;
    private final CorridorRouter       corridorRouter;
    private final RoomPlacer           roomPlacer;
    private final DecorationPainter    decorationPainter;
    private final GenerationValidator  validator;
    private final PluginScheduler      scheduler;
    private final PluginLogger         logger;

    /** Optional isolated-world manager; when set, dungeons build in the dungeon world. */
    private DungeonWorldManager        worldManager;

    public DungeonGenerator(
            @NotNull final DungeonConfig       dungeonConfig,
            @NotNull final DifficultyConfig    difficultyConfig,
            @NotNull final ThemeRegistry       themeRegistry,
            @NotNull final RoomRegistry        roomRegistry,
            @NotNull final PluginScheduler     scheduler,
            @NotNull final PluginLogger        logger
    ) {
        this.dungeonConfig     = dungeonConfig;
        this.difficultyConfig  = difficultyConfig;
        this.themeRegistry     = themeRegistry;
        this.roomRegistry      = roomRegistry;
        this.scheduler         = scheduler;
        this.logger            = logger;
        this.layoutPlanner     = new LayoutPlanner(dungeonConfig, roomRegistry, logger);
        this.corridorRouter    = new CorridorRouter(dungeonConfig, logger);
        this.roomPlacer        = new RoomPlacer(roomRegistry, logger);
        this.decorationPainter = new DecorationPainter(dungeonConfig, logger);
        this.validator         = new GenerationValidator(logger);
    }

    /** Injects the dungeon-world manager so generation targets the isolated world. */
    public void setWorldManager(@NotNull final DungeonWorldManager worldManager) {
        this.worldManager = worldManager;
    }

    // ── IDungeonGenerator ─────────────────────────────────────────────────────

    @Override
    @NotNull
    public CompletableFuture<IDungeonInstance> generate(
            @NotNull final DungeonGenerationRequest request
    ) {
        final CompletableFuture<IDungeonInstance> future = new CompletableFuture<>();
        final UUID instanceId = UUID.randomUUID();

        scheduler.runAsync(() -> {
            try {
                final long startMs = System.currentTimeMillis();
                final GenerationResult result = generateInternal(request, instanceId, startMs);
                if (result == null) {
                    future.completeExceptionally(
                            new IllegalStateException("Dungeon generation failed after "
                                    + MAX_RETRIES + " attempts."));
                    return;
                }
                // Block placement must happen on the main thread
                scheduler.runSync(() -> {
                    try {
                        roomPlacer.placeAll(result.getRoomGraph(),
                                result.getTheme().getPalette());
                        decorationPainter.paintAll(result.getRoomGraph(),
                                result.getTheme().getPalette());
                        final DungeonContext  ctx      = new DungeonContext(instanceId, request);
                        final DungeonInstance instance = new DungeonInstance(ctx);
                        instance.setRoomGraph(result.getRoomGraph());
                        instance.setTheme(result.getTheme());
                        instance.setTotalBosses(Math.max(1, difficultyConfig
                                .getPresetOrDefault(request.getDifficultyId()).bossCount()));
                        logger.info("Dungeon generated: " + instanceId
                                + " (" + result.getGenerationTimeMs() + "ms, "
                                + result.getRoomGraph().getRoomCount() + " rooms, "
                                + "theme=" + result.getTheme().getThemeId() + ")");
                        future.complete(instance);
                    } catch (final Exception e) {
                        logger.severe("Error during block placement for " + instanceId, e);
                        future.completeExceptionally(e);
                    }
                });
            } catch (final Exception e) {
                logger.severe("Error during async generation for " + instanceId, e);
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private GenerationResult generateInternal(
            @NotNull final DungeonGenerationRequest request,
            @NotNull final UUID                     instanceId,
            final long                              startMs
    ) {
        final ThemeDefinition theme = resolveTheme(request.getThemeId());
        if (theme == null) {
            logger.severe("Unknown theme ID: " + request.getThemeId());
            return null;
        }

        final World world = resolveWorld(request);
        if (world == null) {
            logger.severe("No world available for dungeon generation.");
            return null;
        }

        final int targetRooms = resolveTargetRooms(request.getDifficultyId());
        final int bossCount = Math.max(1, difficultyConfig
                .getPresetOrDefault(request.getDifficultyId()).bossCount());
        final Location origin = claimInstanceOrigin(instanceId, world);

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            logger.debug("Generation attempt " + attempt + "/" + MAX_RETRIES
                    + " for instance " + instanceId);

            // Fresh full-range seed per attempt — every run gets a unique layout.
            final long seed = RandomUtil.randomSeed();
            final RoomGraph graph = layoutPlanner.plan(world, theme, seed, targetRooms, bossCount, origin);
            corridorRouter.route(graph);

            if (validator.validate(graph)) {
                final long elapsed = System.currentTimeMillis() - startMs;
                return new GenerationResult(instanceId, graph, theme, elapsed);
            }
            logger.debug("Layout invalid on attempt " + attempt + ", retrying...");
        }
        return null; // all retries exhausted
    }

    /**
     * Room budget for this run: the selected level defines the map size, so
     * higher levels produce larger dungeons. Invalid or missing config values
     * fall back to safe defaults with a warning rather than crashing.
     */
    private int resolveTargetRooms(@NotNull final String difficultyId) {
        final DifficultyConfig.DifficultyPreset preset =
                difficultyConfig.getPresetOrDefault(difficultyId);
        int min = preset.roomsMin() > 0 ? preset.roomsMin() : dungeonConfig.getDungeonSizeMin();
        int max = preset.roomsMax() > 0 ? preset.roomsMax() : dungeonConfig.getDungeonSizeMax();
        if (min <= 0 || max <= 0 || min > max) {
            logger.warning("Invalid room range for level '" + difficultyId
                    + "' (" + min + "-" + max + ") — using safe defaults 10-14.");
        }
        return RandomUtil.safeRange(min, max, 6, 80);
    }

    /**
     * Claims a free, well-separated origin slot on an 8×8 grid for this
     * instance so concurrent dungeons (solo or party) never overlap. The slot
     * is released again in {@link #releaseOrigin} during cleanup.
     */
    @NotNull
    private synchronized Location claimInstanceOrigin(@NotNull final UUID instanceId,
                                                      @NotNull final World world) {
        int slot = 0;
        final var used = new java.util.HashSet<>(claimedSlots.values());
        while (used.contains(slot) && slot < ORIGIN_SLOTS) slot++;
        claimedSlots.put(instanceId, slot);
        final int x = (slot % 8) * INSTANCE_SPACING;
        final int z = (slot / 8) * INSTANCE_SPACING;
        logger.debug("Instance " + instanceId + " claimed origin slot " + slot);
        return new Location(world, x, 64, z);
    }

    /** Frees the origin slot held by an instance once its dungeon is cleaned up. */
    public void releaseOrigin(@NotNull final UUID instanceId) {
        final Integer slot = claimedSlots.remove(instanceId);
        if (slot != null) logger.debug("Instance " + instanceId + " released origin slot " + slot);
    }

    /**
     * Despawns a finished dungeon's BLOCKS: every room is cleared to air (one
     * room per tick to avoid a lag spike), then the corridors, and finally the
     * origin slot is released so the area can host a fresh dungeon. Without
     * this, old dungeons piled up and new ones generated into their leftovers.
     */
    public void clearInstanceBlocks(@NotNull final com.ultimatedungeon.dungeon.instance.DungeonInstance instance) {
        final RoomGraph graph = instance.getRoomGraph();
        final UUID id = instance.getInstanceId();
        if (graph == null) {
            releaseOrigin(id);
            return;
        }
        final var rooms = new java.util.ArrayList<>(graph.getRooms());
        for (int i = 0; i < rooms.size(); i++) {
            final var room = rooms.get(i);
            scheduler.runSyncDelayed(() -> roomPlacer.clearRoom(room), i + 1L);
        }
        scheduler.runSyncDelayed(() -> {
            for (final var conn : graph.getConnections()) {
                roomPlacer.clearCorridor(conn);
            }
            releaseOrigin(id);
            logger.info("Dungeon blocks despawned for instance " + id);
        }, rooms.size() + 2L);
    }

    private ThemeDefinition resolveTheme(@NotNull final String themeId) {
        final var theme = themeRegistry.getTheme(themeId);
        if (theme instanceof final ThemeDefinition def) return def;
        // Fall back to a random registered theme
        return themeRegistry.getAllThemes().stream()
                .filter(t -> t instanceof ThemeDefinition)
                .map(t -> (ThemeDefinition) t)
                .findFirst().orElse(null);
    }

    private World resolveWorld(@NotNull final DungeonGenerationRequest request) {
        // Prefer the isolated dungeon world; fall back to the default world only
        // if world isolation is unavailable.
        if (worldManager != null && worldManager.getDungeonWorld() != null) {
            return worldManager.getDungeonWorld();
        }
        return Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
    }
}
