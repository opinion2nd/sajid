import { SlashCommandBuilder, EmbedBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../types.js";
import { successEmbed, errorEmbed, COLORS } from "../util/embeds.js";
import { postWebhook, maskLicenseKey } from "../util/webhook.js";
import { getGuildSettings } from "../modules/settings.js";
import { getProduct } from "../modules/products.js";
import { redeemLicense } from "../modules/licenses.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("redeem")
    .setDescription("Redeem an unclaimed license key to your account")
    .addStringOption((o) => o.setName("key").setDescription("The license key you received").setRequired(true)),

  async execute(interaction: ChatInputCommandInteraction) {
    const guild = interaction.guild!;
    const key = interaction.options.getString("key", true).trim();
    const result = redeemLicense(guild.id, key, interaction.user.id);

    if (!result.success) {
      const message =
        result.reason === "already_redeemed"
          ? "This license key has already been redeemed."
          : result.reason === "expired"
            ? "This license key has expired."
            : result.reason === "wrong_guild"
              ? "This license key doesn't belong to this server."
              : "This license key does not exist.";
      await interaction.reply({ embeds: [errorEmbed(message)], ephemeral: true });
      return;
    }

    const license = result.license!;
    const product = getProduct(guild.id, license.productName);

    let roleNote = "";
    if (product?.customerRoleId) {
      const member = await guild.members.fetch(interaction.user.id).catch(() => null);
      if (member) {
        await member.roles.add(product.customerRoleId).catch(() => {
          roleNote = "\n⚠️ Couldn't assign the customer role (check the bot's permissions and role hierarchy).";
        });
      }
    }

    await interaction.reply({
      embeds: [
        successEmbed(
          `Redeemed your license for **${license.productName}**!` +
            (license.expiresAt ? ` Expires <t:${Math.floor(license.expiresAt / 1000)}:R>.` : " It never expires.") +
            roleNote,
        ),
      ],
      ephemeral: true,
    });

    await postWebhook(getGuildSettings(guild.id).webhookUrl, {
      embeds: [
        new EmbedBuilder()
          .setTitle("License Redeemed")
          .setColor(COLORS.success)
          .addFields(
            { name: "Product", value: license.productName, inline: true },
            { name: "User", value: `<@${interaction.user.id}>`, inline: true },
            { name: "License Key", value: maskLicenseKey(license.licenseKey), inline: true },
          )
          .toJSON(),
      ],
    });
  },
};

export default command;
