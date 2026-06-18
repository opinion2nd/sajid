import Link from "next/link";
import { CheckCircle2, KeyRound, Download } from "lucide-react";
import prisma from "@brothercraft/db";
import { requireUser } from "@/lib/session";
import { fulfillOrder } from "@/lib/orders";
import { formatPrice } from "@/lib/utils";
import { ClearCart } from "@/components/clear-cart";

export const dynamic = "force-dynamic";

export default async function CheckoutSuccessPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | undefined>>;
}) {
  const user = await requireUser();
  const sp = await searchParams;

  // Mock provider fulfils on the success redirect (no real IPN). Idempotent.
  if (sp.orderId && sp.providerRef) {
    try {
      await fulfillOrder(sp.orderId, sp.providerRef);
    } catch {
      /* already processed or invalid — fall through to display */
    }
  }

  const order = sp.orderId
    ? await prisma.order.findFirst({
        where: { id: sp.orderId, buyerId: user.id },
        include: {
          items: { include: { product: true } },
          licenses: { include: { product: true } },
        },
      })
    : null;

  return (
    <div className="container-page flex justify-center py-12">
      <ClearCart />
      <div className="w-full max-w-2xl">
        <div className="card flex flex-col items-center p-8 text-center">
          <CheckCircle2 className="h-14 w-14 text-accent" />
          <h1 className="mt-4 text-2xl font-bold">Payment successful</h1>
          <p className="mt-1 text-muted">
            Thank you for your purchase. Your downloads and license keys are
            ready.
          </p>
        </div>

        {order && (
          <div className="card mt-4 p-6">
            <div className="flex items-center justify-between border-b border-border pb-3">
              <span className="text-sm text-muted">Order</span>
              <span className="font-mono text-sm">{order.id.slice(0, 12)}</span>
            </div>
            <ul className="divide-y divide-border">
              {order.items.map((item) => (
                <li
                  key={item.id}
                  className="flex items-center justify-between py-3"
                >
                  <span className="font-medium">{item.product.title}</span>
                  <span>{formatPrice(item.unitPriceCents)}</span>
                </li>
              ))}
            </ul>

            {order.licenses.length > 0 && (
              <div className="mt-4 rounded-lg border border-accent/30 bg-accent/5 p-4">
                <h3 className="flex items-center gap-2 text-sm font-semibold text-accent">
                  <KeyRound className="h-4 w-4" /> Your license keys
                </h3>
                <ul className="mt-2 space-y-2">
                  {order.licenses.map((l) => (
                    <li
                      key={l.id}
                      className="flex flex-col gap-1 text-sm sm:flex-row sm:items-center sm:justify-between"
                    >
                      <span className="text-muted">{l.product.title}</span>
                      <code className="rounded bg-surface px-2 py-1 font-mono">
                        {l.key}
                      </code>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            <div className="mt-5 flex flex-col gap-2 sm:flex-row">
              <Link href="/dashboard/downloads" className="btn-primary flex-1">
                <Download className="h-4 w-4" /> Go to downloads
              </Link>
              <Link href="/browse" className="btn-ghost flex-1">
                Keep browsing
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
