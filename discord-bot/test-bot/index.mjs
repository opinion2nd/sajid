// Tester bot: run defensive-feature tests against your own server with a
// slash command instead of a CLI script. Use a SEPARATE bot account from
// your main bot -- the main bot is exempt from its own anti-nuke
// punishment, so it can't usefully test itself.
//
// Setup: copy .env.example to .env and fill in TEST_BOT_TOKEN,
// TEST_CLIENT_ID, TEST_GUILD_ID. Enable the "Server Members Intent" in the
// Bot tab for this application (needed for automod-mentions/ghostping).
// Invite it with Manage Channels + Manage Roles + Send Messages, then:
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
  const lines = ["Creating 4 temporary channels..."];
  const channels = [];
  for (let i = 0; i < 4; i++) {
    const ch = await guild.channels.create({ name: `nuke-test-${i + 1}`, type: 0 });
    channels.push(ch);
    lines.push(`Created #${ch.name}`);
  }
  lines.push("Waiting 2s, then deleting all of them rapidly...");
  await new Promise((r) => setTimeout(r, 2000));
  for (const ch of channels) {
    await ch.delete("nuke-test").catch((err) => lines.push(`Failed to delete ${ch.name}: ${err.message}`));
    lines.push(`Deleted #${ch.name}`);
  }
  return lines;
}

async function nukeRoles(guild) {
  const lines = ["Creating 4 temporary roles..."];
  const roles = [];
  for (let i = 0; i < 4; i++) {
    const role = await guild.roles.create({ name: `nuke-test-role-${i + 1}` });
    roles.push(role);
    lines.push(`Created role ${role.name}`);
  }
  lines.push("Waiting 2s, then deleting all of them rapidly...");
  await new Promise((r) => setTimeout(r, 2000));
  for (const role of roles) {
    await role.delete("nuke-test").catch((err) => lines.push(`Failed to delete ${role.name}: ${err.message}`));
    lines.push(`Deleted role ${role.name}`);
  }
  return lines;
}

async function nukeWebhooks(channel) {
  const lines = ["Creating 4 temporary webhooks in this channel..."];
  const hooks = [];
  for (let i = 0; i < 4; i++) {
    const hook = await channel.createWebhook({ name: `nuke-test-hook-${i + 1}` });
    hooks.push(hook);
    lines.push(`Created webhook ${hook.name}`);
  }
  lines.push("Waiting 2s, then deleting all of them rapidly...");
  await new Promise((r) => setTimeout(r, 2000));
  for (const hook of hooks) {
    await hook.delete("nuke-test").catch((err) => lines.push(`Failed to delete ${hook.name}: ${err.message}`));
    lines.push(`Deleted webhook ${hook.name}`);
  }
  return lines;
}

async function nukePermissions(guild) {
  const lines = ["Creating 4 temporary channels to churn permission overwrites on..."];
  const channels = [];
  for (let i = 0; i < 4; i++) {
    const ch = await guild.channels.create({ name: `perm-test-${i + 1}`, type: 0 });
    channels.push(ch);
    lines.push(`Created #${ch.name}`);
  }
  lines.push("Rapidly denying @everyone's View Channel permission on each...");
  for (const ch of channels) {
    await ch.permissionOverwrites.edit(guild.roles.everyone, { ViewChannel: false }, { reason: "nuke-test" });
    lines.push(`Locked #${ch.name}`);
  }
  lines.push("Cleaning up the temporary channels...");
  for (const ch of channels) {
    await ch.delete("nuke-test").catch((err) => lines.push(`Failed to delete ${ch.name}: ${err.message}`));
  }
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
  const lines = [];
  for (let i = 0; i < 4; i++) {
    await channel.send("spam test message");
    lines.push(`Sent copy ${i + 1}/4`);
    await new Promise((r) => setTimeout(r, 500));
  }
  lines.push("Done. The last message should get flagged if anti_spam is on.");
  return lines;
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
  await new Promise((r) => setTimeout(r, 1000));
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
