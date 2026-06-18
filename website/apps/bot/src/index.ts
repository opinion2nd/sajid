import {
  Client,
  GatewayIntentBits,
  Events,
  type Interaction,
} from "discord.js";
import prisma from "@brothercraft/db";

// Brother Craft Discord bot.
//   /link <code>  — links a Discord account to a marketplace user (code is
//                   generated on the website dashboard) and grants the customer
//                   role if the user owns any product.
//   /licenses     — DMs the user their license keys.
//   /lookup <key> — (staff) shows license status.
//
// Requires DISCORD_BOT_TOKEN. Optional: DISCORD_GUILD_ID, DISCORD_CUSTOMER_ROLE_ID.

const token = process.env.DISCORD_BOT_TOKEN;
if (!token) {
  console.error("DISCORD_BOT_TOKEN is not set — bot cannot start.");
  process.exit(1);
}

const CUSTOMER_ROLE_ID = process.env.DISCORD_CUSTOMER_ROLE_ID;

const client = new Client({ intents: [GatewayIntentBits.Guilds] });

client.once(Events.ClientReady, (c) => {
  console.log(`Brother Craft bot online as ${c.user.tag}`);
});

async function ownsAnyProduct(userId: string): Promise<boolean> {
  const order = await prisma.order.findFirst({
    where: { buyerId: userId, status: { in: ["PAID", "COMPLETED", "ESCROW_HELD"] } },
  });
  return Boolean(order);
}

async function grantCustomerRole(interaction: Interaction, userId: string) {
  if (!CUSTOMER_ROLE_ID || !interaction.guild || !interaction.member) return;
  if (!(await ownsAnyProduct(userId))) return;
  try {
    const member = await interaction.guild.members.fetch(interaction.user.id);
    await member.roles.add(CUSTOMER_ROLE_ID);
  } catch (e) {
    console.warn("Could not assign customer role:", e);
  }
}

client.on(Events.InteractionCreate, async (interaction) => {
  if (!interaction.isChatInputCommand()) return;

  // ── /link ─────────────────────────────────────────
  if (interaction.commandName === "link") {
    const code = interaction.options.getString("code", true).trim().toUpperCase();
    const entry = await prisma.discordLinkCode.findUnique({ where: { code } });
    if (!entry || entry.expiresAt < new Date()) {
      await interaction.reply({
        content: "That code is invalid or has expired. Generate a new one from your dashboard.",
        ephemeral: true,
      });
      return;
    }
    await prisma.discordLink.upsert({
      where: { userId: entry.userId },
      update: {
        discordId: interaction.user.id,
        discordUsername: interaction.user.username,
        rolesSynced: true,
      },
      create: {
        userId: entry.userId,
        discordId: interaction.user.id,
        discordUsername: interaction.user.username,
        rolesSynced: true,
      },
    });
    await prisma.discordLinkCode.delete({ where: { code } });
    await grantCustomerRole(interaction, entry.userId);
    await interaction.reply({
      content: "✅ Your Discord account is now linked to Brother Craft.",
      ephemeral: true,
    });
    return;
  }

  // ── /licenses ─────────────────────────────────────
  if (interaction.commandName === "licenses") {
    const link = await prisma.discordLink.findUnique({
      where: { discordId: interaction.user.id },
    });
    if (!link) {
      await interaction.reply({
        content: "Link your account first with `/link <code>`.",
        ephemeral: true,
      });
      return;
    }
    const licenses = await prisma.license.findMany({
      where: { buyerId: link.userId },
      include: { product: { select: { title: true } } },
    });
    if (licenses.length === 0) {
      await interaction.reply({ content: "You have no licenses yet.", ephemeral: true });
      return;
    }
    const body = licenses
      .map((l) => `• **${l.product.title}** — \`${l.key}\` (${l.status})`)
      .join("\n");
    await interaction.user.send(`Your Brother Craft licenses:\n${body}`).catch(() => {});
    await interaction.reply({ content: "📬 Sent your licenses by DM.", ephemeral: true });
    return;
  }

  // ── /lookup ───────────────────────────────────────
  if (interaction.commandName === "lookup") {
    const key = interaction.options.getString("key", true).trim().toUpperCase();
    const license = await prisma.license.findUnique({
      where: { key },
      include: {
        product: { select: { title: true } },
        activations: true,
      },
    });
    if (!license) {
      await interaction.reply({ content: "No license found for that key.", ephemeral: true });
      return;
    }
    await interaction.reply({
      content: [
        `**${license.product.title}**`,
        `Status: ${license.status}`,
        `Activations: ${license.activations.length}/${license.maxActivations}`,
        license.expiresAt ? `Expires: ${license.expiresAt.toISOString()}` : "No expiry",
      ].join("\n"),
      ephemeral: true,
    });
    return;
  }
});

client.login(token);
