// Standalone anti-nuke test tool. NOT part of the main bot — run this with a
// separate "tester" bot's token (never the main bot's token) against a
// server you own, to verify /security nuke catches and punishes rapid
// channel deletion.
//
// Usage:
//   TEST_BOT_TOKEN=xxx TEST_GUILD_ID=xxx node scripts/nuke-test.mjs
//
// What it does: creates a few temporary text channels, waits a moment, then
// deletes them all in quick succession (faster than your configured
// nuke_window_seconds) so the main bot's anti-nuke should detect the
// TEST bot as the executor and ban/strip it per /security nuke settings.
import { Client, GatewayIntentBits, ChannelType } from "discord.js";

const token = process.env.TEST_BOT_TOKEN;
const guildId = process.env.TEST_GUILD_ID;
const channelCount = Number(process.env.NUKE_TEST_CHANNELS || 4);

if (!token || !guildId) {
  console.error("Set TEST_BOT_TOKEN and TEST_GUILD_ID env vars before running.");
  process.exit(1);
}

const client = new Client({ intents: [GatewayIntentBits.Guilds] });

client.once("ready", async () => {
  console.log(`[nuke-test] Logged in as ${client.user.tag}`);
  const guild = await client.guilds.fetch(guildId);

  console.log(`[nuke-test] Creating ${channelCount} temporary channels...`);
  const channels = [];
  for (let i = 0; i < channelCount; i++) {
    const ch = await guild.channels.create({ name: `nuke-test-${i + 1}`, type: ChannelType.GuildText });
    channels.push(ch);
    console.log(`[nuke-test] Created #${ch.name}`);
  }

  console.log("[nuke-test] Waiting 2s, then deleting all of them rapidly...");
  await new Promise((r) => setTimeout(r, 2000));

  for (const ch of channels) {
    await ch.delete("nuke-test").catch((err) => console.error(`[nuke-test] Failed to delete ${ch.name}:`, err.message));
    console.log(`[nuke-test] Deleted #${ch.name}`);
  }

  console.log("[nuke-test] Done. Check the main bot's modlog channel — this test bot should now be banned or have its roles stripped if anti-nuke is enabled.");
  client.destroy();
  process.exit(0);
});

client.login(token);
