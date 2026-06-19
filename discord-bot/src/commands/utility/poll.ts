import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { errorEmbed } from "../../util/embeds.js";
import { createPoll, buildPollEmbed, buildPollButtonRow, getPollById } from "../../modules/polls.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("poll")
    .setDescription("Create a poll with up to 5 options")
    .addStringOption((o) => o.setName("question").setDescription("The poll question").setRequired(true))
    .addStringOption((o) => o.setName("option1").setDescription("Option 1").setRequired(true))
    .addStringOption((o) => o.setName("option2").setDescription("Option 2").setRequired(true))
    .addStringOption((o) => o.setName("option3").setDescription("Option 3"))
    .addStringOption((o) => o.setName("option4").setDescription("Option 4"))
    .addStringOption((o) => o.setName("option5").setDescription("Option 5")),

  async execute(interaction: ChatInputCommandInteraction) {
    const question = interaction.options.getString("question", true);
    const options = [1, 2, 3, 4, 5]
      .map((n) => interaction.options.getString(`option${n}`))
      .filter((o): o is string => Boolean(o));

    if (!interaction.channel?.isSendable()) {
      await interaction.reply({ embeds: [errorEmbed("I can't post a poll in this channel.")], ephemeral: true });
      return;
    }

    await interaction.deferReply();
    const placeholder = await interaction.editReply({ content: "Creating poll..." });

    const pollId = createPoll(interaction.guild!.id, interaction.channelId, placeholder.id, question, options);
    const poll = getPollById(pollId)!;

    await interaction.editReply({
      content: null,
      embeds: [buildPollEmbed(poll)],
      components: [buildPollButtonRow(pollId, options)],
    });
  },
};

export default command;
