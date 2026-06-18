import prisma from "@brothercraft/db";
import { requireUser } from "@/lib/session";
import { openDispute } from "@/lib/actions";
import { formatPrice, timeAgo } from "@/lib/utils";
import { StatusPill, EmptyState } from "@/components/ui";

export default async function OrdersPage() {
  const user = await requireUser();
  const orders = await prisma.order.findMany({
    where: { buyerId: user.id },
    include: { items: { include: { product: true } }, dispute: true },
    orderBy: { createdAt: "desc" },
  });

  return (
    <div>
      <h1 className="text-2xl font-bold">Orders</h1>
      <div className="mt-6 space-y-3">
        {orders.length === 0 && (
          <EmptyState title="No orders yet" hint="Your purchases will appear here." />
        )}
        {orders.map((o) => (
          <div key={o.id} className="card p-4">
            <div className="flex flex-wrap items-center justify-between gap-2 border-b border-border pb-3">
              <div className="flex items-center gap-3">
                <span className="font-mono text-sm text-muted">
                  #{o.id.slice(0, 8)}
                </span>
                <StatusPill status={o.status} />
              </div>
              <span className="text-sm text-muted">{timeAgo(o.createdAt)}</span>
            </div>
            <ul className="mt-3 space-y-1">
              {o.items.map((i) => (
                <li key={i.id} className="flex justify-between text-sm">
                  <span>{i.product.title}</span>
                  <span>{formatPrice(i.unitPriceCents)}</span>
                </li>
              ))}
            </ul>
            <div className="mt-3 flex justify-between border-t border-border pt-3 font-semibold">
              <span>Total</span>
              <span>{formatPrice(o.totalCents)}</span>
            </div>

            {/* Dispute / refund */}
            {o.dispute ? (
              <p className="mt-3 rounded-lg border border-yellow-500/30 bg-yellow-500/10 px-3 py-2 text-sm text-yellow-300">
                Dispute {o.dispute.status.toLowerCase()}
                {o.dispute.resolution ? ` — ${o.dispute.resolution}` : ""}
              </p>
            ) : (
              o.status === "COMPLETED" && (
                <form
                  action={openDispute}
                  className="mt-3 flex flex-col gap-2 border-t border-border pt-3 sm:flex-row"
                >
                  <input type="hidden" name="orderId" value={o.id} />
                  <input
                    name="reason"
                    placeholder="Describe the problem to request a refund…"
                    className="input flex-1"
                  />
                  <button className="btn-ghost">Report a problem</button>
                </form>
              )
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
