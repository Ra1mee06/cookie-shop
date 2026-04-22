import { spawn } from "node:child_process";

const commands = [
  { name: "catalog", script: "dev:catalog" },
  { name: "orders", script: "dev:orders" },
  { name: "worker", script: "dev:worker" },
  { name: "gateway", script: "dev:gateway" },
];

const children = [];

function prefixOutput(stream, label, colorCode) {
  let buffer = "";
  stream.on("data", (chunk) => {
    buffer += chunk.toString();
    const lines = buffer.split(/\r?\n/);
    buffer = lines.pop() ?? "";
    for (const line of lines) {
      if (line.length === 0) continue;
      process.stdout.write(`\x1b[${colorCode}m[${label}]\x1b[0m ${line}\n`);
    }
  });
}

commands.forEach((cmd, index) => {
  const color = 32 + (index % 5);
  const child = spawn("npm", ["run", cmd.script], {
    shell: true,
    stdio: ["ignore", "pipe", "pipe"],
    env: process.env,
  });

  prefixOutput(child.stdout, cmd.name, color);
  prefixOutput(child.stderr, `${cmd.name}:err`, 31);

  child.on("exit", (code) => {
    process.stdout.write(`[${cmd.name}] exited with code ${code ?? "null"}\n`);
  });

  children.push(child);
});

function shutdown(signal) {
  process.stdout.write(`\nStopping services (${signal})...\n`);
  for (const child of children) {
    if (!child.killed) child.kill("SIGTERM");
  }
  setTimeout(() => process.exit(0), 500);
}

process.on("SIGINT", () => shutdown("SIGINT"));
process.on("SIGTERM", () => shutdown("SIGTERM"));
