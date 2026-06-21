import {
  ActionRowBuilder,
  ButtonBuilder,
  ButtonStyle,
  EmbedBuilder,
  type ButtonInteraction,
} from "discord.js";
import { db, getGuildConfig } from "../db.js";
import { createTicketChannel, getTicketByChannel, closeTicket, refreshTicketPanel } from "../modules/tickets.js";
import { castVote, buildPollEmbed } from "../modules/polls.js";
import { castSuggestionVote, buildSuggestionEmbed } from "../modules/suggestions.js";
import { playTicTacToeMove, getTicTacToeGame, buildTicTacToeEmbed, buildTicTacToeRows } from "../modules/games/tictactoe.js";
import { playConnectFourMove, getConnectFourGame, buildConnectFourEmbed, buildConnectFourRow } from "../modules/games/connectfour.js";
import { move2048, getGame2048, buildGame2048Embed, buildGame2048Rows, type Direction } from "../modules/games/twentyfortyeight.js";
import { buildGiveawayEmbed, buildGiveawayButtonRow, type Giveaway } from "../modules/giveaways.js";
import { buildRolePanelEmbed, buildRolePanelRows, rolesFromMessageComponents } from "../modules/rolepanel.js";
import { errorEmbed, successEmbed, infoEmbed } from "../util/embeds.js";

export function ticketCloseRow() {
  return new ActionRowBuilder<ButtonBuilder>().addComponents(
    new ButtonBuilder().setCustomId("ticket_close").setLabel("Close Ticket").setStyle(ButtonStyle.Danger)
  );
}

export async function routeButton(interaction: ButtonInteraction) {
  const { customId } = interaction;
  if (customId === "ticket_open") return handleTicketOpen(interaction);
  if (customId === "ticket_close") return handleTicketClose(interaction);
  if (customId.startsWith("giveaway_enter_")) {
    return handleGiveawayEntry(interaction, Number(customId.slice("giveaway_enter_".length)));
  }
  if (customId.startsWith("rolepanel_")) {
    return handleRolePanel(interaction, customId.slice("rolepanel_".length));
  }
  if (customId === "verify_member") return handleVerify(interaction);
  if (customId.startsWith("poll_")) {
    const [, pollId, optionIndex] = customId.split("_");
    return handlePollVote(interaction, Number(pollId), Number(optionIndex));
  }
  if (customId.startsWith("suggestion_up_")) {
    return handleSuggestionVote(interaction, Number(customId.slice("suggestion_up_".length)), "up");
  }
  if (customId.startsWith("suggestion_down_")) {
    return handleSuggestionVote(interaction, Number(customId.slice("suggestion_down_".length)), "down");
  }
  if (customId.startsWith("ttt_")) {
    const [, gameId, cell] = customId.split("_");
    return handleTicTacToeMove(interaction, gameId, Number(cell));
  }
  if (customId.startsWith("c4_")) {
    const [, gameId, col] = customId.split("_");
    return handleConnectFourMove(interaction, gameId, Number(col));
  }
  if (customId.startsWith("g2048_")) {
    const [, gameId, direction] = customId.split("_");
    return handleGame2048Move(interaction, gameId, direction as Direction);
  }
}

async function handleTicketOpen(interaction: ButtonInteraction) {
  await interaction.deferReply({ ephemeral: true });
  const result = await createTicketChannel(interaction.guild!, interaction.user.id);
  if (!result.channel) {
    await interaction.editReply({
      embeds: [errorEmbed("Could not create a ticket channel. Ask staff to run `/ticket setup` first.")],
    });
    return;
  }
  if (result.existing) {
    await interaction.editReply({ embeds: [infoEmbed(`You already have an open ticket: ${result.channel}`)] });
    return;
  }
  await result.channel.send({
    content: `${interaction.user} Welcome! Staff will be with you shortly. Describe your issue below.`,
    components: [ticketCloseRow()],
  });
  await refreshTicketPanel(interaction.guild!);
  await interaction.editReply({ embeds: [successEmbed(`Ticket created: ${result.channel}`)] });
}

