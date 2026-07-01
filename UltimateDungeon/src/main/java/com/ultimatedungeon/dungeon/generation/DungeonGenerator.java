package com.ultimatedungeon.dungeon.generation;

import com.ultimatedungeon.api.dungeon.DungeonGenerationRequest;
import com.ultimatedungeon.api.dungeon.IDungeonGenerator;
import com.ultimatedungeon.api.dungeon.IDungeonInstance;
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
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

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

    private final DungeonConfig        dungeonConfig;
    private final ThemeRegistry        themeRegistry;
    private final RoomRegistry         roomRegistry;
    private final LayoutPlanner        layoutPlanner;
    private final CorridorRouter       corridorRouter;
    private final RoomPlacer           roomPlacer;
    private final DecorationPainter    decorationPainter;
    private final GenerationValidator  validator;
    private final PluginScheduler      scheduler;
    private final com.ultimatedungeon.services.DifficultyService difficulty;
    private final PluginLogger         logger;

    /** Optional isolated-world manager; when set, dungeons build in the dungeon world. */
    private DungeonWorldManager        worldManager;

    public DungeonGenerator(
            @NotNull final DungeonConfig       dungeonConfig,
            @NotNull final ThemeRegistry       themeRegistry,
            @NotNull final RoomRegistry        roomRegistry,
            @NotNull final PluginScheduler     scheduler,
            @NotNull final com.ultimatedungeon.services.DifficultyService difficulty,
            @NotNull final PluginLogger        logger
    ) {
        this.dungeonConfig     = dungeonConfig;
        this.themeRegistry     = themeRegistry;
        this.roomRegistry      = roomRegistry;
        this.scheduler         = scheduler;
        this.difficulty        = difficulty;
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

        // Each dungeon gets its own isolated world so instances never overlap.
        // World creation must happen on the main thread (we are on it here).
        final World world = resolveWorld(instanceId);
        if (world == null) {
            future.completeExceptionally(
                    new IllegalStateException("No world available for dungeon generation."));
            return future;
        }

        scheduler.runAsync(() -> {
            try {
                final long startMs = System.currentTimeMillis();
                final GenerationResult result = generateInternal(request, instanceId, world, startMs);
                if (result == null) {
                    discardWorld(instanceId);
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
                        logger.info("Dungeon generated: " + instanceId
                                + " (" + result.getGenerationTimeMs() + "ms, "
                                + result.getRoomGraph().getRoomCount() + " rooms, "
                                + "theme=" + result.getTheme().getThemeId() + ")");
                        future.complete(instance);
                    } catch (final Exception e) {
                        logger.severe("Error during block placement for " + instanceId, e);
                        if (worldManager != null) worldManager.destroyInstanceWorld(instanceId);
                        future.completeExceptionally(e);
                    }
                });
            } catch (final Exception e) {
                logger.severe("Error during async generation for " + instanceId, e);
                discardWorld(instanceId);
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private GenerationResult generateInternal(
            @NotNull final DungeonGenerationRequest request,
            @NotNull final UUID                     instanceId,
            @NotNull final World                    world,
            final long                              startMs
    ) {
        final ThemeDefinition theme = resolveTheme(request.getThemeId());
        if (theme == null) {
            logger.severe("Unknown theme ID: " + request.getThemeId());
            return null;
        }

        // Dungeon level drives its size: level 1 is small, level 4 is large.
        final int level = difficulty.level(request.getDifficultyId());

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            logger.debug("Generation attempt " + attempt + "/" + MAX_RETRIES
                    + " for instance " + instanceId);

            // Full-range int seed. Do NOT use randomInt(MIN, MAX): it adds 1 to
            // the upper bound, overflowing MAX+1 to MIN and throwing on every
            // attempt (which fails all generation).
            final long seed = java.util.concurrent.ThreadLocalRandom.current().nextInt();
            // LayoutPlanner now builds the (wall-to-wall) corridors itself.
            final RoomGraph graph = layoutPlanner.plan(world, theme, seed, level);

            if (validator.validate(graph)) {
                final long elapsed = System.currentTimeMillis() - startMs;
                logger.info("Layout OK [connectivity-model v2]: " + graph.getRoomCount()
                        + " rooms, level " + level + ", all reachable from spawn.");
                return new GenerationResult(instanceId, graph, theme, elapsed);
            }
            logger.debug("Layout invalid on attempt " + attempt + ", retrying...");
        }
        return null; // all retries exhausted
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

    /** Destroys a failed instance's world back on the main thread. */
    private void discardWorld(@NotNull final UUID instanceId) {
        if (worldManager == null) return;
        scheduler.runSync(() -> worldManager.destroyInstanceWorld(instanceId));
    }

    /** Creates this instance's own isolated world (main thread), or falls back. */
    private World resolveWorld(@NotNull final UUID instanceId) {
        if (worldManager != null) {
            final World w = worldManager.createInstanceWorld(instanceId);
            if (w != null) return w;
        }
        return Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
    }
}
