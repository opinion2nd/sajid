import { SlashCommandBuilder, EmbedBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../types.js";
import { COLORS } from "../util/embeds.js";

const command: Command = {
  data: new SlashCommandBuilder().setName("help").setDescription("List all license bot commands"),

  async execute(interaction: ChatInputCommandInteraction) {
    const embed = new EmbedBuilder()
      .setTitle("Flaming License Bot — Commands")
      .setColor(COLORS.info)
      .addFields(
        {
          name: "Staff (Manage Server)",
          value:
            "`/product create|delete|list` — manage licensable products\n" +
            "`/license create` — issue a license directly to a Discord user\n" +
            "`/license generate` — bulk-generate unclaimed stock keys (for marketplace sales)\n" +
            "`/license delete|list|get|cleardata` — manage existing licenses",
        },
        {
          name: "Staff (Administrator)",
          value:
            "`/apikey create|list|revoke` — manage REST API keys for external verification\n" +
            "`/config webhook|view` — set the audit-log webhook for this server",
        },
        {
          name: "Everyone",
          value: "`/redeem <key>` — claim an unclaimed license key bought elsewhere\n" + "`/getlicense` — view your own license keys",
        },
      );
    await interaction.reply({ embeds: [embed], ephemeral: true });
  },
};

export default command;
