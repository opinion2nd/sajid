// Entry point for Pterodactyl Node.js eggs whose fixed startup command only
// supports `node <MAIN_FILE>` (no custom shell command field). Set the
// panel's "Main File" variable to "discord-bot/launcher.js" (or just
// "launcher.js" if this folder's contents are the container root) and leave
// "Node Packages" empty. This spawns the bot and the web dashboard as two
// child processes under one Node entry point.
import { spawn } from "node:child_process";
import { existsSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const dashboardDir = path.join(__dirname, "dashboard");

function run(cmd, args, cwd) {
  return new Promise((resolve, reject) => {
    const proc = spawn(cmd, args, { cwd, stdio: "inherit", shell: true });
    proc.on("exit", (code) => (code === 0 ? resolve() : reject(new Error(`${cmd} ${args.join(" ")} exited with ${code}`))));
  });
}

function spawnLong(cmd, args, cwd) {
  const proc = spawn(cmd, args, { cwd, stdio: "inherit", shell: true });
  proc.on("exit", (code) => console.error(`[launcher] ${cmd} ${args.join(" ")} exited with code ${code}`));
  return proc;
}

async function main() {
  if (!existsSync(path.join(__dirname, "node_modules"))) {
    await run("npm", ["install"], __dirname);
  }
  if (!existsSync(path.join(dashboardDir, "node_modules"))) {
    await run("npm", ["install"], dashboardDir);
  }

  await run("npm", ["run", "deploy-commands"], __dirname).catch((err) =>
    console.error("[launcher] deploy-commands failed (continuing anyway):", err.message)
  );

  spawnLong("npm", ["start"], __dirname);

  if (!existsSync(path.join(dashboardDir, ".next"))) {
    await run("npm", ["run", "build"], dashboardDir);
  }

  const port = process.env.SERVER_PORT || "3000";
  spawnLong("npx", ["next", "start", "-p", port, "-H", "0.0.0.0"], dashboardDir);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
