// Entry point for Pterodactyl Node.js eggs whose fixed startup command only
// supports `node <MAIN_FILE>` (no custom shell command field). Set the
// panel's "Main File" variable to "discord-bot/launcher.js" (or just
// "launcher.js" if this folder's contents are the container root) and leave
// "Node Packages" empty. This spawns the bot and the web dashboard as two
// child processes under one Node entry point.
//
// Everything below spawns `process.execPath` (the same `node` binary this
// file is already running under) directly on a target JS file, instead of
// going through the `npm`/`npx` shell-script wrappers or `/bin/sh -c ...`.
// Some restrictive containers don't have a usable shell or fail to resolve
// `npm` via PATH; calling `node <file.js>` directly avoids both wrappers.
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
  if (!found) throw new Error(`Could not locate npm-cli.js near ${node}`);
  return found;
}

function run(args, cwd) {
  return new Promise((resolve, reject) => {
    const proc = spawn(node, args, { cwd, stdio: "inherit" });
    proc.on("exit", (code) => (code === 0 ? resolve() : reject(new Error(`node ${args.join(" ")} exited with ${code}`))));
  });
}

function spawnLong(args, cwd) {
  const proc = spawn(node, args, { cwd, stdio: "inherit" });
  proc.on("exit", (code) => console.error(`[launcher] node ${args.join(" ")} exited with code ${code}`));
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

  const tsxCli = path.join(__dirname, "node_modules", "tsx", "dist", "cli.mjs");
  const nextCli = path.join(dashboardDir, "node_modules", "next", "dist", "bin", "next");

  await run([tsxCli, "src/deploy-commands.ts"], __dirname).catch((err) =>
    console.error("[launcher] deploy-commands failed (continuing anyway):", err.message)
  );

  spawnLong([tsxCli, "src/index.ts"], __dirname);

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
