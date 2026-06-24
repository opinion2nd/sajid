import { SlashCommandBuilder, PermissionFlagsBits, EmbedBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed, infoEmbed, COLORS } from "../../util/embeds.js";
import { createApiKey, listApiKeys, revokeApiKey, KNOWN_PERMISSIONS } from "../../modules/apiKeys.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("apikey")
    .setDescription("Manage REST API keys for external license verification")
    .setDefaultMemberPermissions(PermissionFlagsBits.Administrator)
    .addSubcommand((sc) =>
      sc
        .setName("create")
        .setDescription("Create a new API key")
        .addStringOption((o) => o.setName("name").setDescription("Descriptive name for this key").setRequired(true))
        .addStringOption((o) =>
          o
            .setName("permissions")
            .setDescription("Comma-separated, e.g. licenses:read,products:read,auth or *")
            .setRequired(true)
        )
        .addIntegerOption((o) => o.setName("rate_limit").setDescription("Requests per minute (default 60)").setMinValue(1))
    )
    .addSubcommand((sc) => sc.setName("list").setDescription("List API keys"))
    .addSubcommand((sc) =>
      sc
        .setName("revoke")
        .setDescription("Revoke an API key")
        .addStringOption((o) => o.setName("name").setDescription("Key name").setRequired(true))
    ),

  async execute(interaction: ChatInputCommandInteraction) {
    const sub = interaction.options.getSubcommand();
    const guildId = interaction.guild!.id;

    if (sub === "create") {
      const name = interaction.options.getString("name", true).trim();
      const permissions = interaction.options
        .getString("permissions", true)
        .split(",")
        .map((p) => p.trim())
        .filter(Boolean);

      const invalid = permissions.filter((p) => !KNOWN_PERMISSIONS.includes(p as (typeof KNOWN_PERMISSIONS)[number]));
      if (invalid.length > 0) {
        await interaction.reply({
          embeds: [errorEmbed(`Unknown permission(s): ${invalid.join(", ")}.\nValid: ${KNOWN_PERMISSIONS.join(", ")}`)],
          ephemeral: true,
        });
        return;
      }

      const rateLimit = interaction.options.getInteger("rate_limit") ?? 60;
      const { record, plaintextKey } = createApiKey(guildId, name, permissions, rateLimit, interaction.user.id);

      const embed = new EmbedBuilder()
        .setTitle("API Key Created")
        .setColor(COLORS.success)
        .setDescription(
          "Save this key now — it cannot be shown again. If lost, revoke it and create a new one.\n" +
            `\`\`\`${plaintextKey}\`\`\``
        )
        .addFields(
          { name: "Name", value: record.name, inline: true },
          { name: "Rate limit", value: `${record.rate_limit}/min`, inline: true },
          { name: "Permissions", value: permissions.join(", ") }
        );
      await interaction.reply({ embeds: [embed], ephemeral: true });
      return;
    }

    if (sub === "list") {
      const keys = listApiKeys(guildId);
      if (keys.length === 0) {
        await interaction.reply({ embeds: [infoEmbed("There are no API keys yet.")], ephemeral: true });
        return;
      }
      const embed = new EmbedBuilder()
        .setTitle("API Keys")
        .setColor(COLORS.info)
        .addFields(
          keys.map((k) => ({
            name: k.name,
            value: `Permissions: ${JSON.parse(k.permissions).join(", ")}\nRate limit: ${k.rate_limit}/min\nCreated by <@${k.created_by}>`,
          }))
        );
      await interaction.reply({ embeds: [embed], ephemeral: true });
      return;
    }

    // revoke
    const name = interaction.options.getString("name", true).trim();
    const revoked = revokeApiKey(guildId, name);
    if (!revoked) {
      await interaction.reply({ embeds: [errorEmbed("No API key with that name was found.")], ephemeral: true });
      return;
    }
    await interaction.reply({ embeds: [successEmbed(`Revoked API key **${name}**.`)], ephemeral: true });
  },
};

export default command;
