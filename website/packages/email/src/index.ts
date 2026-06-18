// Transactional email. Uses Resend when EMAIL_API_KEY is set; otherwise logs to
// the console so the app is fully functional in demo mode without credentials.

export type SendEmailInput = {
  to: string;
  subject: string;
  html: string;
};

const FROM = process.env.EMAIL_FROM ?? "Brother Craft <no-reply@brothercraft.dev>";

export async function sendEmail(input: SendEmailInput): Promise<void> {
  const apiKey = process.env.EMAIL_API_KEY;

  if (!apiKey) {
    console.log(
      `[email:console] To: ${input.to} | Subject: ${input.subject}`
    );
    return;
  }

  try {
    const res = await fetch("https://api.resend.com/emails", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${apiKey}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        from: FROM,
        to: input.to,
        subject: input.subject,
        html: input.html,
      }),
    });
    if (!res.ok) {
      console.error("[email] send failed", res.status, await res.text());
    }
  } catch (e) {
    console.error("[email] send error", e);
  }
}

function layout(title: string, body: string): string {
  return `<div style="font-family:sans-serif;max-width:560px;margin:0 auto;background:#10151d;color:#e7ebf2;border-radius:12px;padding:24px">
    <h1 style="color:#43b581;margin:0 0 4px">Brother Craft</h1>
    <h2 style="margin:12px 0">${title}</h2>
    ${body}
    <hr style="border:none;border-top:1px solid #232c3a;margin:20px 0"/>
    <p style="color:#93a0b3;font-size:12px">The Minecraft marketplace · Payments via bKash &amp; Nagad</p>
  </div>`;
}

export function receiptEmail(params: {
  buyerName: string;
  items: { title: string; priceLabel: string }[];
  totalLabel: string;
  licenses: { title: string; key: string }[];
}): { subject: string; html: string } {
  const rows = params.items
    .map(
      (i) =>
        `<tr><td style="padding:6px 0">${i.title}</td><td style="text-align:right">${i.priceLabel}</td></tr>`
    )
    .join("");
  const keys = params.licenses.length
    ? `<h3>Your license keys</h3>` +
      params.licenses
        .map(
          (l) =>
            `<p style="margin:4px 0">${l.title}: <code style="background:#161c26;padding:2px 6px;border-radius:4px">${l.key}</code></p>`
        )
        .join("")
    : "";
  return {
    subject: "Your Brother Craft purchase",
    html: layout(
      `Thanks for your purchase, ${params.buyerName}!`,
      `<table style="width:100%;border-collapse:collapse">${rows}
        <tr><td style="padding-top:10px;font-weight:bold;border-top:1px solid #232c3a">Total</td>
        <td style="padding-top:10px;text-align:right;font-weight:bold;border-top:1px solid #232c3a">${params.totalLabel}</td></tr>
      </table>${keys}
      <p style="margin-top:16px">Download anytime from your dashboard.</p>`
    ),
  };
}

export function payoutEmail(params: {
  sellerName: string;
  amountLabel: string;
  method: string;
}): { subject: string; html: string } {
  return {
    subject: "Payout requested",
    html: layout(
      "Payout requested",
      `<p>Hi ${params.sellerName}, your payout of <strong>${params.amountLabel}</strong> via ${params.method} has been requested and is being processed.</p>`
    ),
  };
}
