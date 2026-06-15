#!/usr/bin/env node
/**
 * AntiFreeam Key Generator CLI
 * Usage: afc-keygen <command> [options]
 *
 * Commands:
 *   generate  [--count N] [--notes "text"] [--expires YYYY-MM-DD]
 *   revoke    --key AFC-XXXX-XXXX-XXXX-XXXX
 *   unbind    --key AFC-XXXX-XXXX-XXXX-XXXX
 *   list
 *
 * Environment:
 *   LICENSE_SERVER_URL   e.g. http://localhost:3000
 *   ADMIN_SECRET         Admin secret for the license server
 */

const SERVER_URL = (process.env.LICENSE_SERVER_URL ?? '').replace(/\/$/, '');
const ADMIN_SECRET = process.env.ADMIN_SECRET ?? '';

async function apiCall(method: string, path: string, body?: object): Promise<object> {
  if (!SERVER_URL) {
    console.error('ERROR: Set LICENSE_SERVER_URL environment variable.');
    process.exit(1);
  }
  if (!ADMIN_SECRET) {
    console.error('ERROR: Set ADMIN_SECRET environment variable.');
    process.exit(1);
  }

  const res = await fetch(`${SERVER_URL}${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      'X-Admin-Secret': ADMIN_SECRET
    },
    body: body ? JSON.stringify(body) : undefined
  });

  const data = await res.json() as object;
  if (res.status >= 400) {
    console.error('API Error:', JSON.stringify(data, null, 2));
    process.exit(1);
  }
  return data;
}

function parseArgs(args: string[]): Record<string, string> {
  const result: Record<string, string> = {};
  for (let i = 0; i < args.length; i++) {
    if (args[i].startsWith('--') && i + 1 < args.length) {
      result[args[i].slice(2)] = args[i + 1];
      i++;
    }
  }
  return result;
}

async function cmdGenerate(opts: Record<string, string>) {
  const count = parseInt(opts['count'] ?? '1', 10);
  const notes = opts['notes'];
  const expiresAt = opts['expires'];

  console.log(`Generating ${count} license key(s)...`);
  for (let i = 0; i < count; i++) {
    const data = await apiCall('POST', '/api/v1/admin/generate', {
      notes: notes ?? `Generated on ${new Date().toISOString()}`,
      expiresAt: expiresAt ?? null
    }) as { key: string };
    console.log(data.key);
  }
}

async function cmdRevoke(opts: Record<string, string>) {
  const key = opts['key'];
  if (!key) { console.error('--key is required'); process.exit(1); }
  await apiCall('POST', '/api/v1/admin/revoke', { key });
  console.log(`Revoked: ${key}`);
}

async function cmdUnbind(opts: Record<string, string>) {
  const key = opts['key'];
  if (!key) { console.error('--key is required'); process.exit(1); }
  await apiCall('POST', '/api/v1/admin/unbind', { key });
  console.log(`Unbound: ${key}`);
}

async function cmdList() {
  const data = await apiCall('GET', '/api/v1/admin/licenses') as object[];
  if (!Array.isArray(data) || data.length === 0) {
    console.log('No licenses found.');
    return;
  }
  console.log('\nLicenses:');
  console.log('-'.repeat(80));
  for (const lic of data as Array<Record<string,string|null>>) {
    const status = lic['revokedAt'] ? 'REVOKED' :
                   (lic['expiresAt'] && new Date(lic['expiresAt']) < new Date()) ? 'EXPIRED' : 'ACTIVE';
    console.log(`${lic['key']}  [${status}]`);
    console.log(`  Server: ${lic['serverId'] ?? 'unbound'}  Notes: ${lic['notes'] ?? '-'}`);
    console.log(`  Created: ${lic['createdAt']}  Expires: ${lic['expiresAt'] ?? 'never'}`);
    console.log('');
  }
}

async function main() {
  const [,, command, ...rest] = process.argv;
  const opts = parseArgs(rest);

  switch (command) {
    case 'generate': await cmdGenerate(opts); break;
    case 'revoke':   await cmdRevoke(opts); break;
    case 'unbind':   await cmdUnbind(opts); break;
    case 'list':     await cmdList(); break;
    default:
      console.log('AntiFreeam Key Generator CLI');
      console.log('');
      console.log('Usage: afc-keygen <command> [options]');
      console.log('');
      console.log('Commands:');
      console.log('  generate [--count N] [--notes "text"] [--expires YYYY-MM-DD]');
      console.log('  revoke   --key AFC-XXXX-XXXX-XXXX-XXXX');
      console.log('  unbind   --key AFC-XXXX-XXXX-XXXX-XXXX');
      console.log('  list');
      console.log('');
      console.log('Environment:');
      console.log('  LICENSE_SERVER_URL   Base URL of the license server');
      console.log('  ADMIN_SECRET         Admin secret');
  }
}

main().catch(e => { console.error(e); process.exit(1); });
