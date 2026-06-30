package com.ultimatedungeon.dungeon.generation;

import com.ultimatedungeon.api.dungeon.DungeonGenerationRequest;
import com.ultimatedungeon.api.dungeon.IDungeonGenerator;
import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.config.files.DungeonConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.core.PluginScheduler;
import com.ultimatedungeon.dungeon.instance.DungeonContext;
import com.ultimatedungeon.dungeon.instance.DungeonInstance;
import com.ultimatedungeon.room.model.RoomGraph;
import com.ultimatedungeon.room.registry.RoomRegistry;
import com.ultimatedungeon.theme.model.ThemeDefinition;
import com.ultimatedungeon.theme.registry.ThemeRegistry;
import com.ultimatedungeon.util.RandomUtil;
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
    private final PluginLogger         logger;

    public DungeonGenerator(
            @NotNull final DungeonConfig       dungeonConfig,
            @NotNull final ThemeRegistry       themeRegistry,
            @NotNull final RoomRegistry        roomRegistry,
            @NotNull final PluginScheduler     scheduler,
            @NotNull final PluginLogger        logger
    ) {
        this.dungeonConfig     = dungeonConfig;
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

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            logger.debug("Generation attempt " + attempt + "/" + MAX_RETRIES
                    + " for instance " + instanceId);

            final long seed = RandomUtil.randomInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
            final RoomGraph graph = layoutPlanner.plan(world, theme, seed);
            corridorRouter.route(graph);

            if (validator.validate(graph)) {
                final long elapsed = System.currentTimeMillis() - startMs;
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

    private World resolveWorld(@NotNull final DungeonGenerationRequest request) {
        // For now use the default world — world isolation is handled in Milestone 3
        return Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
    }
}
