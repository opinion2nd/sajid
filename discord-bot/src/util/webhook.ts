interface WebhookPayload {
  content?: string;
  embeds?: unknown[];
}

export async function postWebhook(url: string | undefined, payload: WebhookPayload) {
  if (!url) return;
  try {
    await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
  } catch (error) {
    console.error("[webhook] Failed to post:", error);
  }
}

/** Shows only the first block of a license key, e.g. "AB3F-****-****-****". */
export function maskLicenseKey(key: string): string {
  const [first, ...rest] = key.split("-");
  return [first, ...rest.map((block) => "*".repeat(block.length))].join("-");
}
