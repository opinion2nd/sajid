import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { infoEmbed } from "../../util/embeds.js";

function hashScore(a: string, b: string): number {
  const combined = [a, b].sort().join("+");
  let hash = 0;
  for (let i = 0; i < combined.length; i++) {
    hash = (hash * 31 + combined.charCodeAt(i)) % 100000;
  }
  return hash % 101;
}

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("ship")
    .setDescription("Calculate the compatibility between two people")
    .addUserOption((o) => o.setName("user1").setDescription("First person").setRequired(true))
    .addUserOption((o) => o.setName("user2").setDescription("Second person").setRequired(true)),

  async execute(interaction: ChatInputCommandInteraction) {
    const user1 = interaction.options.getUser("user1", true);
    const user2 = interaction.options.getUser("user2", true);
    const score = hashScore(user1.id, user2.id);
    const barLength = Math.round(score / 10);
    const bar = "💖".repeat(barLength) + "🖤".repeat(10 - barLength);
    await interaction.reply({ embeds: [infoEmbed(`💘 **${user1.username}** + **${user2.username}** = **${score}%**\n${bar}`)] });
  },
};

export default command;
