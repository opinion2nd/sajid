import {
  SlashCommandBuilder,
  PermissionFlagsBits,
  EmbedBuilder,
  AttachmentBuilder,
  type ChatInputCommandInteraction,
  type AutocompleteInteraction,
} from "discord.js";
import type { Command } from "../types.js";
import { successEmbed, errorEmbed, infoEmbed, COLORS } from "../util/embeds.js";
import { parseDuration } from "../util/format.js";
import { postWebhook, maskLicenseKey } from "../util/webhook.js";
import { getGuildSettings } from "../modules/settings.js";
import { getProduct, listProducts } from "../modules/products.js";
import {
  createLicense,
  deleteLicense,
  getLicenseByKey,
  getLicensesByGuild,
  getLicensesByProduct,
  getLicensesByUser,
  clearHwidList,
  clearIpList,
} from "../modules/licenses.js";

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
        .addStringOption((o) => o.setName("product").setDescription("Product name").setRequired(true).setAutocomplete(true))
        .addUserOption((o) => o.setName("user").setDescription("User to receive the license").setRequired(true))
        .addStringOption((o) => o.setName("expires_in").setDescription("Duration until expiry, e.g. 30d, 12h (blank = never)"))
        .addIntegerOption((o) => o.setName("ip_cap").setDescription("Max distinct IPs allowed").setMinValue(0))
        .addIntegerOption((o) => o.setName("hwid_cap").setDescription("Max distinct HWIDs allowed").setMinValue(0)),
    )
    .addSubcommand((sc) =>
      sc
        .setName("generate")
        .setDescription("Bulk-generate unclaimed stock keys (customers redeem them later with /redeem)")
        .addStringOption((o) => o.setName("product").setDescription("Product name").setRequired(true).setAutocomplete(true))
        .addIntegerOption((o) => o.setName("count").setDescription("How many keys to generate").setRequired(true).setMinValue(1).setMaxValue(100))
        .addStringOption((o) => o.setName("expires_after_redeem").setDescription("Expiry timer starts when the buyer redeems, e.g. 30d, 12h"))
        .addIntegerOption((o) => o.setName("ip_cap").setDescription("Max distinct IPs allowed").setMinValue(0))
        .addIntegerOption((o) => o.setName("hwid_cap").setDescription("Max distinct HWIDs allowed").setMinValue(0)),
    )
    .addSubcommand((sc) =>
      sc
        .setName("delete")
        .setDescription("Delete a license key")
        .addStringOption((o) => o.setName("key").setDescription("License key").setRequired(true)),
    )
    .addSubcommand((sc) =>
      sc
        .setName("list")
        .setDescription("List license keys")
        .addStringOption((o) => o.setName("product").setDescription("Filter by product").setAutocomplete(true))
        .addUserOption((o) => o.setName("user").setDescription("Filter by user")),
    )
    .addSubcommand((sc) =>
      sc
        .setName("get")
        .setDescription("Show details for a license key")
        .addStringOption((o) => o.setName("key").setDescription("License key").setRequired(true)),
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
            .addChoices({ name: "IP addresses", value: "ip" }, { name: "HWIDs", value: "hwid" }, { name: "Both", value: "both" }),
        ),
    ),

  async autocomplete(interaction: AutocompleteInteraction) {
    const focused = interaction.options.getFocused().toLowerCase();
    const choices = listProducts(interaction.guild!.id)
      .filter((p) => p.name.toLowerCase().includes(focused))
      .slice(0, 25)
      .map((p) => ({ name: p.name, value: p.name }));
    await interaction.respond(choices);
  },

  async execute(interaction: ChatInputCommandInteraction) {
    const sub = interaction.options.getSubcommand();
    const guild = interaction.guild!;
    const webhookUrl = getGuildSettings(guild.id).webhookUrl;

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
      const existing = getLicensesByUser(guild.id, targetUser.id).filter((l) => l.productName === product.name);

      const license = createLicense({
        guildId: guild.id,
        productName: product.name,
        discordUserId: targetUser.id,
        createdBy: interaction.user.id,
        expiresAt,
        ipCap: interaction.options.getInteger("ip_cap") ?? product.defaultIpCap,
        hwidCap: interaction.options.getInteger("hwid_cap") ?? product.defaultHwidCap,
      });

      let roleNote = "";
      if (product.customerRoleId) {
        const member = await guild.members.fetch(targetUser.id).catch(() => null);
        if (member) {
          await member.roles.add(product.customerRoleId).catch(() => {
            roleNote = "\n⚠️ Couldn't assign the customer role (check my permissions and role hierarchy).";
          });
        }
      }

      const dmEmbed = new EmbedBuilder()
        .setTitle("Your License Key")
        .setColor(COLORS.info)
        .setDescription(
          "Sharing this key with anyone will result in your license being **permanently** disabled and your access to the product **removed**.",
        )
        .addFields(
          { name: "Product", value: product.name, inline: true },
          { name: "Expires", value: formatExpiry(license.expiresAt), inline: true },
          { name: "License Key", value: `\`\`\`${license.licenseKey}\`\`\`` },
        );
      const dmSent = await targetUser.send({ embeds: [dmEmbed] }).then(
        () => true,
        () => false,
      );

      await interaction.reply({
        embeds: [
          successEmbed(
            `Created a license for ${targetUser} on **${product.name}**.` +
              (dmSent ? " It was sent to their DMs." : "\n⚠️ I couldn't DM them — ask them to enable DMs and run `/getlicense`.") +
              (existing.length > 0 ? `\n⚠️ This user already had ${existing.length} license(s) for this product.` : "") +
              roleNote,
          ),
        ],
      });

      await postWebhook(webhookUrl, {
        embeds: [
          new EmbedBuilder()
            .setTitle("License Created")
            .setColor(COLORS.success)
            .addFields(
              { name: "Product", value: product.name, inline: true },
              { name: "User", value: `<@${targetUser.id}>`, inline: true },
              { name: "License Key", value: maskLicenseKey(license.licenseKey), inline: true },
              { name: "Created By", value: `<@${interaction.user.id}>`, inline: true },
            )
            .toJSON(),
        ],
      });
      return;
    }

    if (sub === "generate") {
      const productName = interaction.options.getString("product", true).trim();
      const product = getProduct(guild.id, productName);
      if (!product) {
        await interaction.reply({ embeds: [errorEmbed("A product with that name doesn't exist.")], ephemeral: true });
        return;
      }

      const count = interaction.options.getInteger("count", true);
      const expiresAfterRedeemRaw = interaction.options.getString("expires_after_redeem");
      let expiresInMsOnRedeem: number | null = null;
      if (expiresAfterRedeemRaw) {
        expiresInMsOnRedeem = parseDuration(expiresAfterRedeemRaw);
        if (expiresInMsOnRedeem === null) {
          await interaction.reply({ embeds: [errorEmbed("Invalid duration format. Try `30d`, `12h`, `45m`.")], ephemeral: true });
          return;
        }
      }

      const ipCap = interaction.options.getInteger("ip_cap") ?? product.defaultIpCap;
      const hwidCap = interaction.options.getInteger("hwid_cap") ?? product.defaultHwidCap;

      const keys: string[] = [];
      for (let i = 0; i < count; i++) {
        const license = createLicense({
          guildId: guild.id,
          productName: product.name,
          discordUserId: null,
          createdBy: interaction.user.id,
          expiresInMsOnRedeem,
          ipCap,
          hwidCap,
        });
        keys.push(license.licenseKey);
      }

      const file = new AttachmentBuilder(Buffer.from(keys.join("\n"), "utf8"), {
        name: `${product.name}-keys.txt`,
      });
      await interaction.reply({
        embeds: [
          successEmbed(
            `Generated **${count}** unclaimed key(s) for **${product.name}**. Buyers redeem theirs with \`/redeem <key>\`.`,
          ),
        ],
        files: [file],
        ephemeral: true,
      });

      await postWebhook(webhookUrl, {
        embeds: [
          new EmbedBuilder()
            .setTitle("Stock Keys Generated")
            .setColor(COLORS.success)
            .addFields(
              { name: "Product", value: product.name, inline: true },
              { name: "Count", value: String(count), inline: true },
              { name: "Created By", value: `<@${interaction.user.id}>`, inline: true },
            )
            .toJSON(),
        ],
      });
      return;
    }

    if (sub === "delete") {
      const key = interaction.options.getString("key", true).trim();
      const license = getLicenseByKey(key);
      if (!license || license.guildId !== guild.id) {
        await interaction.reply({ embeds: [errorEmbed("This license key does not exist.")], ephemeral: true });
        return;
      }
      const product = getProduct(guild.id, license.productName);
      if (product?.customerRoleId && license.discordUserId) {
        const member = await guild.members.fetch(license.discordUserId).catch(() => null);
        if (member?.roles.cache.has(product.customerRoleId)) {
          await member.roles.remove(product.customerRoleId).catch(() => {});
        }
      }
      deleteLicense(key);
      await interaction.reply({ embeds: [successEmbed("You successfully deleted this license key!")] });
      await postWebhook(webhookUrl, {
        embeds: [
          new EmbedBuilder()
            .setTitle("License Deleted")
            .setColor(COLORS.error)
            .addFields(
              { name: "License Key", value: maskLicenseKey(key), inline: true },
              { name: "Deleted By", value: `<@${interaction.user.id}>`, inline: true },
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
      if (productFilter && userFilter) licenses = licenses.filter((l) => l.productName.toLowerCase() === productFilter.toLowerCase());

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
            .map(
              (l) =>
                `\`${l.licenseKey}\` — **${l.productName}** — ${l.discordUserId ? `<@${l.discordUserId}>` : "*unredeemed*"} — expires ${formatExpiry(l.expiresAt)}`,
            )
            .join("\n") + (licenses.length > shown.length ? `\n…and ${licenses.length - shown.length} more.` : ""),
        );
      await interaction.reply({ embeds: [embed], ephemeral: true });
      return;
    }

    if (sub === "get") {
      const key = interaction.options.getString("key", true).trim();
      const license = getLicenseByKey(key);
      if (!license || license.guildId !== guild.id) {
        await interaction.reply({ embeds: [errorEmbed("This license key does not exist.")], ephemeral: true });
        return;
      }
      const embed = new EmbedBuilder()
        .setTitle(`License: ${license.licenseKey}`)
        .setColor(COLORS.info)
        .addFields(
          { name: "Product", value: license.productName, inline: true },
          { name: "User", value: license.discordUserId ? `<@${license.discordUserId}>` : "*unredeemed*", inline: true },
          { name: "Created", value: `<t:${Math.floor(license.createdAt / 1000)}:R>`, inline: true },
          { name: "Expires", value: formatExpiry(license.expiresAt), inline: true },
          { name: "Total Requests", value: String(license.totalRequests), inline: true },
          {
            name: "IPs",
            value: `${license.ips.length}/${license.ipCap} used${license.ips.length ? `\n${license.ips.join(", ")}` : ""}`,
          },
          {
            name: "HWIDs",
            value: `${license.hwids.length}/${license.hwidCap} used${license.hwids.length ? `\n${license.hwids.join(", ")}` : ""}`,
          },
        );
      await interaction.reply({ embeds: [embed], ephemeral: true });
      return;
    }

    if (sub === "cleardata") {
      const key = interaction.options.getString("key", true).trim();
      const target = interaction.options.getString("target", true);
      const license = getLicenseByKey(key);
      if (!license || license.guildId !== guild.id) {
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
                : "You successfully deleted the HWID data of this license key!",
          ),
        ],
      });
    }
  },
};

export default command;
