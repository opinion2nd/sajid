package com.ultimatedungeon.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Converts MiniMessage strings into legacy section-code text and delivers it
 * through universal Bukkit APIs.
 *
 * <p>The plugin bundles its own (relocated) copy of Adventure/MiniMessage, so
 * MiniMessage formatting works identically on Paper <em>and</em> plain Spigot/
 * Bukkit. Output is serialized to legacy {@code §} strings and sent via methods
 * that exist on every platform ({@code sendMessage(String)},
 * {@code sendTitle(...)}, and the Spigot chat API for action bars).</p>
 */
public final class MiniMessageUtil {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private MiniMessageUtil() {}

    // ── Parsing ───────────────────────────────────────────────────────────────

    /** Parses a MiniMessage string into a component (internal building block). */
    @NotNull
    public static Component parse(@NotNull final String raw) {
        return MM.deserialize(raw);
    }

    @NotNull
    public static Component parse(@NotNull final String raw, @NotNull final Map<String, String> placeholders) {
        final TagResolver.Builder resolverBuilder = TagResolver.builder();
        for (final Map.Entry<String, String> entry : placeholders.entrySet()) {
            resolverBuilder.resolver(Placeholder.unparsed(entry.getKey(), entry.getValue()));
        }
        return MM.deserialize(raw, resolverBuilder.build());
    }

    @NotNull
    public static Component parse(@NotNull final String raw, @NotNull final String key, @NotNull final String value) {
        return MM.deserialize(raw, Placeholder.unparsed(key, value));
    }

    // ── Legacy serialization (cross-platform text) ─────────────────────────────

    /** Renders a MiniMessage string to a legacy {@code §}-code string. */
    @NotNull
    public static String legacy(@NotNull final String raw) {
        return LEGACY.serialize(parse(raw));
    }

    /** Renders a MiniMessage string with placeholders to a legacy string. */
    @NotNull
    public static String legacy(@NotNull final String raw, @NotNull final Map<String, String> placeholders) {
        return LEGACY.serialize(parse(raw, placeholders));
    }

    // ── Send helpers (universal Bukkit APIs) ───────────────────────────────────

    public static void send(@NotNull final CommandSender sender, @NotNull final String raw) {
        sender.sendMessage(legacy(raw));
    }

    public static void send(@NotNull final CommandSender sender, @NotNull final String raw,
                            @NotNull final Map<String, String> placeholders) {
        sender.sendMessage(legacy(raw, placeholders));
    }

    public static void sendActionBar(@NotNull final Player player, @NotNull final String raw) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(legacy(raw)));
    }

    public static void sendActionBar(@NotNull final Player player, @NotNull final String raw,
                                     @NotNull final Map<String, String> placeholders) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(legacy(raw, placeholders)));
    }

    public static void sendTitle(@NotNull final Player player, @NotNull final String titleRaw,
                                 @NotNull final String subtitleRaw, final int fadeIn, final int stay, final int fadeOut) {
        player.sendTitle(legacy(titleRaw), legacy(subtitleRaw), fadeIn, stay, fadeOut);
    }

    /** Strips all MiniMessage tags and returns plain text. */
    @NotNull
    public static String stripTags(@NotNull final String raw) {
        return MM.stripTags(raw);
    }
}
