import "dotenv/config";
import { REST, Routes, SlashCommandBuilder, ChannelType, PermissionFlagsBits } from "discord.js";

const command = new SlashCommandBuilder()
  .setName("test")
  .setDescription("Run a defensive-feature test against this server (anti-nuke / automod / ghost-ping)")
  .setDefaultMemberPermissions(PermissionFlagsBits.Administrator)
  .addSubcommand((sc) => sc.setName("nuke-channels").setDescription("Create + rapidly delete channels (tests /security nuke)"))
  .addSubcommand((sc) => sc.setName("nuke-roles").setDescription("Create + rapidly delete roles (tests /security nuke)"))
  .addSubcommand((sc) =>
    sc
      .setName("nuke-webhooks")
      .setDescription("Create + rapidly delete webhooks in a channel (tests webhook-nuke detection)")
      .addChannelOption((o) =>
        o.setName("channel").setDescription("Channel to create webhooks in").setRequired(true).addChannelTypes(ChannelType.GuildText)
      )
  )
  .addSubcommand((sc) =>
    sc.setName("nuke-permissions").setDescription("Create temp channels and rapidly lock everyone out of them (tests permission-nuke detection)")
  )
  .addSubcommand((sc) =>
    sc
      .setName("automod-invite")
      .setDescription("Send a fake invite link (tests automod anti_invite)")
      .addChannelOption((o) =>
        o.setName("channel").setDescription("Channel to post in").setRequired(true).addChannelTypes(ChannelType.GuildText)
      )
  )
  .addSubcommand((sc) =>
    sc
      .setName("automod-caps")
      .setDescription("Send an ALL-CAPS message (tests automod anti_caps)")
      .addChannelOption((o) =>
        o.setName("channel").setDescription("Channel to post in").setRequired(true).addChannelTypes(ChannelType.GuildText)
      )
  )
  .addSubcommand((sc) =>
    sc
      .setName("automod-spam")
      .setDescription("Send the same message 4x quickly (tests automod anti_spam)")
      .addChannelOption((o) =>
        o.setName("channel").setDescription("Channel to post in").setRequired(true).addChannelTypes(ChannelType.GuildText)
      )
  )
  .addSubcommand((sc) =>
    sc
      .setName("automod-mentions")
      .setDescription("Ping several real members at once (tests automod max_mentions). Will actually ping people.")
      .addChannelOption((o) =>
        o.setName("channel").setDescription("Channel to post in").setRequired(true).addChannelTypes(ChannelType.GuildText)
      )
  )
  .addSubcommand((sc) =>
    sc
      .setName("ghostping")
      .setDescription("Ping a member then delete the message fast (tests ghost-ping detection). Will actually ping someone.")
      .addChannelOption((o) =>
        o.setName("channel").setDescription("Channel to post in").setRequired(true).addChannelTypes(ChannelType.GuildText)
      )
  );

const token = process.env.TEST_BOT_TOKEN;
const clientId = process.env.TEST_CLIENT_ID;
const guildId = process.env.TEST_GUILD_ID;

if (!token || !clientId || !guildId) {
  console.error("Set TEST_BOT_TOKEN, TEST_CLIENT_ID, and TEST_GUILD_ID in .env first.");
  process.exit(1);
}

const rest = new REST({ version: "10" }).setToken(token);

await rest.put(Routes.applicationGuildCommands(clientId, guildId), { body: [command.toJSON()] });
console.log("Registered /test command in the guild.");
