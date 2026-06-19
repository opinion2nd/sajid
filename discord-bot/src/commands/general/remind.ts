import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed } from "../../util/embeds.js";
import { parseDuration } from "../../util/format.js";
import { createReminder } from "../../modules/reminders.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("remind")
    .setDescription("Set a reminder")
    .addStringOption((o) => o.setName("duration").setDescription("e.g. 10m, 1h, 2d").setRequired(true))
    .addStringOption((o) => o.setName("message").setDescription("What to remind you about").setRequired(true)),

  async execute(interaction: ChatInputCommandInteraction) {
    const durationInput = interaction.options.getString("duration", true);
    const message = interaction.options.getString("message", true);

    const ms = parseDuration(durationInput);
    if (!ms || ms <= 0) {
      await interaction.reply({ embeds: [errorEmbed("Invalid duration. Use a format like `10m`, `1h`, or `2d`.")], ephemeral: true });
      return;
    }

    const remindAt = Date.now() + ms;
    createReminder(interaction.client, interaction.user.id, interaction.channelId, interaction.guild!.id, remindAt, message);
    await interaction.reply({ embeds: [successEmbed(`I'll remind you <t:${Math.floor(remindAt / 1000)}:R>.`)], ephemeral: true });
  },
};

export default command;
