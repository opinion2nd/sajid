package com.ultimatedungeon.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Utility for converting MiniMessage strings to Adventure Components and
 * sending them to players or command senders.
 *
 * <p>All user-facing text passes through this class. Placeholder substitution
 * uses Adventure's {@link TagResolver} API — never string concatenation, so
 * malicious player names cannot break message formatting.</p>
 *
 * <h3>Placeholder format in YAML</h3>
 * <pre>{@code
 * message: "<gold>Hello <player>!"
 * }</pre>
 * Tags like {@code <player>} are supplied as {@link Placeholder}s at send time.
 */
public final class MiniMessageUtil {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private MiniMessageUtil() {}

    // ── Parsing ───────────────────────────────────────────────────────────────

    /**
     * Parses a raw MiniMessage string into an Adventure {@link Component}.
     *
     * @param raw the MiniMessage-formatted string
     * @return the parsed component
     */
    @NotNull
    public static Component parse(@NotNull final String raw) {
        return MM.deserialize(raw);
    }

    /**
     * Parses a MiniMessage string with named string placeholder substitutions.
     *
     * <p>Keys in {@code placeholders} correspond to tag names in the template
     * (e.g. key {@code "player"} replaces {@code <player>} in the string).
     * Values are inserted as literal unparsed text to prevent injection.</p>
     *
     * @param raw          the MiniMessage-formatted template
     * @param placeholders map of tag name → replacement value
     * @return the parsed component with placeholders resolved
     */
    @NotNull
    public static Component parse(
            @NotNull final String              raw,
            @NotNull final Map<String, String> placeholders
    ) {
        final TagResolver.Builder resolverBuilder = TagResolver.builder();
        for (final Map.Entry<String, String> entry : placeholders.entrySet()) {
            resolverBuilder.resolver(
                Placeholder.unparsed(entry.getKey(), entry.getValue())
            );
        }
        return MM.deserialize(raw, resolverBuilder.build());
    }

    /**
     * Convenience overload for a single placeholder substitution.
     *
     * @param raw   the MiniMessage template
     * @param key   placeholder tag name
     * @param value replacement text (literal, not parsed)
     * @return the parsed component
     */
    @NotNull
    public static Component parse(
            @NotNull final String raw,
            @NotNull final String key,
            @NotNull final String value
    ) {
        return MM.deserialize(raw, Placeholder.unparsed(key, value));
    }

    // ── Send helpers ──────────────────────────────────────────────────────────

    /**
     * Sends a parsed MiniMessage string to a {@link CommandSender}.
     *
     * @param sender the recipient
     * @param raw    the MiniMessage-formatted string
     */
    public static void send(
            @NotNull final CommandSender sender,
            @NotNull final String        raw
    ) {
        sender.sendMessage(parse(raw));
    }

    /**
     * Sends a MiniMessage string with placeholder substitutions to a sender.
     *
     * @param sender       the recipient
     * @param raw          the template
     * @param placeholders substitution map
     */
    public static void send(
            @NotNull final CommandSender       sender,
            @NotNull final String              raw,
            @NotNull final Map<String, String> placeholders
    ) {
        sender.sendMessage(parse(raw, placeholders));
    }

    /**
     * Sends an action bar message to a player.
     *
     * @param player the recipient
     * @param raw    the MiniMessage-formatted action bar text
     */
    public static void sendActionBar(
            @NotNull final Player player,
            @NotNull final String raw
    ) {
        player.sendActionBar(parse(raw));
    }

    /**
     * Sends an action bar message with placeholder substitution.
     *
     * @param player       the recipient
     * @param raw          the MiniMessage template
     * @param placeholders substitution map
     */
    public static void sendActionBar(
            @NotNull final Player              player,
            @NotNull final String              raw,
            @NotNull final Map<String, String> placeholders
    ) {
        player.sendActionBar(parse(raw, placeholders));
    }

    /**
     * Sends a title + subtitle to a player.
     *
     * @param player      the recipient
     * @param titleRaw    MiniMessage title text
     * @param subtitleRaw MiniMessage subtitle text
     * @param fadeIn      fade-in ticks
     * @param stay        stay ticks
     * @param fadeOut     fade-out ticks
     */
    public static void sendTitle(
            @NotNull final Player player,
            @NotNull final String titleRaw,
            @NotNull final String subtitleRaw,
            final int             fadeIn,
            final int             stay,
            final int             fadeOut
    ) {
        player.showTitle(net.kyori.adventure.title.Title.title(
            parse(titleRaw),
            parse(subtitleRaw),
            net.kyori.adventure.title.Title.Times.times(
                net.kyori.adventure.util.Ticks.duration(fadeIn),
                net.kyori.adventure.util.Ticks.duration(stay),
                net.kyori.adventure.util.Ticks.duration(fadeOut)
            )
        ));
    }

    /** Strips all MiniMessage tags and returns plain text. */
    @NotNull
    public static String stripTags(@NotNull final String raw) {
        return MM.stripTags(raw);
    }
}
