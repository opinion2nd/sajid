import { SlashCommandBuilder, PermissionFlagsBits, type ChatInputCommandInteraction, type GuildMember } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed, brandEmbed } from "../../util/embeds.js";
import {
  addShopItem,
  removeShopItem,
  getShopItems,
  getShopItem,
  spend,
  addToInventory,
  CURRENCY,
} from "../../modules/economy.js";
import { refreshBaltopPanel } from "../../modules/ecoleaderboardpanel.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("shop")
    .setDescription("Browse and buy server shop items")
    .addSubcommand((sc) => sc.setName("view").setDescription("View the items for sale"))
    .addSubcommand((sc) =>
      sc
        .setName("buy")
        .setDescription("Buy an item from the shop")
        .addIntegerOption((o) => o.setName("id").setDescription("Item ID (see /shop view)").setRequired(true).setMinValue(1))
    )
    .addSubcommand((sc) =>
      sc
        .setName("additem")
        .setDescription("(Admin) Add an item to the shop")
        .addStringOption((o) => o.setName("name").setDescription("Item name").setRequired(true))
        .addIntegerOption((o) => o.setName("price").setDescription("Price in coins").setRequired(true).setMinValue(1))
        .addRoleOption((o) => o.setName("role").setDescription("Role granted when bought (optional)"))
        .addStringOption((o) => o.setName("description").setDescription("Short description"))
    )
    .addSubcommand((sc) =>
      sc
        .setName("removeitem")
        .setDescription("(Admin) Remove an item from the shop")
        .addIntegerOption((o) => o.setName("id").setDescription("Item ID").setRequired(true).setMinValue(1))
    ),

  async execute(interaction: ChatInputCommandInteraction) {
    const sub = interaction.options.getSubcommand();
    const guildId = interaction.guild!.id;

    if (sub === "view") {
      const items = getShopItems(guildId);
      if (items.length === 0) {
        await interaction.reply({ embeds: [errorEmbed("The shop is empty. Admins can add items with `/shop additem`.")], ephemeral: true });
        return;
      }
      const lines = items.map(
        (i) => `**#${i.id} · ${i.name}** — ${i.price.toLocaleString()} ${CURRENCY}${i.role_id ? ` 🎁 <@&${i.role_id}>` : ""}${i.description ? `\n   ${i.description}` : ""}`
      );
      await interaction.reply({
        embeds: [brandEmbed().setTitle("🛒 Server Shop").setDescription(lines.join("\n\n")).setFooter({ text: "Buy with /shop buy id:<ID>" })],
      });
      return;
    }

    if (sub === "buy") {
      const id = interaction.options.getInteger("id", true);
      const item = getShopItem(guildId, id);
      if (!item) {
        await interaction.reply({ embeds: [errorEmbed("No item with that ID. Check `/shop view`.")], ephemeral: true });
        return;
      }
      if (spend(guildId, interaction.user.id, item.price) === null) {
        await interaction.reply({ embeds: [errorEmbed(`You can't afford **${item.name}** (${item.price.toLocaleString()} ${CURRENCY}).`)], ephemeral: true });
        return;
      }
      addToInventory(guildId, interaction.user.id, item.id);

      let roleNote = "";
      if (item.role_id) {
        const role = interaction.guild!.roles.cache.get(item.role_id);
        const member = interaction.member as GuildMember;
        if (role && member) {
          await member.roles.add(role).catch(() => {});
          roleNote = ` You received the **${role.name}** role!`;
        }
      }
      await interaction.reply({ embeds: [successEmbed(`You bought **${item.name}** for ${item.price.toLocaleString()} ${CURRENCY}.${roleNote}`)] });
      await refreshBaltopPanel(interaction.guild!);
      return;
    }

    // Admin-only subcommands.
    const member = interaction.member as GuildMember;
    if (!member.permissions.has(PermissionFlagsBits.ManageGuild)) {
      await interaction.reply({ embeds: [errorEmbed("You need the Manage Server permission to do that.")], ephemeral: true });
      return;
    }

    if (sub === "additem") {
      const name = interaction.options.getString("name", true);
      const price = interaction.options.getInteger("price", true);
      const role = interaction.options.getRole("role");
      const description = interaction.options.getString("description");
      const id = addShopItem(guildId, name, price, role?.id ?? null, description);
      await interaction.reply({ embeds: [successEmbed(`Added **${name}** to the shop as item **#${id}**.`)] });
      return;
    }

    if (sub === "removeitem") {
      const id = interaction.options.getInteger("id", true);
      const result = removeShopItem(guildId, id);
      if (result.changes === 0) {
        await interaction.reply({ embeds: [errorEmbed("No item with that ID.")], ephemeral: true });
        return;
      }
      await interaction.reply({ embeds: [successEmbed(`Removed item **#${id}** from the shop.`)] });
    }
  },
};

export default command;
