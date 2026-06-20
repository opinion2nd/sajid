import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { brandEmbed } from "../../util/embeds.js";

const CATEGORY_LABELS: Record<string, string> = {
  general: "🔧 General",
  moderation: "🛡️ Moderation",
  security: "🔒 Security",
  automod: "🚫 Automod",
  tickets: "🎫 Tickets",
  leveling: "📈 Leveling",
  economy: "💰 Economy",
  giveaway: "🎉 Giveaways",
  welcome: "👋 Welcome / Leave",
  utility: "🧰 Utility",
  fun: "🎲 Fun",
  games: "🎮 Games",
};

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("help")
    .setDescription("List every command Brother Craft offers, grouped by category")
    .addStringOption((opt) =>
      opt.setName("category").setDescription("Show only this category").setRequired(false)
    ),

  async execute(interaction: ChatInputCommandInteraction) {
    const requested = interaction.options.getString("category")?.toLowerCase();
    const categories = interaction.client.commandCategories;

    const embed = brandEmbed()
      .setTitle("📖 Brother Craft — Command List")
      .setDescription("Use `/help category:<name>` to filter. Categories: " + [...categories.keys()].join(", "));

    const entries = requested ? categories.filter((_, key) => key === requested) : categories;

    if (entries.size === 0) {
      await interaction.reply({
        embeds: [embed.setDescription(`No category named \`${requested}\` found. Categories: ${[...categories.keys()].join(", ")}`)],
        ephemeral: true,
      });
      return;
    }

    for (const [category, names] of entries) {
      const label = CATEGORY_LABELS[category] ?? `📁 ${category}`;
      const lines = names
        .map((name) => {
          const cmd = interaction.client.commands.get(name);
          return cmd ? `\`/${cmd.data.name}\` — ${cmd.data.description}` : `\`/${name}\``;
        })
        .join("\n");
      embed.addFields({ name: label, value: lines });
    }

    await interaction.reply({ embeds: [embed] });
  },
};

export default command;
