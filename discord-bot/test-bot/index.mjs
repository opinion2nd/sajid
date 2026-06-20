// Tester bot: run defensive-feature tests against your own server with a
// slash command instead of a CLI script. Use a SEPARATE bot account from
// your main bot -- the main bot is exempt from its own anti-nuke
// punishment, so it can't usefully test itself.
//
// Every create/delete/kick/ban burst below fires concurrently
// (Promise.all/allSettled) instead of looping with sleeps, so each test
// runs as fast as Discord's API allows.
//
// Setup: copy .env.example to .env and fill in TEST_BOT_TOKEN,
// TEST_CLIENT_ID, TEST_GUILD_ID. Enable the "Server Members Intent" in the
// Bot tab for this application (needed for automod-mentions/ghostping/
// nuke-kicks/nuke-bans). Invite it with Manage Channels, Manage Roles,
// Manage Webhooks, Manage Emojis and Stickers, Kick Members, Ban Members,
// and Send Messages, then:
//   npm install
//   npm run deploy-commands
//   npm start
// Then in Discord: /test nuke-channels, /test automod-caps, etc.
import "dotenv/config";
import { Client, GatewayIntentBits } from "discord.js";

const client = new Client({
  intents: [GatewayIntentBits.Guilds, GatewayIntentBits.GuildMessages, GatewayIntentBits.GuildMembers],
});

async function nukeChannels(guild) {
  const lines = ["Creating 4 channels concurrently..."];
  const channels = await Promise.all(Array.from({ length: 4 }, (_, i) => guild.channels.create({ name: `nuke-test-${i + 1}`, type: 0 })));
  lines.push("Deleting all of them concurrently...");
  const results = await Promise.allSettled(channels.map((ch) => ch.delete("nuke-test")));
  results.forEach((r, i) => lines.push(r.status === "fulfilled" ? `Deleted #${channels[i].name}` : `Failed to delete #${channels[i].name}: ${r.reason?.message}`));
  return lines;
}

async function nukeRoles(guild) {
  const lines = ["Creating 4 roles concurrently..."];
  const roles = await Promise.all(Array.from({ length: 4 }, (_, i) => guild.roles.create({ name: `nuke-test-role-${i + 1}` })));
  lines.push("Deleting all of them concurrently...");
  const results = await Promise.allSettled(roles.map((role) => role.delete("nuke-test")));
  results.forEach((r, i) => lines.push(r.status === "fulfilled" ? `Deleted role ${roles[i].name}` : `Failed to delete ${roles[i].name}: ${r.reason?.message}`));
  return lines;
}

async function nukeWebhooks(channel) {
  const lines = ["Creating 4 webhooks concurrently..."];
  const hooks = await Promise.all(Array.from({ length: 4 }, (_, i) => channel.createWebhook({ name: `nuke-test-hook-${i + 1}` })));
  lines.push("Deleting all of them concurrently...");
  const results = await Promise.allSettled(hooks.map((hook) => hook.delete("nuke-test")));
  results.forEach((r, i) => lines.push(r.status === "fulfilled" ? `Deleted webhook ${hooks[i].name}` : `Failed to delete webhook ${hooks[i].name}: ${r.reason?.message}`));
  return lines;
}

async function nukePermissions(guild) {
  const lines = ["Creating 4 channels concurrently..."];
  const channels = await Promise.all(Array.from({ length: 4 }, (_, i) => guild.channels.create({ name: `perm-test-${i + 1}`, type: 0 })));
  lines.push("Denying @everyone's View Channel permission on all of them concurrently...");
  await Promise.all(channels.map((ch) => ch.permissionOverwrites.edit(guild.roles.everyone, { ViewChannel: false }, { reason: "nuke-test" })));
  lines.push("Cleaning up the temporary channels concurrently...");
  const results = await Promise.allSettled(channels.map((ch) => ch.delete("nuke-test")));
  results.forEach((r, i) => {
    if (r.status === "rejected") lines.push(`Failed to delete #${channels[i].name}: ${r.reason?.message}`);
  });
  return lines;
}

async function nukeEmojis(guild) {
  const lines = ["Creating 4 temporary emojis (using the bot's own avatar) concurrently..."];
  const avatar = client.user.displayAvatarURL({ extension: "png", size: 128 });
  const emojis = await Promise.all(Array.from({ length: 4 }, (_, i) => guild.emojis.create({ attachment: avatar, name: `nuke_test_${i + 1}` })));
  lines.push("Deleting all of them concurrently...");
  const results = await Promise.allSettled(emojis.map((e) => e.delete("nuke-test")));
  results.forEach((r, i) => lines.push(r.status === "fulfilled" ? `Deleted emoji ${emojis[i].name}` : `Failed to delete emoji ${emojis[i].name}: ${r.reason?.message}`));
  return lines;
}

