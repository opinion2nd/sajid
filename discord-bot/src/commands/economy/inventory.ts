import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { getInventory, CURRENCY } from "../../modules/economy.js";
import { brandEmbed, infoEmbed } from "../../util/embeds.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("inventory")
    .setDescription("Show the items you've bought from the shop")
    .addUserOption((o) => o.setName("user").setDescription("Whose inventory to view")),

  async execute(interaction: ChatInputCommandInteraction) {
    const target = interaction.options.getUser("user") ?? interaction.user;
    const items = getInventory(interaction.guild!.id, target.id);

    if (items.length === 0) {
      await interaction.reply({ embeds: [infoEmbed(`**${target.username}** hasn't bought anything yet.`)], ephemeral: true });
      return;
    }

    const lines = items.map((i) => `• **${i.name}** ×${i.qty} _(worth ${i.price.toLocaleString()} ${CURRENCY} each)_`);
    await interaction.reply({
      embeds: [brandEmbed().setTitle(`🎒 ${target.username}'s Inventory`).setDescription(lines.join("\n"))],
    });
  },
};

export default command;
