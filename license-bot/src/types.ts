import type { ChatInputCommandInteraction, Collection } from "discord.js";

export interface CommandData {
  name: string;
  toJSON(): unknown;
}

export interface Command {
  data: CommandData;
  execute(interaction: ChatInputCommandInteraction): Promise<void>;
}

declare module "discord.js" {
  interface Client {
    commands: Collection<string, Command>;
  }
}
