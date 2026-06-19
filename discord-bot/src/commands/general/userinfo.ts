import { SlashCommandBuilder, EmbedBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("userinfo")
    .setDescription("Show information about a member")
    .addUserOption((o) => o.setName("user").setDescription("The user to look up")),

  async execute(interaction: ChatInputCommandInteraction) {
    const target = interaction.options.getUser("user") ?? interaction.user;
    const member = await interaction.guild!.members.fetch(target.id).catch(() => null);

    const embed = new EmbedBuilder()
      .setTitle(target.tag)
      .setThumbnail(target.displayAvatarURL())
      .setColor(0x5865f2)
      .addFields(
        { name: "User ID", value: target.id, inline: true },
        { name: "Account Created", value: `<t:${Math.floor(target.createdTimestamp / 1000)}:D>`, inline: true }
      );

    if (member) {
      embed.addFields(
        { name: "Joined Server", value: member.joinedTimestamp ? `<t:${Math.floor(member.joinedTimestamp / 1000)}:D>` : "Unknown", inline: true },
        {
          name: "Roles",
          value: member.roles.cache.filter((r) => r.id !== interaction.guild!.id).map((r) => r.toString()).join(" ") || "None",
        }
      );
    }

    await interaction.reply({ embeds: [embed] });
  },
};

export default command;
