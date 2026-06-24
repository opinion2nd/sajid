import { SlashCommandBuilder, PermissionFlagsBits, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed, infoEmbed } from "../../util/embeds.js";
import { isAuthorized } from "../../lib/isAdmin.js";
import { issueLicense, revokeLicense, unbindLicense, getLicense, getLicenses } from "../../lib/licenseApiClient.js";

function statusOf(lic: { revokedAt: string | null; expiresAt: string | null }): string {
  if (lic.revokedAt) return "REVOKED";
  if (lic.expiresAt && new Date(lic.expiresAt) < new Date()) return "EXPIRED";
  return "ACTIVE";
}

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("license")
    .setDescription("Manage license keys for premium plugins and bots")
    .setDefaultMemberPermissions(PermissionFlagsBits.Administrator)
    .addSubcommand((sc) =>
      sc
        .setName("issue")
        .setDescription("Issue a new license key and DM it to a buyer")
        .addStringOption((o) => o.setName("product").setDescription("Product slug, e.g. antifreecam").setRequired(true))
        .addUserOption((o) => o.setName("user").setDescription("Buyer to DM the key to").setRequired(true))
        .addStringOption((o) => o.setName("expires").setDescription("Expiry date, YYYY-MM-DD (optional)"))
        .addStringOption((o) => o.setName("notes").setDescription("Internal notes, e.g. order ID (optional)"))
    )
    .addSubcommand((sc) =>
      sc
        .setName("revoke")
        .setDescription("Revoke a license key")
        .addStringOption((o) => o.setName("key").setDescription("License key").setRequired(true))
    )
    .addSubcommand((sc) =>
      sc
        .setName("unbind")
        .setDescription("Clear a license key's server binding so it can move to a new server")
        .addStringOption((o) => o.setName("key").setDescription("License key").setRequired(true))
    )
    .addSubcommand((sc) =>
      sc
        .setName("info")
        .setDescription("Show the status of a license key")
        .addStringOption((o) => o.setName("key").setDescription("License key").setRequired(true))
    )
    .addSubcommand((sc) =>
      sc
        .setName("list")
        .setDescription("List license keys")
        .addStringOption((o) => o.setName("product").setDescription("Filter by product slug (optional)"))
    ),

  async execute(interaction: ChatInputCommandInteraction) {
    if (!isAuthorized(interaction)) {
      await interaction.reply({ embeds: [errorEmbed("You are not authorized to manage licenses.")], ephemeral: true });
      return;
    }

    const sub = interaction.options.getSubcommand();

    if (sub === "issue") {
      const product = interaction.options.getString("product", true);
      const user = interaction.options.getUser("user", true);
      const expires = interaction.options.getString("expires") ?? undefined;
      const notes = interaction.options.getString("notes") ?? undefined;

      await interaction.deferReply({ ephemeral: true });
      const { key } = await issueLicense(product, notes, expires);

      let dmStatus = "DM sent to buyer.";
      try {
        await user.send({
          embeds: [infoEmbed(`You've received a license key for **${product}**:\n\`${key}\`\n\nKeep this key private — it only works on one server/instance.`)],
        });
      } catch {
        dmStatus = "Could not DM the buyer (their DMs may be closed) — relay the key manually.";
      }

      await interaction.editReply({
        embeds: [successEmbed(`Issued license for **${product}**\nKey: \`${key}\`\nBuyer: ${user.tag}\nExpires: ${expires ?? "never"}\n\n${dmStatus}`)],
      });
      return;
    }

    if (sub === "revoke") {
      const key = interaction.options.getString("key", true);
      await revokeLicense(key);
      await interaction.reply({ embeds: [successEmbed(`Revoked \`${key}\`.`)], ephemeral: true });
      return;
    }

    if (sub === "unbind") {
      const key = interaction.options.getString("key", true);
      await unbindLicense(key);
      await interaction.reply({ embeds: [successEmbed(`Unbound \`${key}\` — it can now be activated on a new server.`)], ephemeral: true });
      return;
    }

    if (sub === "info") {
      const key = interaction.options.getString("key", true);
      const lic = await getLicense(key);
      if (!lic) {
        await interaction.reply({ embeds: [errorEmbed(`No license found for \`${key}\`.`)], ephemeral: true });
        return;
      }
      await interaction.reply({
        embeds: [
          infoEmbed(
            `**${lic.key}**\nProduct: ${lic.product}\nStatus: ${statusOf(lic)}\nServer: ${lic.serverId ?? "unbound"}\nCreated: ${lic.createdAt}\nExpires: ${lic.expiresAt ?? "never"}\nNotes: ${lic.notes ?? "-"}`
          ),
        ],
        ephemeral: true,
      });
      return;
    }

    if (sub === "list") {
      const product = interaction.options.getString("product") ?? undefined;
      const licenses = await getLicenses(product);
      if (licenses.length === 0) {
        await interaction.reply({ embeds: [infoEmbed("No licenses found.")], ephemeral: true });
        return;
      }
      const shown = licenses.slice(0, 25);
      const lines = shown.map((lic) => `\`${lic.key}\` [${lic.product}] ${statusOf(lic)} — server: ${lic.serverId ?? "unbound"}`);
      const suffix = licenses.length > 25 ? `\n... and ${licenses.length - 25} more` : "";
      await interaction.reply({ embeds: [infoEmbed(lines.join("\n") + suffix)], ephemeral: true });
      return;
    }
  },
};

export default command;
