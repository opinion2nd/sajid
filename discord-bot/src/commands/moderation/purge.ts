import { SlashCommandBuilder, PermissionFlagsBits, type ChatInputCommandInteraction, type TextBasedChannel } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed } from "../../util/embeds.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("purge")
    .setDescription("Bulk delete recent messages in this channel")
    .setDefaultMemberPermissions(PermissionFlagsBits.ManageMessages)
    .addIntegerOption((o) =>
      o.setName("amount").setDescription("Number of messages to delete (1-100)").setRequired(true).setMinValue(1).setMaxValue(100)
    )
    .addUserOption((o) => o.setName("user").setDescription("Only delete messages from this user")),

  async execute(interaction: ChatInputCommandInteraction) {
    const amount = interaction.options.getInteger("amount", true);
    const user = interaction.options.getUser("user");
    const channel = interaction.channel as TextBasedChannel & { bulkDelete: (n: number) => Promise<unknown> };

    if (!channel || !("bulkDelete" in channel)) {
      await interaction.reply({ embeds: [errorEmbed("This command can't be used in this channel.")], ephemeral: true });
      return;
    }

    await interaction.deferReply({ ephemeral: true });

    let deletedCount = amount;
    if (user) {
      const messages = await (channel as any).messages.fetch({ limit: 100 });
      const filtered = messages.filter((m: any) => m.author.id === user.id).first(amount);
      await (channel as any).bulkDelete(filtered, true);
      deletedCount = filtered.length;
    } else {
      await channel.bulkDelete(amount);
    }

    await interaction.editReply({ embeds: [successEmbed(`Deleted ${deletedCount} message(s).`)] });
  },
};

export default command;
