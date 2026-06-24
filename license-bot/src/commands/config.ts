import { SlashCommandBuilder, PermissionFlagsBits, EmbedBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../types.js";
import { successEmbed, COLORS } from "../util/embeds.js";
import { getGuildSettings, setWebhookUrl } from "../modules/settings.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("config")
    .setDescription("Configure the license bot for this server")
    .setDefaultMemberPermissions(PermissionFlagsBits.Administrator)
    .addSubcommand((sc) =>
      sc
        .setName("webhook")
        .setDescription("Set the audit-log webhook for license/product/redeem events")
        .addStringOption((o) => o.setName("url").setDescription("Discord webhook URL (leave blank to disable)")),
    )
    .addSubcommand((sc) => sc.setName("view").setDescription("View current settings")),

  async execute(interaction: ChatInputCommandInteraction) {
    const sub = interaction.options.getSubcommand();
    const guildId = interaction.guild!.id;

    if (sub === "webhook") {
      const url = interaction.options.getString("url");
      if (url && !/^https:\/\/(canary\.|ptb\.)?discord(app)?\.com\/api\/webhooks\//.test(url)) {
        await interaction.reply({
          embeds: [successEmbed("That doesn't look like a Discord webhook URL — please double-check it.")],
          ephemeral: true,
        });
        return;
      }
      setWebhookUrl(guildId, url);
      await interaction.reply({
        embeds: [successEmbed(url ? "Webhook URL updated." : "Webhook disabled.")],
        ephemeral: true,
      });
      return;
    }

    // view
    const settings = getGuildSettings(guildId);
    const embed = new EmbedBuilder()
      .setTitle("License Bot Settings")
      .setColor(COLORS.info)
      .addFields({ name: "Webhook URL", value: settings.webhookUrl ?? "*not set*" });
    await interaction.reply({ embeds: [embed], ephemeral: true });
  },
};

export default command;
