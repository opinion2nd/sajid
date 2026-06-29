package com.ultimatedungeon.core;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Typed dependency injection container for UltimateDungeon.
 *
 * <p>Every singleton-scoped service or manager is registered here by its
 * interface or concrete class type and retrieved by the same key. This
 * eliminates static singletons and direct inter-system coupling.</p>
 *
 * <p>Registration order must respect the dependency graph — a service must be
 * registered before any other service that needs it. Attempting to register the
 * same type twice throws immediately so mis-wiring is caught at startup.</p>
 *
 * <p>The underlying map is a {@link LinkedHashMap} so registration order is
 * preserved and deterministic shutdown (reverse order) is straightforward.</p>
 *
 * <p>This class is <strong>not</strong> thread-safe; all registrations happen
 * on the main thread during startup before any async work begins.</p>
 */
public final class ServiceRegistry {

    private final PluginLogger logger;

    /** Preserves insertion order so callers can iterate in registration sequence. */
    private final Map<Class<?>, Object> services = new LinkedHashMap<>();

    public ServiceRegistry(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    // ── Registration ──────────────────────────────────────────────────────────

    /**
     * Registers {@code service} under {@code type}.
     *
     * @param type    interface or class key — callers retrieve with the same type
     * @param service the instance to store
     * @param <T>     service type
     * @throws IllegalStateException if a service is already registered for {@code type}
     * @throws IllegalArgumentException if {@code service} is not assignable to {@code type}
     */
    public <T> void register(@NotNull final Class<T> type, @NotNull final T service) {
        if (services.containsKey(type)) {
            throw new IllegalStateException(
                "Duplicate registration — a service for '"
                + type.getName() + "' is already registered."
            );
        }
        if (!type.isInstance(service)) {
            throw new IllegalArgumentException(
                service.getClass().getName() + " is not assignable to " + type.getName()
            );
        }
        services.put(type, service);
        logger.debug("Registered service: " + type.getSimpleName()
                + " -> " + service.getClass().getSimpleName());
    }

    // ── Retrieval ─────────────────────────────────────────────────────────────

    /**
     * Retrieves the service registered under {@code type}.
     *
     * @param type the key used during registration
     * @param <T>  service type
     * @return the registered instance, never {@code null}
     * @throws IllegalStateException if nothing is registered for {@code type}
     */
    @NotNull
    public <T> T get(@NotNull final Class<T> type) {
        final Object service = services.get(type);
        if (service == null) {
            throw new IllegalStateException(
                "No service registered for type '" + type.getName() + "'. "
                + "Check startup order in PluginBootstrap."
            );
        }
        return type.cast(service);
    }

    /**
     * Returns {@code true} if a service is registered for {@code type}.
     *
     * @param type the key to check
     * @return {@code true} if registered
     */
    public boolean isRegistered(@NotNull final Class<?> type) {
        return services.containsKey(type);
    }

    /**
     * Returns an unmodifiable view of all registered type keys.
     * Useful for diagnostic logging.
     *
     * @return set of registered type keys in registration order
     */
    @NotNull
    public Set<Class<?>> registeredTypes() {
        return Collections.unmodifiableSet(services.keySet());
    }

    // ── Shutdown ──────────────────────────────────────────────────────────────

    /**
     * Clears all registrations. Called during plugin shutdown after every
     * system has been individually torn down.
     */
    public void clear() {
        final int count = services.size();
        services.clear();
        logger.debug("Service registry cleared (" + count + " services released).");
    }
}
