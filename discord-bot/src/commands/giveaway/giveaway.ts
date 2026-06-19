import { SlashCommandBuilder, PermissionFlagsBits, ChannelType, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed, infoEmbed } from "../../util/embeds.js";
import { parseDuration, formatDuration } from "../../util/format.js";
import { db } from "../../db.js";
import { buildGiveawayEmbed, buildGiveawayButtonRow, endGiveaway, scheduleGiveawayEnd, type Giveaway } from "../../modules/giveaways.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("giveaway")
    .setDescription("Start, end, reroll, or list giveaways")
    .setDefaultMemberPermissions(PermissionFlagsBits.ManageGuild)
    .addSubcommand((sc) =>
      sc
        .setName("start")
        .setDescription("Start a new giveaway")
        .addStringOption((o) => o.setName("duration").setDescription("e.g. 10m, 1h, 3d").setRequired(true))
        .addStringOption((o) => o.setName("prize").setDescription("What is being given away").setRequired(true))
        .addIntegerOption((o) => o.setName("winners").setDescription("Number of winners").setMinValue(1).setMaxValue(20))
        .addChannelOption((o) =>
          o.setName("channel").setDescription("Channel to post the giveaway in").addChannelTypes(ChannelType.GuildText)
        )
    )
    .addSubcommand((sc) =>
      sc
        .setName("end")
        .setDescription("End a giveaway early")
        .addStringOption((o) => o.setName("message_id").setDescription("Giveaway message ID").setRequired(true))
    )
    .addSubcommand((sc) =>
      sc
        .setName("reroll")
        .setDescription("Pick a new winner for a finished giveaway")
        .addStringOption((o) => o.setName("message_id").setDescription("Giveaway message ID").setRequired(true))
    )
    .addSubcommand((sc) => sc.setName("list").setDescription("List active giveaways")),

  async execute(interaction: ChatInputCommandInteraction) {
    const sub = interaction.options.getSubcommand();
    const guild = interaction.guild!;

    if (sub === "start") {
      const durationStr = interaction.options.getString("duration", true);
      const prize = interaction.options.getString("prize", true);
      const winnerCount = interaction.options.getInteger("winners") ?? 1;
      const channelOption = interaction.options.getChannel("channel");

      const ms = parseDuration(durationStr);
      if (!ms) {
        await interaction.reply({ embeds: [errorEmbed("Invalid duration. Use formats like `10m`, `1h`, `3d`.")], ephemeral: true });
        return;
      }

      const channel = channelOption ? guild.channels.cache.get(channelOption.id) : interaction.channel;
      if (!channel?.isSendable()) {
        await interaction.reply({ embeds: [errorEmbed("That channel can't receive messages.")], ephemeral: true });
        return;
      }

      const endAt = Date.now() + ms;
      const embed = buildGiveawayEmbed(prize, winnerCount, endAt, interaction.user.id, 0);
      const message = await channel.send({ embeds: [embed], components: [] });

      const result = db
        .prepare(
          "INSERT INTO giveaways (guild_id, channel_id, message_id, host_id, prize, winner_count, end_at) VALUES (?, ?, ?, ?, ?, ?, ?)"
        )
        .run(guild.id, channel.id, message.id, interaction.user.id, prize, winnerCount, endAt);

      const giveawayId = Number(result.lastInsertRowid);
      await message.edit({ embeds: [embed], components: [buildGiveawayButtonRow(giveawayId)] });
      scheduleGiveawayEnd(interaction.client, giveawayId, endAt);

      await interaction.reply({
        embeds: [successEmbed(`Giveaway started in ${channel}! Ends in ${formatDuration(ms)}.`)],
        ephemeral: true,
      });
      return;
    }

    if (sub === "end" || sub === "reroll") {
      const messageId = interaction.options.getString("message_id", true);
      const giveaway = db.prepare("SELECT * FROM giveaways WHERE message_id = ? AND guild_id = ?").get(messageId, guild.id) as
        | Giveaway
        | undefined;
      if (!giveaway) {
        await interaction.reply({ embeds: [errorEmbed("No giveaway found with that message ID.")], ephemeral: true });
        return;
      }
      if (sub === "end" && giveaway.ended) {
        await interaction.reply({ embeds: [errorEmbed("That giveaway has already ended.")], ephemeral: true });
        return;
      }

      await endGiveaway(interaction.client, giveaway.id, sub === "reroll");
      await interaction.reply({
        embeds: [successEmbed(sub === "reroll" ? "Rerolled the giveaway winner." : "Giveaway ended.")],
        ephemeral: true,
      });
      return;
    }

    // list
    const active = db
      .prepare("SELECT * FROM giveaways WHERE guild_id = ? AND ended = 0")
      .all(guild.id) as Giveaway[];
    if (active.length === 0) {
      await interaction.reply({ embeds: [infoEmbed("There are no active giveaways.")], ephemeral: true });
      return;
    }
    const lines = active.map((g) => `• **${g.prize}** in <#${g.channel_id}> — ends <t:${Math.floor(g.end_at / 1000)}:R> (\`${g.message_id}\`)`);
    await interaction.reply({ embeds: [infoEmbed(lines.join("\n"))], ephemeral: true });
  },
};

export default command;
