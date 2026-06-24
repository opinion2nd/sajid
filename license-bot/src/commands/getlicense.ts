import { SlashCommandBuilder, EmbedBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../types.js";
import { infoEmbed, COLORS } from "../util/embeds.js";
import { getLicensesByUser } from "../modules/licenses.js";

const command: Command = {
  data: new SlashCommandBuilder().setName("getlicense").setDescription("View your own license keys"),

  async execute(interaction: ChatInputCommandInteraction) {
    const licenses = getLicensesByUser(interaction.guild!.id, interaction.user.id);
    if (licenses.length === 0) {
      await interaction.reply({ embeds: [infoEmbed("You don't have any license keys! Got a key to redeem? Use `/redeem`.")], ephemeral: true });
      return;
    }

    const embed = new EmbedBuilder()
      .setTitle("Your License Keys")
      .setColor(COLORS.info)
      .addFields(
        licenses.slice(0, 25).map((l) => ({
          name: l.productName,
          value: `\`\`\`${l.licenseKey}\`\`\`Expires: ${l.expiresAt ? `<t:${Math.floor(l.expiresAt / 1000)}:R>` : "Never"}`,
        })),
      );
    await interaction.reply({ embeds: [embed], ephemeral: true });
  },
};

export default command;