async function nukeKicks(guild, targets) {
  if (targets.length === 0) return ["No target members provided."];
  const lines = [`Kicking ${targets.length} member(s) concurrently...`];
  const results = await Promise.allSettled(targets.map((user) => guild.members.kick(user.id, "nuke-test")));
  results.forEach((r, i) => lines.push(r.status === "fulfilled" ? `Kicked ${targets[i].tag}` : `Failed to kick ${targets[i].tag}: ${r.reason?.message}`));
  lines.push("Kicks can't be auto-undone — send them a fresh invite link to rejoin.");
  return lines;
}

async function nukeBans(guild, targets) {
  if (targets.length === 0) return ["No target members provided."];
  const lines = [`Banning ${targets.length} member(s) concurrently...`];
  const banResults = await Promise.allSettled(targets.map((user) => guild.bans.create(user.id, { reason: "nuke-test" })));
  banResults.forEach((r, i) => lines.push(r.status === "fulfilled" ? `Banned ${targets[i].tag}` : `Failed to ban ${targets[i].tag}: ${r.reason?.message}`));
  lines.push("Unbanning everyone concurrently...");
  const unbanResults = await Promise.allSettled(targets.map((user) => guild.bans.remove(user.id, "nuke-test cleanup")));
  unbanResults.forEach((r, i) => {
    if (r.status === "rejected") lines.push(`Failed to unban ${targets[i].tag}: ${r.reason?.message}`);
  });
  lines.push("Unbanned everyone. They'll still need a fresh invite link to rejoin.");
  return lines;
}

async function automodInvite(channel) {
  await channel.send("hey check out my server discord.gg/fake-invite-test");
  return ["Sent a fake invite link. Should get deleted/punished if anti_invite is on."];
}

async function automodCaps(channel) {
  await channel.send("THIS IS A LOUD ALL CAPS MESSAGE TO TEST AUTOMOD CAPS DETECTION");
  return ["Sent an all-caps message. Should get flagged if anti_caps is on."];
}

async function automodSpam(channel) {
  await Promise.all(Array.from({ length: 4 }, () => channel.send("spam test message")));
  return ["Sent 4 copies of the same message concurrently. Should get flagged if anti_spam is on."];
}

async function automodMentions(guild, channel) {
  const members = await guild.members.fetch();
  const targets = members.filter((m) => !m.user.bot).first(6);
  if (targets.length < 2) {
    return ["Not enough non-bot members in this server to test mention spam."];
  }
  const mentionText = targets.map((m) => `<@${m.id}>`).join(" ");
  await channel.send(`${mentionText} mention spam test`);
  return [`Mentioned ${targets.length} members. Should get flagged if max_mentions is lower than that count.`];
}

async function ghostping(guild, channel) {
  const members = await guild.members.fetch();
  const target = members.filter((m) => !m.user.bot).first();
  if (!target) return ["No non-bot member found to ghost-ping."];
  const msg = await channel.send(`<@${target.id}>`);
  await msg.delete();
  return [`Pinged ${target.user.tag} then deleted the message. Should get flagged if anti-ghostping is on.`];
}

client.on("interactionCreate", async (interaction) => {
  if (!interaction.isChatInputCommand() || interaction.commandName !== "test") return;

  if (interaction.user.id !== process.env.TEST_OWNER_ID) {
    await interaction.reply({ content: "You're not allowed to use this command.", ephemeral: true });
    return;
  }

  const sub = interaction.options.getSubcommand();
  const guild = interaction.guild;
  const channel = interaction.options.getChannel("channel");
  const targets = ["target1", "target2", "target3", "target4"]
    .map((name) => interaction.options.getUser(name))
    .filter(Boolean);

  await interaction.deferReply({ ephemeral: true });

  try {
    let lines;
    switch (sub) {
      case "nuke-channels":
        lines = await nukeChannels(guild);
        break;
      case "nuke-roles":
        lines = await nukeRoles(guild);
        break;
      case "nuke-webhooks":
        lines = await nukeWebhooks(channel);
        break;
      case "nuke-permissions":
        lines = await nukePermissions(guild);
        break;
      case "nuke-emojis":
        lines = await nukeEmojis(guild);
        break;
      case "nuke-kicks":
        lines = await nukeKicks(guild, targets);
        break;
      case "nuke-bans":
        lines = await nukeBans(guild, targets);
        break;
      case "automod-invite":
        lines = await automodInvite(channel);
        break;
      case "automod-caps":
        lines = await automodCaps(channel);
        break;
      case "automod-spam":
        lines = await automodSpam(channel);
        break;
      case "automod-mentions":
        lines = await automodMentions(guild, channel);
        break;
      case "ghostping":
        lines = await ghostping(guild, channel);
        break;
      default:
        lines = ["Unknown test."];
    }
    lines.push("Check the main bot's modlog channel for results.");
    await interaction.editReply(lines.join("\n"));
  } catch (err) {
    await interaction.editReply(`Test failed: ${err.message}`);
  }
});

client.once("ready", () => console.log(`[test-bot] Logged in as ${client.user.tag}`));
client.login(process.env.TEST_BOT_TOKEN);
