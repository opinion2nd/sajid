package com.ultimatedungeon.room.registry;

import com.ultimatedungeon.api.room.IRoomRegistry;
import com.ultimatedungeon.api.room.IRoomTemplate;
import com.ultimatedungeon.core.PluginLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

/** Holds all room templates with weighted random selection. */
public final class RoomRegistry implements IRoomRegistry {

    private final PluginLogger logger;
    private final Map<String, IRoomTemplate> templates = new LinkedHashMap<>();

    public RoomRegistry(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    @Override
    public void register(@NotNull final IRoomTemplate template) {
        templates.put(template.getTemplateId(), template);
        logger.debug("Registered room template: " + template.getTemplateId());
    }

    @Override
    @Nullable
    public IRoomTemplate getTemplate(@NotNull final String templateId) {
        return templates.get(templateId);
    }

    @Override
    @NotNull
    public IRoomTemplate selectWeighted() {
        throw new UnsupportedOperationException("RoomRegistry.selectWeighted() — Milestone 2.");
    }

    @Override
    @NotNull
    public Collection<IRoomTemplate> getAllTemplates() {
        return Collections.unmodifiableCollection(templates.values());
    }
}
