import type { Client } from "discord.js";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export async function loadEvents(client: Client) {
  const eventsDir = path.join(__dirname, "..", "events");

  for (const file of fs.readdirSync(eventsDir)) {
    if (!file.endsWith(".ts") && !file.endsWith(".js")) continue;
    const mod = await import(pathToFileURL(path.join(eventsDir, file)).href);
    if (mod.once) {
      client.once(mod.name, mod.execute);
    } else {
      client.on(mod.name, mod.execute);
    }
  }
}
