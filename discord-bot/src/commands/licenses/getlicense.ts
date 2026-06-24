import { SlashCommandBuilder, EmbedBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { infoEmbed, COLORS } from "../../util/embeds.js";
import { getLicensesByUser } from "../../modules/licenses.js";

const command: Command = {
  data: new SlashCommandBuilder().setName("getlicense").setDescription("View your own license keys"),

  async execute(interaction: ChatInputCommandInteraction) {
    const licenses = getLicensesByUser(interaction.guild!.id, interaction.user.id);
    if (licenses.length === 0) {
      await interaction.reply({ embeds: [infoEmbed("You don't have any license keys!")], ephemeral: true });
      return;
    }

    const embed = new EmbedBuilder()
      .setTitle("Your License Keys")
      .setColor(COLORS.info)
      .addFields(
        licenses.slice(0, 25).map((l) => ({
          name: l.product_name,
          value: `\`\`\`${l.license_key}\`\`\`Expires: ${
            l.expires_at ? `<t:${Math.floor(l.expires_at / 1000)}:R>` : "Never"
          }`,
        }))
      );
    await interaction.reply({ embeds: [embed], ephemeral: true });
  },
};

export default command;