async function handleTicketClose(interaction: ButtonInteraction) {
  const ticket = getTicketByChannel(interaction.channelId);
  if (!ticket || ticket.status === "closed") {
    await interaction.reply({ embeds: [errorEmbed("This is not an open ticket channel.")], ephemeral: true });
    return;
  }

  await interaction.reply({ embeds: [infoEmbed("🔒 Closing this ticket in 5 seconds...")] });
  closeTicket(interaction.channelId);

  const guild = interaction.guild!;
  await refreshTicketPanel(guild);
  const config = getGuildConfig(guild.id);
  if (config.ticket_log_channel) {
    const logChannel = guild.channels.cache.get(config.ticket_log_channel);
    const channelName = interaction.channel && "name" in interaction.channel ? interaction.channel.name : interaction.channelId;
    if (logChannel?.isTextBased()) {
      await logChannel.send(`🎫 Ticket \`#${channelName}\` closed by ${interaction.user}.`).catch(() => {});
    }
  }

  setTimeout(() => {
    interaction.channel?.delete().catch(() => {});
  }, 5000);
}

async function handleGiveawayEntry(interaction: ButtonInteraction, giveawayId: number) {
  const giveaway = db.prepare("SELECT * FROM giveaways WHERE id = ?").get(giveawayId) as Giveaway | undefined;
  if (!giveaway || giveaway.ended) {
    await interaction.reply({ embeds: [errorEmbed("This giveaway has ended.")], ephemeral: true });
    return;
  }

  const entries: string[] = JSON.parse(giveaway.entries);
  const idx = entries.indexOf(interaction.user.id);
  let replyEmbed;
  if (idx === -1) {
    entries.push(interaction.user.id);
    replyEmbed = successEmbed("You're entered into the giveaway! Good luck 🍀");
  } else {
    entries.splice(idx, 1);
    replyEmbed = infoEmbed("You left the giveaway.");
  }
  db.prepare("UPDATE giveaways SET entries = ? WHERE id = ?").run(JSON.stringify(entries), giveawayId);

  // Live-update the public giveaway message so the entry count reflects reality.
  const refreshed = buildGiveawayEmbed(giveaway.prize, giveaway.winner_count, giveaway.end_at, giveaway.host_id, entries.length);
  await interaction.update({ embeds: [refreshed], components: [buildGiveawayButtonRow(giveawayId)] });
  await interaction.followUp({ embeds: [replyEmbed], ephemeral: true });
}

async function handleRolePanel(interaction: ButtonInteraction, roleId: string) {
  const member = interaction.member;
  const guild = interaction.guild;
  if (!guild || !member || !("roles" in member)) return;

  const role = guild.roles.cache.get(roleId);
  if (!role) {
    await interaction.reply({ embeds: [errorEmbed("That role no longer exists.")], ephemeral: true });
    return;
  }

  try {
    const guildMember = await guild.members.fetch(member.user.id);
    const hasRole = guildMember.roles.cache.has(roleId);
    let replyEmbed;
    if (hasRole) {
      await guildMember.roles.remove(role);
      replyEmbed = infoEmbed(`Removed role **${role.name}**.`);
    } else {
      await guildMember.roles.add(role);
      replyEmbed = successEmbed(`Added role **${role.name}**.`);
    }

    // Re-render the panel embed so the live member counts stay accurate.
    const sourceEmbed = interaction.message.embeds[0];
    const rawTitle = sourceEmbed?.title ?? "Role Panel";
    const title = rawTitle.replace(/^🎭\s*/, "");
    const description = (sourceEmbed?.description ?? "").split("\n\n")[0];
    const roles = rolesFromMessageComponents(interaction.message.components as never, guild);
    const refreshed = buildRolePanelEmbed(title, description, roles, guild);
    await interaction.update({ embeds: [refreshed], components: buildRolePanelRows(roles) });
    await interaction.followUp({ embeds: [replyEmbed], ephemeral: true });
  } catch {
    await interaction.reply({
      embeds: [errorEmbed("I don't have permission to manage that role (check role hierarchy).")],
      ephemeral: true,
    }).catch(() => {});
  }
}

