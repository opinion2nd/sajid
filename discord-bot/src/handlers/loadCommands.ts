import { Collection } from "discord.js";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";
import type { Command } from "../types.js";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export async function loadCommands(): Promise<Collection<string, Command>> {
  const commands = new Collection<string, Command>();
  const commandsDir = path.join(__dirname, "..", "commands");

  for (const category of fs.readdirSync(commandsDir)) {
    const categoryPath = path.join(commandsDir, category);
    if (!fs.statSync(categoryPath).isDirectory()) continue;

    for (const file of fs.readdirSync(categoryPath)) {
      if (!file.endsWith(".ts") && !file.endsWith(".js")) continue;
      const mod = await import(pathToFileURL(path.join(categoryPath, file)).href);
      const command: Command = mod.default;
      commands.set(command.data.name, command);
    }
  }

  return commands;
}
