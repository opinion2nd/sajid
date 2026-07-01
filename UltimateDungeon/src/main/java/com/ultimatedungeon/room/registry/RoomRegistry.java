package com.ultimatedungeon.room.registry;

import com.ultimatedungeon.api.room.IRoomRegistry;
import com.ultimatedungeon.api.room.IRoomTemplate;
import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.room.model.RoomType;
import com.ultimatedungeon.util.WeightedRandomSelector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Registry of all room templates with weighted random selection.
 *
 * <p>Templates are registered once at startup. The weighted selector is rebuilt
 * whenever a template is registered to keep selection O(log n).</p>
 */
public final class RoomRegistry implements IRoomRegistry {

    private final PluginLogger logger;
    private final Map<String, IRoomTemplate>      byId   = new LinkedHashMap<>();
    private final Map<RoomType, List<IRoomTemplate>> byType = new EnumMap<>(RoomType.class);
    private final WeightedRandomSelector<IRoomTemplate> selector = new WeightedRandomSelector<>();

    public RoomRegistry(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    @Override
    public void register(@NotNull final IRoomTemplate template) {
        byId.put(template.getTemplateId(), template);
        byType.computeIfAbsent(
                ((com.ultimatedungeon.room.templates.AbstractRoomTemplate) template).getRoomType(),
                k -> new ArrayList<>()
        ).add(template);
        selector.add(template, template.getWeight());
        logger.debug("Registered room template: " + template.getTemplateId()
                + " (weight=" + template.getWeight() + ")");
    }

    @Override
    @Nullable
    public IRoomTemplate getTemplate(@NotNull final String templateId) {
        return byId.get(templateId);
    }

    @Override
    @NotNull
    public IRoomTemplate selectWeighted() {
        return selector.select();
    }

    /**
     * Returns a random template for a specific room type, or {@code null} if
     * no templates of that type are registered.
     */
    @Nullable
    public IRoomTemplate selectForType(@NotNull final RoomType type) {
        final List<IRoomTemplate> pool = byType.get(type);
        if (pool == null || pool.isEmpty()) return null;
        if (pool.size() == 1) return pool.get(0);
        return pool.get(new java.util.Random().nextInt(pool.size()));
    }

    @Override
    @NotNull
    public Collection<IRoomTemplate> getAllTemplates() {
        return Collections.unmodifiableCollection(byId.values());
    }

    public int getTemplateCount() { return byId.size(); }
}
