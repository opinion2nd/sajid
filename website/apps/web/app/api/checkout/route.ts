import { NextResponse } from "next/server";
import { z } from "zod";
import { getPaymentProvider } from "@brothercraft/payments";
import { getCurrentUser } from "@/lib/session";
import { createOrder } from "@/lib/orders";

const schema = z.object({
  productIds: z.array(z.string()).min(1),
});

export async function POST(req: Request) {
  const user = await getCurrentUser();
  if (!user) {
    return NextResponse.json({ error: "Not signed in" }, { status: 401 });
  }

  let body;
  try {
    body = schema.parse(await req.json());
  } catch {
    return NextResponse.json({ error: "Invalid cart" }, { status: 400 });
  }

  const provider = getPaymentProvider();
  const order = await createOrder(
    user.id,
    body.productIds.map((productId) => ({ productId })),
    provider.id
  );

  const appUrl = process.env.APP_URL ?? "http://localhost:3000";
  const session = await provider.createSession({
    orderId: order.id,
    amountCents: order.totalCents,
    currency: order.currency,
    customerEmail: user.email ?? "buyer@brothercraft.dev",
    successUrl: `${appUrl}/checkout/success`,
    cancelUrl: `${appUrl}/cart?canceled=1`,
    ipnUrl: `${appUrl}/api/payments/${provider.id.toLowerCase()}/webhook`,
  });

  return NextResponse.json({ redirectUrl: session.redirectUrl });
}
