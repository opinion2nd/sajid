// Assembles a buyer-ready folder from the obfuscated build (run after `npm run build` + `npm run obfuscate`).
// Output: discord-bot/sell-package/ — obfuscated bot, trimmed package.json, env template, buyer README, dashboard (source, not obfuscated).
import { cpSync, existsSync, mkdirSync, readFileSync, rmSync, writeFileSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.join(__dirname, "..");
const distObf = path.join(root, "dist-obf");
const out = path.join(root, "sell-package");

if (!existsSync(distObf)) {
  console.error("dist-obf/ not found — run `npm run build` and `npm run obfuscate` first.");
  process.exit(1);
}

rmSync(out, { recursive: true, force: true });
mkdirSync(out, { recursive: true });

cpSync(distObf, out, { recursive: true });

const sourcePkg = JSON.parse(readFileSync(path.join(root, "package.json"), "utf8"));
const sellPkg = {
  name: sourcePkg.name,
  version: sourcePkg.version,
  private: true,
  type: "module",
  scripts: {
    start: "node index.js",
    "deploy-commands": "node deploy-commands.js",
  },
  dependencies: sourcePkg.dependencies,
  allowScripts: sourcePkg.allowScripts,
};
writeFileSync(path.join(out, "package.json"), JSON.stringify(sellPkg, null, 2) + "\n");

cpSync(path.join(root, ".env.example"), path.join(out, ".env.example"));
mkdirSync(path.join(out, "data"), { recursive: true });
writeFileSync(path.join(out, "data", ".gitkeep"), "");

// launcher.js for fixed-Main-File Pterodactyl eggs — same two-process model as the dev version,
// but runs the compiled/obfuscated JS directly instead of going through tsx.
const launcher = `// Entry point for Pterodactyl Node.js eggs whose fixed startup command only
// supports \`node <MAIN_FILE>\` (no custom shell command field). Set the
// panel's "Main File" variable to "index.js" and leave "Node Packages" empty.
// Spawns the bot and the web dashboard as two child processes.
import { spawn } from "node:child_process";
import { existsSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const dashboardDir = path.join(__dirname, "dashboard");
const node = process.execPath;

function findNpmCli() {
  const binDir = path.dirname(node);
  const candidates = [
    path.join(binDir, "..", "lib", "node_modules", "npm", "bin", "npm-cli.js"),
    path.join(binDir, "..", "lib64", "node_modules", "npm", "bin", "npm-cli.js"),
    path.join(binDir, "node_modules", "npm", "bin", "npm-cli.js"),
  ];
  const found = candidates.find(existsSync);
  if (!found) throw new Error(\`Could not locate npm-cli.js near \${node}\`);
  return found;
}

function run(args, cwd) {
  return new Promise((resolve, reject) => {
    const proc = spawn(node, args, { cwd, stdio: "inherit" });
    proc.on("exit", (code) => (code === 0 ? resolve() : reject(new Error(\`node \${args.join(" ")} exited with \${code}\`))));
  });
}

function spawnLong(args, cwd) {
  const proc = spawn(node, args, { cwd, stdio: "inherit" });
  proc.on("exit", (code) => console.error(\`[launcher] node \${args.join(" ")} exited with code \${code}\`));
  return proc;
}

async function main() {
  const npmCli = findNpmCli();

  if (!existsSync(path.join(__dirname, "node_modules"))) {
    await run([npmCli, "install"], __dirname);
  }
  if (!existsSync(path.join(dashboardDir, "node_modules"))) {
    await run([npmCli, "install"], dashboardDir);
  }

  const nextCli = path.join(dashboardDir, "node_modules", "next", "dist", "bin", "next");

  await run(["deploy-commands.js"], __dirname).catch((err) =>
    console.error("[launcher] deploy-commands failed (continuing anyway):", err.message)
  );

  spawnLong(["index.js"], __dirname);

  if (!existsSync(path.join(dashboardDir, ".next"))) {
    await run([nextCli, "build"], dashboardDir);
  }

  const port = process.env.SERVER_PORT || "3000";
  spawnLong([nextCli, "start", "-p", port, "-H", "0.0.0.0"], dashboardDir);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
`;
writeFileSync(path.join(out, "launcher.js"), launcher);

cpSync(path.join(root, "pterodactyl-start.sh"), path.join(out, "pterodactyl-start.sh"));

cpSync(path.join(root, "dashboard"), path.join(out, "dashboard"), {
  recursive: true,
  filter: (src) => !["node_modules", ".next", "tsconfig.tsbuildinfo"].includes(path.basename(src)),
});

writeFileSync(
  path.join(out, "README.md"),
  `# Brother Craft Bot — Setup

This is the compiled, licensed build. The Discord bot's code is precompiled and
obfuscated (no editable TypeScript source) per the license terms of your purchase —
this license covers usage on **1 Discord server**.

## Setup

1. \`npm install\`
2. Copy \`.env.example\` to \`.env\` and fill in \`DISCORD_BOT_TOKEN\` and \`DISCORD_CLIENT_ID\`
   (from the [Discord Developer Portal](https://discord.com/developers/applications)).
3. Register slash commands: \`npm run deploy-commands\`
4. Start the bot: \`npm start\`

## Web Dashboard

See \`dashboard/README.md\`. The dashboard runs as a normal Next.js production build
(\`npm install\` then \`npm run build\` then \`npm start\` inside \`dashboard/\`).

## Pterodactyl / NyctoHost

See \`pterodactyl-start.sh\` (shell-capable eggs) or \`launcher.js\` (fixed Main File eggs) —
same instructions as the standard setup guide, just pointed at this compiled build.
`,
);

console.log(`Sell package written to ${out}`);
