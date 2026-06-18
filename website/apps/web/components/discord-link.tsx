"use client";

import { useState } from "react";
import { Loader2, Copy, Check } from "lucide-react";

export function DiscordLinkCode() {
  const [code, setCode] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [copied, setCopied] = useState(false);

  async function generate() {
    setLoading(true);
    const res = await fetch("/api/discord/code", { method: "POST" });
    const data = await res.json();
    setCode(data.code ?? null);
    setLoading(false);
  }

  return (
    <div className="space-y-3">
      {code ? (
        <div className="rounded-lg border border-accent/30 bg-accent/5 p-4">
          <p className="text-sm text-muted">In Discord, run:</p>
          <div className="mt-2 flex items-center gap-2">
            <code className="rounded bg-surface px-3 py-2 font-mono text-lg">
              /link {code}
            </code>
            <button
              onClick={() => {
                navigator.clipboard.writeText(`/link ${code}`);
                setCopied(true);
                setTimeout(() => setCopied(false), 1500);
              }}
              className="rounded-lg border border-border bg-surface p-2 text-muted hover:text-text"
            >
              {copied ? (
                <Check className="h-4 w-4 text-accent" />
              ) : (
                <Copy className="h-4 w-4" />
              )}
            </button>
          </div>
          <p className="mt-2 text-xs text-muted">Expires in 10 minutes.</p>
        </div>
      ) : (
        <button onClick={generate} disabled={loading} className="btn-brand">
          {loading && <Loader2 className="h-4 w-4 animate-spin" />}
          Generate link code
        </button>
      )}
    </div>
  );
}
