import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { getBalance, addBalance, CURRENCY } from "../../modules/economy.js";
import { refreshBaltopPanel } from "../../modules/ecoleaderboardpanel.js";
import { errorEmbed, brandEmbed } from "../../util/embeds.js";

const ROB_SUCCESS_CHANCE = 0.4;
const FINE_FRACTION = 0.2; // lose 20% of your own balance if caught

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("rob")
    .setDescription("Attempt to rob another member — risky business")
    .addUserOption((o) => o.setName("user").setDescription("Who to rob").setRequired(true)),

  async execute(interaction: ChatInputCommandInteraction) {
    const target = interaction.options.getUser("user", true);
    const guildId = interaction.guild!.id;
    const robberId = interaction.user.id;

    if (target.id === robberId) {
      await interaction.reply({ embeds: [errorEmbed("You can't rob yourself.")], ephemeral: true });
      return;
    }
    if (target.bot) {
      await interaction.reply({ embeds: [errorEmbed("You can't rob a bot.")], ephemeral: true });
      return;
    }

    const victimBalance = getBalance(guildId, target.id);
    if (victimBalance < 100) {
      await interaction.reply({ embeds: [errorEmbed(`${target.username} doesn't have enough coins worth robbing.`)], ephemeral: true });
      return;
    }
    if (getBalance(guildId, robberId) < 100) {
      await interaction.reply({ embeds: [errorEmbed("You need at least 100 coins to risk a robbery.")], ephemeral: true });
      return;
    }

    if (Math.random() < ROB_SUCCESS_CHANCE) {
      const stolen = Math.floor(victimBalance * (0.1 + Math.random() * 0.3)); // 10–40%
      addBalance(guildId, target.id, -stolen);
      const balance = addBalance(guildId, robberId, stolen);
      await interaction.reply({
        embeds: [
          brandEmbed()
            .setColor(0x57f287)
            .setTitle("🦹 Heist successful!")
            .setDescription(`You robbed **${stolen.toLocaleString()}** ${CURRENCY} from ${target}.\nYour balance: **${balance.toLocaleString()}** ${CURRENCY}`),
        ],
      });
    } else {
      const fine = Math.floor(getBalance(guildId, robberId) * FINE_FRACTION);
      const balance = addBalance(guildId, robberId, -fine);
      await interaction.reply({
        embeds: [
          brandEmbed()
            .setColor(0xed4245)
            .setTitle("🚓 Caught!")
            .setDescription(`You got caught and were fined **${fine.toLocaleString()}** ${CURRENCY}.\nYour balance: **${balance.toLocaleString()}** ${CURRENCY}`),
        ],
      });
    }
    await refreshBaltopPanel(interaction.guild!);
  },
};

export default command;
