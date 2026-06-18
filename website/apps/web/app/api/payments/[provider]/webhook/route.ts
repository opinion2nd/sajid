import { NextResponse } from "next/server";
import { getPaymentProvider } from "@brothercraft/payments";
import { fulfillOrder } from "@/lib/orders";

export const runtime = "nodejs";
export const dynamic = "force-dynamic";

// Server-to-server IPN. The provider verifies the payment (signature / val_id)
// before we ever fulfil the order. Client redirects are never trusted here.
export async function POST(req: Request) {
  const contentType = req.headers.get("content-type") ?? "";
  let payload: Record<string, string> = {};
  if (contentType.includes("application/json")) {
    payload = await req.json();
  } else {
    const form = await req.formData();
    for (const [k, v] of form.entries()) payload[k] = String(v);
  }

  const provider = getPaymentProvider();
  const result = await provider.verifyWebhook(payload);
  if (!result.ok || !result.orderId || !result.providerRef) {
    return NextResponse.json(
      { error: result.reason ?? "verification failed" },
      { status: 400 }
    );
  }

  await fulfillOrder(result.orderId, result.providerRef);
  return NextResponse.json({ ok: true });
}
