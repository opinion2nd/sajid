import type { ChatInputCommandInteraction, AutocompleteInteraction, Collection } from "discord.js";

export interface CommandData {
  name: string;
  toJSON(): unknown;
}

export interface Command {
  data: CommandData;
  execute(interaction: ChatInputCommandInteraction): Promise<void>;
  autocomplete?(interaction: AutocompleteInteraction): Promise<void>;
}

declare module "discord.js" {
  interface Client {
    commands: Collection<string, Command>;
  }
}
