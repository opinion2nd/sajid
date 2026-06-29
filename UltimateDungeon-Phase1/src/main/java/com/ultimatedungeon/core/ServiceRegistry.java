package com.ultimatedungeon.core;

import com.ultimatedungeon.UltimateDungeon;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Central service locator / dependency injection container.
 *
 * <p>All singleton-scoped services and managers are registered here during
 * startup and retrieved by any system that needs them. This eliminates
 * direct static references between systems and keeps coupling low.</p>
 *
 * <p>Services are keyed by their interface or class type. Registration
 * order must match the dependency graph defined in {@link PluginBootstrap}.</p>
 */
public final class ServiceRegistry {

    private final UltimateDungeon plugin;
    private final PluginLogger logger;
    private final Map<Class<?>, Object> services = new HashMap<>();

    public ServiceRegistry(
            @NotNull final UltimateDungeon plugin,
            @NotNull final PluginLogger logger
    ) {
        this.plugin = plugin;
        this.logger = logger;
    }

    // ── Registration ──────────────────────────────────────────────────────────

    /**
     * Registers a service instance under the given type key.
     *
     * @param type    the interface or class used as the lookup key
     * @param service the service instance to register
     * @param <T>     the service type
     * @throws IllegalStateException if a service for this type is already registered
     */
    public <T> void register(@NotNull final Class<T> type, @NotNull final T service) {
        if (services.containsKey(type)) {
            throw new IllegalStateException(
                    "Service already registered for type: " + type.getName()
            );
        }
        services.put(type, service);
        logger.debug("Registered service: " + type.getSimpleName());
    }

    // ── Retrieval ─────────────────────────────────────────────────────────────

    /**
     * Retrieves a registered service by its type key.
     *
     * @param type the interface or class key
     * @param <T>  the service type
     * @return the registered service instance
     * @throws IllegalStateException if no service is registered for the given type
     */
    @NotNull
    public <T> T get(@NotNull final Class<T> type) {
        final Object service = services.get(type);
        if (service == null) {
            throw new IllegalStateException(
                    "No service registered for type: " + type.getName()
            );
        }
        return type.cast(service);
    }

    /**
     * Returns true if a service is registered for the given type.
     *
     * @param type the type key to check
     * @return true if registered, false otherwise
     */
    public boolean isRegistered(@NotNull final Class<?> type) {
        return services.containsKey(type);
    }

    // ── Shutdown ──────────────────────────────────────────────────────────────

    /**
     * Clears all registered services. Called during plugin shutdown.
     */
    public void clear() {
        services.clear();
        logger.debug("Service registry cleared.");
    }
}
