// Standalone test tool for trying out the main bot's defensive features
// (anti-nuke, automod, ghost-ping detection) against a server you own.
// Run this with a SEPARATE "tester" bot account's token — never the main
// bot's token, since the main bot is exempt from its own anti-nuke
// punishment and can't usefully test itself.
//
// Setup:
//   1. Create a second Discord application + bot ("Nuke Tester" or similar).
//   2. Invite it to your server with: Manage Channels, Manage Roles,
//      Send Messages (Manage Roles only needed for the nuke-roles test).
//   3. Run one of the tests below from your own computer (not the panel):
//
//   TEST_BOT_TOKEN=xxx TEST_GUILD_ID=xxx node scripts/test-bot.mjs <test>
//
// Available <test> values:
//   nuke-channels   create + rapidly delete a few channels (tests /security nuke)
//   nuke-roles      create + rapidly delete a few roles (tests /security nuke)
//   automod-invite  sends a fake discord.gg invite link (tests anti_invite)
//   automod-caps    sends a long ALL-CAPS message (tests anti_caps)
//   automod-spam    sends the same message 4x quickly (tests anti_spam)
//   automod-mentions  pings several real members at once (tests max_mentions)
//   ghostping       sends a message pinging a member, then deletes it fast
//
// automod-* and ghostping tests also need TEST_CHANNEL_ID (a text channel
// the tester bot can send messages in). automod-mentions and ghostping will
// actually ping real members of your server — use a test server or warn
// people first.
import { Client, GatewayIntentBits, ChannelType } from "discord.js";

const test = process.argv[2];
const token = process.env.TEST_BOT_TOKEN;
const guildId = process.env.TEST_GUILD_ID;
const channelId = process.env.TEST_CHANNEL_ID;

const TESTS = [
  "nuke-channels",
  "nuke-roles",
  "automod-invite",
  "automod-caps",
  "automod-spam",
  "automod-mentions",
  "ghostping",
];

if (!token || !guildId || !TESTS.includes(test)) {
  console.error(`Usage: TEST_BOT_TOKEN=xxx TEST_GUILD_ID=xxx [TEST_CHANNEL_ID=xxx] node scripts/test-bot.mjs <test>`);
  console.error(`<test> must be one of: ${TESTS.join(", ")}`);
  process.exit(1);
}

const client = new Client({ intents: [GatewayIntentBits.Guilds, GatewayIntentBits.GuildMessages] });

async function nukeChannels(guild) {
  console.log("[test] Creating 4 temporary channels...");
  const channels = [];
  for (let i = 0; i < 4; i++) {
    const ch = await guild.channels.create({ name: `nuke-test-${i + 1}`, type: ChannelType.GuildText });
    channels.push(ch);
    console.log(`[test] Created #${ch.name}`);
  }
  console.log("[test] Waiting 2s, then deleting all of them rapidly...");
  await new Promise((r) => setTimeout(r, 2000));
  for (const ch of channels) {
    await ch.delete("nuke-test").catch((err) => console.error(`[test] Failed to delete ${ch.name}:`, err.message));
    console.log(`[test] Deleted #${ch.name}`);
  }
}

async function nukeRoles(guild) {
  console.log("[test] Creating 4 temporary roles...");
  const roles = [];
  for (let i = 0; i < 4; i++) {
    const role = await guild.roles.create({ name: `nuke-test-role-${i + 1}` });
    roles.push(role);
    console.log(`[test] Created role ${role.name}`);
  }
  console.log("[test] Waiting 2s, then deleting all of them rapidly...");
  await new Promise((r) => setTimeout(r, 2000));
  for (const role of roles) {
    await role.delete("nuke-test").catch((err) => console.error(`[test] Failed to delete ${role.name}:`, err.message));
    console.log(`[test] Deleted role ${role.name}`);
  }
}

async function requireChannel(guild) {
  if (!channelId) {
    console.error("This test needs TEST_CHANNEL_ID set to a text channel the tester bot can post in.");
    process.exit(1);
  }
  const channel = await guild.channels.fetch(channelId);
  if (!channel?.isTextBased()) {
    console.error("TEST_CHANNEL_ID is not a text channel.");
    process.exit(1);
  }
  return channel;
}

async function automodInvite(guild) {
  const channel = await requireChannel(guild);
  console.log("[test] Sending a message with a fake invite link...");
  await channel.send("hey check out my server discord.gg/fake-invite-test");
  console.log("[test] Sent. The message should get deleted/punished if anti_invite is on.");
}

async function automodCaps(guild) {
  const channel = await requireChannel(guild);
  console.log("[test] Sending an all-caps message...");
  await channel.send("THIS IS A LOUD ALL CAPS MESSAGE TO TEST AUTOMOD CAPS DETECTION");
  console.log("[test] Sent. The message should get flagged if anti_caps is on.");
}

async function automodSpam(guild) {
  const channel = await requireChannel(guild);
  console.log("[test] Sending the same message 4 times quickly...");
  for (let i = 0; i < 4; i++) {
    await channel.send("spam test message");
    console.log(`[test] Sent copy ${i + 1}/4`);
    await new Promise((r) => setTimeout(r, 500));
  }
  console.log("[test] Done. The last message should get flagged if anti_spam is on.");
}

async function automodMentions(guild) {
  const channel = await requireChannel(guild);
  console.log("[test] Fetching real members to mention (this WILL ping them)...");
  const members = await guild.members.fetch();
  const targets = members.filter((m) => !m.user.bot).first(6);
  if (targets.length < 2) {
    console.error("Not enough non-bot members in this server to test mention spam.");
    process.exit(1);
  }
  const mentionText = targets.map((m) => `<@${m.id}>`).join(" ");
  console.log(`[test] Mentioning ${targets.length} members...`);
  await channel.send(`${mentionText} mention spam test`);
  console.log("[test] Sent. Should get flagged if max_mentions is lower than the mention count.");
}

async function ghostping(guild) {
  const channel = await requireChannel(guild);
  const members = await guild.members.fetch();
  const target = members.filter((m) => !m.user.bot).first();
  if (!target) {
    console.error("No non-bot member found to ghost-ping.");
    process.exit(1);
  }
  console.log(`[test] Pinging ${target.user.tag} then deleting the message fast...`);
  const msg = await channel.send(`<@${target.id}>`);
  await new Promise((r) => setTimeout(r, 1000));
  await msg.delete();
  console.log("[test] Done. Should get flagged if anti-ghostping is on.");
}

client.once("ready", async () => {
  console.log(`[test] Logged in as ${client.user.tag}`);
  const guild = await client.guilds.fetch(guildId);

  const runners = {
    "nuke-channels": nukeChannels,
    "nuke-roles": nukeRoles,
    "automod-invite": automodInvite,
    "automod-caps": automodCaps,
    "automod-spam": automodSpam,
    "automod-mentions": automodMentions,
    ghostping,
  };

  await runners[test](guild);

  console.log("[test] Finished. Check the main bot's modlog channel for results.");
  client.destroy();
  process.exit(0);
});

client.login(token);
