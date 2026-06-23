// Assembles a plain (non-obfuscated) buyer-ready folder straight from source.
// Output: discord-bot/plain-sell-package/ — readable TypeScript source, same setup as dev.
import { cpSync, mkdirSync, rmSync, writeFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.join(__dirname, "..");
// Built outside `root` — cpSync refuses to copy a directory into its own subdirectory.
const out = process.argv[2] ?? path.join(root, "..", "plain-sell-package");

const EXCLUDE = new Set([
  "node_modules",
  "data",
  ".env",
  ".env.local",
  ".next",
  "dist",
  "dist-obf",
  "sell-package",
  "plain-sell-package",
  "tsconfig.tsbuildinfo",
  "obfuscator.config.json",
  "tsconfig.build.json",
  "scripts",
]);

rmSync(out, { recursive: true, force: true });
mkdirSync(out, { recursive: true });

cpSync(root, out, {
  recursive: true,
  filter: (src) => !EXCLUDE.has(path.basename(src)),
});

// Blank templates, not the seller's real filled-in secrets.
cpSync(path.join(root, ".env.example"), path.join(out, ".env"));
cpSync(path.join(root, "dashboard", ".env.example"), path.join(out, "dashboard", ".env.local"));
mkdirSync(path.join(out, "data"), { recursive: true });
writeFileSync(path.join(out, "data", ".gitkeep"), "");

console.log(`Plain sell package written to ${out}`);
