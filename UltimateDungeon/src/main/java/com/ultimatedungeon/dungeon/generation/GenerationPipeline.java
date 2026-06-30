package com.ultimatedungeon.dungeon.generation;

import com.ultimatedungeon.api.dungeon.DungeonGenerationRequest;
import com.ultimatedungeon.api.dungeon.IDungeonInstance;
import com.ultimatedungeon.config.files.DungeonConfig;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Orchestrates the full dungeon generation and instance registration flow.
 *
 * <p>Validates capacity limits, delegates to {@link DungeonGenerator}, registers
 * the resulting instance with {@link DungeonInstanceManager}, and invokes the
 * caller's completion / failure callbacks on the main thread.</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * pipeline.launch(request, players,
 *     instance -> teleportPlayers(instance, players),
 *     err      -> notifyPlayers(players, "Generation failed: " + err.getMessage())
 * );
 * }</pre>
 */
public final class GenerationPipeline {

    private final DungeonConfig          dungeonConfig;
    private final DungeonGenerator       generator;
    private final DungeonInstanceManager instanceManager;
    private final PluginLogger           logger;

    public GenerationPipeline(
            @NotNull final DungeonConfig          dungeonConfig,
            @NotNull final DungeonGenerator       generator,
            @NotNull final DungeonInstanceManager instanceManager,
            @NotNull final PluginLogger           logger
    ) {
        this.dungeonConfig   = dungeonConfig;
        this.generator       = generator;
        this.instanceManager = instanceManager;
        this.logger          = logger;
    }

    /**
     * Checks capacity and starts a new dungeon generation pipeline.
     *
     * @param request    the generation request
     * @param players    the players who will enter the dungeon
     * @param onSuccess  called on the main thread with the created instance
     * @param onFailure  called on the main thread if generation fails
     * @return {@code false} if the request was rejected (capacity exceeded)
     */
    public boolean launch(
            @NotNull final DungeonGenerationRequest request,
            @NotNull final List<Player>             players,
            @NotNull final Consumer<IDungeonInstance> onSuccess,
            @NotNull final Consumer<Throwable>        onFailure
    ) {
        if (instanceManager.getActiveCount() >= dungeonConfig.getMaxConcurrentInstances()) {
            logger.debug("Generation rejected: max concurrent instances reached ("
                    + dungeonConfig.getMaxConcurrentInstances() + ").");
            return false;
        }

        logger.info("Generating dungeon for " + players.size() + " player(s) "
                + "[theme=" + request.getThemeId()
                + ", difficulty=" + request.getDifficultyId() + "]");

        final CompletableFuture<IDungeonInstance> future = generator.generate(request);

        future.thenAccept(instance -> {
            instanceManager.registerInstance(instance);
            players.forEach(p -> instanceManager.associatePlayer(p, instance.getInstanceId()));
            logger.info("Dungeon ready: " + instance.getInstanceId());
            onSuccess.accept(instance);
        }).exceptionally(err -> {
            logger.severe("Dungeon generation failed: " + err.getMessage(), err);
            onFailure.accept(err);
            return null;
        });

        return true;
    }
}
