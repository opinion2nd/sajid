import {
  SlashCommandBuilder,
  PermissionFlagsBits,
  EmbedBuilder,
  type ChatInputCommandInteraction,
  type AutocompleteInteraction,
} from "discord.js";
import type { Command } from "../types.js";
import { successEmbed, errorEmbed, infoEmbed, COLORS } from "../util/embeds.js";
import { createProduct, deleteProduct, getProduct, listProducts } from "../modules/products.js";
import { getLicensesByProduct } from "../modules/licenses.js";
import { getGuildSettings } from "../modules/settings.js";
import { postWebhook } from "../util/webhook.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("product")
    .setDescription("Manage licensable products")
    .setDefaultMemberPermissions(PermissionFlagsBits.ManageGuild)
    .addSubcommand((sc) =>
      sc
        .setName("create")
        .setDescription("Create a new product")
        .addStringOption((o) => o.setName("name").setDescription("Product name (no spaces)").setRequired(true))
        .addRoleOption((o) => o.setName("customer_role").setDescription("Role granted to license holders"))
        .addIntegerOption((o) => o.setName("default_ip_cap").setDescription("Default max IPs per license").setMinValue(0))
        .addIntegerOption((o) => o.setName("default_hwid_cap").setDescription("Default max HWIDs per license").setMinValue(0)),
    )
    .addSubcommand((sc) =>
      sc
        .setName("delete")
        .setDescription("Delete a product")
        .addStringOption((o) =>
          o.setName("name").setDescription("Product name").setRequired(true).setAutocomplete(true),
        )
        .addBooleanOption((o) => o.setName("cascade").setDescription("Also delete all licenses for this product")),
    )
    .addSubcommand((sc) => sc.setName("list").setDescription("List all products")),

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
    const guildId = interaction.guild!.id;
    const webhookUrl = getGuildSettings(guildId).webhookUrl;

    if (sub === "create") {
      const name = interaction.options.getString("name", true).trim();
      if (/\s/.test(name)) {
        await interaction.reply({ embeds: [errorEmbed("A product name can't contain spaces.")], ephemeral: true });
        return;
      }
      if (getProduct(guildId, name)) {
        await interaction.reply({ embeds: [errorEmbed("A product with that name already exists.")], ephemeral: true });
        return;
      }

      const role = interaction.options.getRole("customer_role");
      const product = createProduct({
        guildId,
        name,
        customerRoleId: role?.id ?? null,
        defaultIpCap: interaction.options.getInteger("default_ip_cap") ?? 1,
        defaultHwidCap: interaction.options.getInteger("default_hwid_cap") ?? 1,
        createdBy: interaction.user.id,
      });

      await interaction.reply({
        embeds: [
          successEmbed(
            `Created product **${product.name}**.\nCustomer role: ${role ? role.toString() : "*none*"}\n` +
              `Default IP cap: ${product.defaultIpCap} • Default HWID cap: ${product.defaultHwidCap}`,
          ),
        ],
      });
      await postWebhook(webhookUrl, {
        embeds: [
          new EmbedBuilder()
            .setTitle("Product Created")
            .setColor(COLORS.success)
            .addFields(
              { name: "Product", value: product.name, inline: true },
              { name: "Created By", value: `<@${interaction.user.id}>`, inline: true },
            )
            .toJSON(),
        ],
      });
      return;
    }

    if (sub === "delete") {
      const name = interaction.options.getString("name", true).trim();
      const cascade = interaction.options.getBoolean("cascade") ?? false;
      if (!getProduct(guildId, name)) {
        await interaction.reply({ embeds: [errorEmbed("A product with that name doesn't exist.")], ephemeral: true });
        return;
      }
      const licenseCount = getLicensesByProduct(guildId, name).length;
      deleteProduct(guildId, name, cascade);
      await interaction.reply({
        embeds: [
          successEmbed(`Deleted product **${name}**.` + (cascade ? ` Also removed ${licenseCount} license(s).` : "")),
        ],
      });
      await postWebhook(webhookUrl, {
        embeds: [
          new EmbedBuilder()
            .setTitle("Product Deleted")
            .setColor(COLORS.error)
            .addFields(
              { name: "Product", value: name, inline: true },
              { name: "Deleted By", value: `<@${interaction.user.id}>`, inline: true },
            )
            .toJSON(),
        ],
      });
      return;
    }

    // list
    const products = listProducts(guildId);
    if (products.length === 0) {
      await interaction.reply({ embeds: [infoEmbed("There are no products yet.")], ephemeral: true });
      return;
    }
    const embed = new EmbedBuilder()
      .setTitle("Products")
      .setColor(COLORS.info)
      .addFields(
        products.slice(0, 25).map((p) => ({
          name: p.name,
          value: `Role: ${p.customerRoleId ? `<@&${p.customerRoleId}>` : "*none*"}\nIP cap: ${p.defaultIpCap} • HWID cap: ${p.defaultHwidCap}`,
        })),
      );
    await interaction.reply({ embeds: [embed] });
  },
};

export default command;
