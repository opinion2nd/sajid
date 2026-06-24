import { SlashCommandBuilder, PermissionFlagsBits, EmbedBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed, infoEmbed, COLORS } from "../../util/embeds.js";
import { parseDuration } from "../../util/format.js";
import { postWebhook, maskLicenseKey } from "../../util/webhook.js";
import { getProduct } from "../../modules/products.js";
import {
  createLicense,
  deleteLicense,
  getLicenseByKey,
  getLicensesByGuild,
  getLicensesByProduct,
  getLicensesByUser,
  getHwidList,
  getIpList,
  clearHwidList,
  clearIpList,
} from "../../modules/licenses.js";

function formatExpiry(expiresAt: number | null): string {
  if (!expiresAt) return "Never";
  return `<t:${Math.floor(expiresAt / 1000)}:R>`;
}

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("license")
    .setDescription("Manage license keys")
    .setDefaultMemberPermissions(PermissionFlagsBits.ManageGuild)
    .addSubcommand((sc) =>
      sc
        .setName("create")
        .setDescription("Create a new license key for a user")
        .addStringOption((o) => o.setName("product").setDescription("Product name").setRequired(true))
        .addUserOption((o) => o.setName("user").setDescription("User to receive the license").setRequired(true))
        .addStringOption((o) => o.setName("builtbybit_id").setDescription("Buyer's BuiltByBit user ID"))
        .addStringOption((o) => o.setName("expires_in").setDescription("Duration until expiry, e.g. 30d, 12h (blank = never)"))
        .addIntegerOption((o) => o.setName("ip_cap").setDescription("Max distinct IPs allowed").setMinValue(0))
        .addIntegerOption((o) => o.setName("hwid_cap").setDescription("Max distinct HWIDs allowed").setMinValue(0))
    )
    .addSubcommand((sc) =>
      sc
        .setName("delete")
        .setDescription("Delete a license key")
        .addStringOption((o) => o.setName("key").setDescription("License key").setRequired(true))
    )
    .addSubcommand((sc) =>
      sc
        .setName("list")
        .setDescription("List license keys")
        .addStringOption((o) => o.setName("product").setDescription("Filter by product"))
        .addUserOption((o) => o.setName("user").setDescription("Filter by user"))
    )
    .addSubcommand((sc) =>
      sc
        .setName("get")
        .setDescription("Show details for a license key")
        .addStringOption((o) => o.setName("key").setDescription("License key").setRequired(true))
    )
    .addSubcommand((sc) =>
      sc
        .setName("cleardata")
        .setDescription("Clear IP/HWID data from a license key")
        .addStringOption((o) => o.setName("key").setDescription("License key").setRequired(true))
        .addStringOption((o) =>
          o
            .setName("target")
            .setDescription("What to clear")
            .setRequired(true)
            .addChoices({ name: "IP addresses", value: "ip" }, { name: "HWIDs", value: "hwid" }, { name: "Both", value: "both" })
        )
    ),

  async execute(interaction: ChatInputCommandInteraction) {
    const sub = interaction.options.getSubcommand();
    const guild = interaction.guild!;

    if (sub === "create") {
      const productName = interaction.options.getString("product", true).trim();
      const product = getProduct(guild.id, productName);
      if (!product) {
        await interaction.reply({ embeds: [errorEmbed("A product with that name doesn't exist.")], ephemeral: true });
        return;
      }

      const expiresInRaw = interaction.options.getString("expires_in");
      let expiresAt: number | null = null;
      if (expiresInRaw) {
        const durationMs = parseDuration(expiresInRaw);
        if (durationMs === null) {
          await interaction.reply({ embeds: [errorEmbed("Invalid duration format. Try `30d`, `12h`, `45m`.")], ephemeral: true });
          return;
        }
        expiresAt = Date.now() + durationMs;
      }

      const targetUser = interaction.options.getUser("user", true);
      const existing = getLicensesByUser(guild.id, targetUser.id).filter((l) => l.product_name === product.name);

      const license = createLicense({
        guildId: guild.id,
        productName: product.name,
        discordUserId: targetUser.id,
        builtbybitUserId: interaction.options.getString("builtbybit_id"),
        createdBy: interaction.user.id,
        expiresAt,
        ipCap: interaction.options.getInteger("ip_cap") ?? product.default_ip_cap,
        hwidCap: interaction.options.getInteger("hwid_cap") ?? product.default_hwid_cap,
      });

      let roleNote = "";
      if (product.customer_role_id) {
        const member = await guild.members.fetch(targetUser.id).catch(() => null);
        if (member) {
          await member.roles.add(product.customer_role_id).catch(() => {
            roleNote = "\n⚠️ Couldn't assign the customer role (check my permissions and role hierarchy).";
          });
        }
      }

      const dmEmbed = new EmbedBuilder()
        .setTitle("Your License Key")
        .setColor(COLORS.info)
        .setDescription(
          "Sharing this key with anyone will result in your license being **permanently** disabled and your access to the product **removed**."
        )
        .addFields(
          { name: "Product", value: product.name, inline: true },
          { name: "Expires", value: formatExpiry(license.expires_at), inline: true },
          { name: "License Key", value: `\`\`\`${license.license_key}\`\`\`` }
        );
      const dmSent = await targetUser.send({ embeds: [dmEmbed] }).then(
        () => true,
        () => false
      );

      await interaction.reply({
        embeds: [
          successEmbed(
            `Created a license for ${targetUser} on **${product.name}**.` +
              (dmSent ? " It was sent to their DMs." : "\n⚠️ I couldn't DM them — ask them to enable DMs and run `/getlicense`.") +
              (existing.length > 0 ? `\n⚠️ This user already had ${existing.length} license(s) for this product.` : "") +
              roleNote
          ),
        ],
      });

      await postWebhook(process.env.LICENSE_WEBHOOK_URL, {
        embeds: [
          new EmbedBuilder()
            .setTitle("License Created")
            .setColor(COLORS.success)
            .addFields(
              { name: "Product", value: product.name, inline: true },
              { name: "User", value: `<@${targetUser.id}>`, inline: true },
              { name: "License Key", value: maskLicenseKey(license.license_key), inline: true },
              { name: "Created By", value: `<@${interaction.user.id}>`, inline: true }
            )
            .toJSON(),
        ],
      });
      return;
    }

    if (sub === "delete") {
      const key = interaction.options.getString("key", true).trim();
      const license = getLicenseByKey(key);
      if (!license || license.guild_id !== guild.id) {
        await interaction.reply({ embeds: [errorEmbed("This license key does not exist.")], ephemeral: true });
        return;
      }
      const product = getProduct(guild.id, license.product_name);
      if (product?.customer_role_id) {
        const member = await guild.members.fetch(license.discord_user_id).catch(() => null);
        if (member?.roles.cache.has(product.customer_role_id)) {
          await member.roles.remove(product.customer_role_id).catch(() => {});
        }
      }
      deleteLicense(key);
      await interaction.reply({ embeds: [successEmbed("You successfully deleted this license key!")] });
      await postWebhook(process.env.LICENSE_WEBHOOK_URL, {
        embeds: [
          new EmbedBuilder()
            .setTitle("License Deleted")
            .setColor(COLORS.error)
            .addFields(
              { name: "License Key", value: maskLicenseKey(key), inline: true },
              { name: "Deleted By", value: `<@${interaction.user.id}>`, inline: true }
            )
            .toJSON(),
        ],
      });
      return;
    }

    if (sub === "list") {
      const productFilter = interaction.options.getString("product");
      const userFilter = interaction.options.getUser("user");
      let licenses = userFilter
        ? getLicensesByUser(guild.id, userFilter.id)
        : productFilter
          ? getLicensesByProduct(guild.id, productFilter)
          : getLicensesByGuild(guild.id);
      if (productFilter && userFilter) licenses = licenses.filter((l) => l.product_name === productFilter);

      if (licenses.length === 0) {
        await interaction.reply({ embeds: [infoEmbed("There's no license keys yet!")], ephemeral: true });
        return;
      }

      const shown = licenses.slice(0, 20);
      const embed = new EmbedBuilder()
        .setTitle(`Licenses (${licenses.length})`)
        .setColor(COLORS.info)
        .setDescription(
          shown
            .map((l) => `\`${l.license_key}\` — **${l.product_name}** — <@${l.discord_user_id}> — expires ${formatExpiry(l.expires_at)}`)
            .join("\n") + (licenses.length > shown.length ? `\n…and ${licenses.length - shown.length} more.` : "")
        );
      await interaction.reply({ embeds: [embed], ephemeral: true });
      return;
    }

    if (sub === "get") {
      const key = interaction.options.getString("key", true).trim();
      const license = getLicenseByKey(key);
      if (!license || license.guild_id !== guild.id) {
        await interaction.reply({ embeds: [errorEmbed("This license key does not exist.")], ephemeral: true });
        return;
      }
      const ips = getIpList(key);
      const hwids = getHwidList(key);
      const embed = new EmbedBuilder()
        .setTitle(`License: ${license.license_key}`)
        .setColor(COLORS.info)
        .addFields(
          { name: "Product", value: license.product_name, inline: true },
          { name: "User", value: `<@${license.discord_user_id}>`, inline: true },
          { name: "BuiltByBit ID", value: license.builtbybit_user_id ?? "*none*", inline: true },
          { name: "Created", value: `<t:${Math.floor(license.created_at / 1000)}:R>`, inline: true },
          { name: "Expires", value: formatExpiry(license.expires_at), inline: true },
          { name: "Total Requests", value: String(license.total_requests), inline: true },
          { name: "IPs", value: `${ips.length}/${license.ip_cap} used${ips.length ? `\n${ips.map((r) => r.ip).join(", ")}` : ""}` },
          { name: "HWIDs", value: `${hwids.length}/${license.hwid_cap} used${hwids.length ? `\n${hwids.map((r) => r.hwid).join(", ")}` : ""}` }
        );
      await interaction.reply({ embeds: [embed], ephemeral: true });
      return;
    }

    if (sub === "cleardata") {
      const key = interaction.options.getString("key", true).trim();
      const target = interaction.options.getString("target", true);
      const license = getLicenseByKey(key);
      if (!license || license.guild_id !== guild.id) {
        await interaction.reply({ embeds: [errorEmbed("This license key does not exist.")], ephemeral: true });
        return;
      }
      if (target === "ip" || target === "both") clearIpList(key);
      if (target === "hwid" || target === "both") clearHwidList(key);
      await interaction.reply({
        embeds: [
          successEmbed(
            target === "both"
              ? "You successfully deleted the IP and HWID data of this license key!"
              : target === "ip"
                ? "You successfully deleted the IP data of this license key!"
                : "You successfully deleted the HWID data of this license key!"
          ),
        ],
      });
    }
  },
};

export default command;
