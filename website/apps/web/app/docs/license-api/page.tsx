import type { Metadata } from "next";

export const metadata: Metadata = { title: "License API" };

export default function LicenseApiDocs() {
  return (
    <div className="container-page max-w-3xl py-12">
      <h1 className="text-3xl font-bold">License Validation API</h1>
      <p className="mt-2 text-muted">
        License-gated products mint a key per purchase. Your Minecraft plugin
        validates that key against this endpoint. The contract is compatible with
        the existing AntiFreecam client — no plugin changes required.
      </p>

      <h2 className="mt-8 text-xl font-bold">Endpoint</h2>
      <pre className="card mt-2 overflow-x-auto p-4 font-mono text-sm">
{`POST /api/v1/license/validate
Content-Type: application/json

{
  "key": "PIL-XXXX-XXXX-XXXX-XXXX",
  "serverId": "<persistent server UUID>",
  "pluginVersion": "1.0.0"
}`}
      </pre>

      <h2 className="mt-8 text-xl font-bold">Response</h2>
      <pre className="card mt-2 overflow-x-auto p-4 font-mono text-sm">
{`200 OK
{
  "status": "VALID | INVALID | MISMATCH | EXPIRED",
  "message": "human readable",
  "expiresAt": "ISO-8601 | null",
  "boundServerId": "<uuid> | null"
}`}
      </pre>

      <h2 className="mt-8 text-xl font-bold">Status meanings</h2>
      <ul className="prose-mc mt-2">
        <li><strong>VALID</strong> — key is active and bound to this server.</li>
        <li><strong>MISMATCH</strong> — activation limit reached on other servers.</li>
        <li><strong>EXPIRED</strong> — key passed its expiry date.</li>
        <li><strong>INVALID</strong> — key not found or revoked.</li>
      </ul>

      <h2 className="mt-8 text-xl font-bold">Behaviour</h2>
      <p className="prose-mc mt-2">
        Keys bind on first use. A key may activate on up to{" "}
        <code>maxActivations</code> servers (configurable per product). Persist a
        random UUID per server and send it as <code>serverId</code>. Validate on
        startup and periodically; disable features on a non-<code>VALID</code>{" "}
        status.
      </p>
    </div>
  );
}