async function handleVerify(interaction: ButtonInteraction) {
  const guild = interaction.guild;
  if (!guild) return;

  const config = getGuildConfig(guild.id);
  if (!config.verify_role) {
    await interaction.reply({ embeds: [errorEmbed("Verification is not configured on this server.")], ephemeral: true });
    return;
  }

  const role = guild.roles.cache.get(config.verify_role);
  if (!role) {
    await interaction.reply({ embeds: [errorEmbed("The verification role no longer exists.")], ephemeral: true });
    return;
  }

  try {
    const member = await guild.members.fetch(interaction.user.id);
    if (member.roles.cache.has(role.id)) {
      await interaction.reply({ embeds: [infoEmbed("You're already verified.")], ephemeral: true });
      return;
    }
    await member.roles.add(role);

    // Refresh the panel footer with the new verified-member count.
    const sourceEmbed = interaction.message.embeds[0];
    if (sourceEmbed) {
      const refreshed = EmbedBuilder.from(sourceEmbed).setFooter({
        text: `✔️ Verified members: ${role.members.size}`,
      });
      await interaction.update({ embeds: [refreshed] });
      await interaction.followUp({ embeds: [successEmbed("You're verified! Welcome to the server.")], ephemeral: true });
    } else {
      await interaction.reply({ embeds: [successEmbed("You're verified! Welcome to the server.")], ephemeral: true });
    }
  } catch {
    await interaction.reply({
      embeds: [errorEmbed("I couldn't assign the verification role (check my permissions and role hierarchy).")],
      ephemeral: true,
    });
  }
}

async function handlePollVote(interaction: ButtonInteraction, pollId: number, optionIndex: number) {
  const poll = castVote(pollId, interaction.user.id, optionIndex);
  if (!poll) {
    await interaction.reply({ embeds: [errorEmbed("This poll no longer exists.")], ephemeral: true });
    return;
  }
  await interaction.update({ embeds: [buildPollEmbed(poll)] });
}

async function handleSuggestionVote(interaction: ButtonInteraction, suggestionId: number, direction: "up" | "down") {
  const suggestion = castSuggestionVote(suggestionId, interaction.user.id, direction);
  if (!suggestion) {
    await interaction.reply({ embeds: [errorEmbed("This suggestion no longer exists.")], ephemeral: true });
    return;
  }
  const author = await interaction.client.users.fetch(suggestion.user_id).catch(() => null);
  await interaction.update({ embeds: [buildSuggestionEmbed(suggestion, author?.tag ?? suggestion.user_id)] });
}

async function handleTicTacToeMove(interaction: ButtonInteraction, gameId: string, cell: number) {
  const result = playTicTacToeMove(gameId, interaction.user.id, cell);
  if (result.error) {
    await interaction.reply({ embeds: [errorEmbed(result.error)], ephemeral: true });
    return;
  }
  const game = getTicTacToeGame(gameId)!;
  await interaction.update({ embeds: [buildTicTacToeEmbed(game)], components: buildTicTacToeRows(game) });
}

async function handleConnectFourMove(interaction: ButtonInteraction, gameId: string, col: number) {
  const result = playConnectFourMove(gameId, interaction.user.id, col);
  if (result.error) {
    await interaction.reply({ embeds: [errorEmbed(result.error)], ephemeral: true });
    return;
  }
  const game = getConnectFourGame(gameId)!;
  await interaction.update({ embeds: [buildConnectFourEmbed(game)], components: [buildConnectFourRow(game)] });
}

async function handleGame2048Move(interaction: ButtonInteraction, gameId: string, direction: Direction) {
  const result = move2048(gameId, interaction.user.id, direction);
  if (result.error) {
    await interaction.reply({ embeds: [errorEmbed(result.error)], ephemeral: true });
    return;
  }
  const game = getGame2048(gameId)!;
  await interaction.update({ embeds: [buildGame2048Embed(game)], components: buildGame2048Rows(game) });
}
