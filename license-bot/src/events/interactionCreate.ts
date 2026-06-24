import { Events, type Interaction } from "discord.js";
import { errorEmbed } from "../util/embeds.js";

export const name = Events.InteractionCreate;

export async function execute(interaction: Interaction) {
  if (interaction.isAutocomplete()) {
    const command = interaction.client.commands.get(interaction.commandName);
    if (!command?.autocomplete) return;
    try {
      await command.autocomplete(interaction);
    } catch (error) {
      console.error(`Error handling autocomplete for /${interaction.commandName}:`, error);
    }
    return;
  }

  if (interaction.isChatInputCommand()) {
    const command = interaction.client.commands.get(interaction.commandName);
    if (!command) return;
    try {
      await command.execute(interaction);
    } catch (error) {
      console.error(`Error executing /${interaction.commandName}:`, error);
      const payload = { embeds: [errorEmbed("Something went wrong while running that command.")], ephemeral: true };
      if (interaction.replied || interaction.deferred) {
        await interaction.followUp(payload).catch(() => {});
      } else {
        await interaction.reply(payload).catch(() => {});
      }
    }
  }
}
