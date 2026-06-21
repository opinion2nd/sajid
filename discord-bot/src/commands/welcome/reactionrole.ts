import {
  SlashCommandBuilder,
  PermissionFlagsBits,
  ChannelType,
  type ChatInputCommandInteraction,
  type TextChannel,
} from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed } from "../../util/embeds.js";
import { addReactionRole, removeReactionRole } from "../../modules/reactionroles.js";

/** Parses a custom-emoji mention into a stable key + the form usable in .react(). */
function parseEmoji(input: string): { key: string; reactable: string } {
  const custom = input.match(/^<a?:\w+:(\d+)>$/);
  if (custom) return { key: custom[1], reactable: input };
  return { key: input.trim(), reactable: input.trim() };
}

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("reactionrole")
    .setDescription("Give members a role when they react to a message")
    .setDefaultMemberPermissions(PermissionFlagsBits.ManageRoles)
    .addSubcommand((sc) =>
      sc
        .setName("add")
        .setDescription("Link an emoji reaction on a message to a role")
        .addChannelOption((o) =>
          o.setName("channel").setDescription("Channel the message is in").setRequired(true).addChannelTypes(ChannelType.GuildText)
        )
        .addStringOption((o) => o.setName("message_id").setDescription("Target message ID").setRequired(true))
        .addStringOption((o) => o.setName("emoji").setDescription("Emoji to react with").setRequired(true))
        .addRoleOption((o) => o.setName("role").setDescription("Role to grant").setRequired(true))
    )
    .addSubcommand((sc) =>
      sc
        .setName("remove")
        .setDescription("Unlink an emoji reaction from its role")
        .addStringOption((o) => o.setName("message_id").setDescription("Target message ID").setRequired(true))
        .addStringOption((o) => o.setName("emoji").setDescription("Emoji that was linked").setRequired(true))
    ),

  async execute(interaction: ChatInputCommandInteraction) {
    const sub = interaction.options.getSubcommand();
    const guild = interaction.guild!;

    if (sub === "add") {
      const channel = interaction.options.getChannel("channel", true);
      const messageId = interaction.options.getString("message_id", true);
      const emojiInput = interaction.options.getString("emoji", true);
      const role = interaction.options.getRole("role", true);

      const target = guild.channels.cache.get(channel.id) as TextChannel | undefined;
      if (!target?.isTextBased()) {
        await interaction.reply({ embeds: [errorEmbed("That channel can't be read.")], ephemeral: true });
        return;
      }

      const message = await target.messages.fetch(messageId).catch(() => null);
      if (!message) {
        await interaction.reply({ embeds: [errorEmbed("No message found with that ID in that channel.")], ephemeral: true });
        return;
      }

      const { key, reactable } = parseEmoji(emojiInput);
      try {
        await message.react(reactable);
      } catch {
        await interaction.reply({ embeds: [errorEmbed("I couldn't react with that emoji. Use a default emoji or one from this server.")], ephemeral: true });
        return;
      }

      addReactionRole(guild.id, target.id, messageId, key, role.id);
      await interaction.reply({
        embeds: [successEmbed(`Reacting with ${emojiInput} on that message now grants **${role.name}**.`)],
        ephemeral: true,
      });
      return;
    }

    // remove
    const messageId = interaction.options.getString("message_id", true);
    const emojiInput = interaction.options.getString("emoji", true);
    const { key } = parseEmoji(emojiInput);
    const result = removeReactionRole(guild.id, messageId, key);
    if (result.changes === 0) {
      await interaction.reply({ embeds: [errorEmbed("No reaction-role link found for that message and emoji.")], ephemeral: true });
      return;
    }
    await interaction.reply({ embeds: [successEmbed("Reaction-role link removed.")], ephemeral: true });
  },
};

export default command;
