import { redirect } from "next/navigation";
import prisma from "@brothercraft/db";
import { requireUser } from "@/lib/session";
import { formatPrice } from "@/lib/utils";
import { StatusPill } from "@/components/ui";
import { timeAgo } from "@/lib/utils";
import { getPlatformEarnings, getPlatformPayout } from "@/lib/platform";
import {
  approveSeller,
  setProductStatus,
  processPayout,
  resolveDispute,
  releaseDueEscrow,
} from "@/lib/actions";

async function releaseEscrowAction() {
  "use server";
  await releaseDueEscrow();
}

export const metadata = { title: "Admin" };

export default async function AdminPage() {
  const user = await requireUser();
  if (user.role !== "ADMIN") redirect("/dashboard");

  const [pendingSellers, pendingProducts, pendingPayouts, openDisputes, totals] =
    await Promise.all([
      prisma.sellerProfile.findMany({
        where: { status: "PENDING" },
        include: { user: { select: { email: true } } },
      }),
      prisma.product.findMany({
        where: { status: "PENDING_REVIEW" },
        include: { seller: { select: { displayName: true } } },
      }),
      prisma.payout.findMany({
        where: { status: "REQUESTED" },
        include: { seller: { select: { displayName: true } } },
        orderBy: { requestedAt: "desc" },
      }),
      prisma.dispute.findMany({
        where: { status: "OPEN" },
        include: { order: { select: { id: true, totalCents: true } } },
        orderBy: { createdAt: "desc" },
      }),
      Promise.all([
        prisma.user.count(),
        prisma.product.count({ where: { status: "PUBLISHED" } }),
        prisma.order.aggregate({
          where: { status: "COMPLETED" },
          _sum: { totalCents: true },
          _count: true,
        }),
      ]),
    ]);
  const [userCount, productCount, orderAgg] = totals;
  const [platformEarnings, payout] = await Promise.all([
    getPlatformEarnings(),
    Promise.resolve(getPlatformPayout()),
  ]);

  return (
    <div className="container-page py-8">
      <h1 className="text-2xl font-bold">Admin panel</h1>

      <div className="mt-6 grid gap-4 sm:grid-cols-4">
        <Stat label="Users" value={userCount} />
        <Stat label="Live products" value={productCount} />
        <Stat label="Orders" value={orderAgg._count} />
        <Stat label="GMV" value={formatPrice(orderAgg._sum.totalCents ?? 0)} />
      </div>

      {/* Platform earnings → owner's bKash/Nagad */}
      <div className="mt-4 card flex flex-wrap items-center justify-between gap-4 border-accent/30 bg-accent/5 p-5">
        <div>
          <p className="text-sm text-muted">
            Your platform earnings ({payout.feePercent}% of every sale)
          </p>
          <p className="text-3xl font-bold text-accent">
            {formatPrice(platformEarnings)}
          </p>
        </div>
        <div className="text-right text-sm">
          <p className="text-muted">Payout to</p>
          <p className="font-mono font-semibold">bKash {payout.bkash}</p>
          <p className="font-mono font-semibold">Nagad {payout.nagad}</p>
        </div>
      </div>

      {/* Seller approvals */}
      <section className="mt-8">
        <h2 className="text-lg font-bold">Pending sellers ({pendingSellers.length})</h2>
        <div className="mt-3 space-y-2">
          {pendingSellers.length === 0 && (
            <p className="text-sm text-muted">No pending applications.</p>
          )}
          {pendingSellers.map((s) => (
            <div key={s.id} className="card flex items-center justify-between p-4">
              <div>
                <p className="font-medium">{s.displayName}</p>
                <p className="text-sm text-muted">{s.user.email}</p>
              </div>
              <form action={approveSeller}>
                <input type="hidden" name="id" value={s.id} />
                <button className="btn-primary">Approve</button>
              </form>
            </div>
          ))}
        </div>
      </section>

      {/* Product review */}
      <section className="mt-8">
        <h2 className="text-lg font-bold">Products awaiting review ({pendingProducts.length})</h2>
        <div className="mt-3 space-y-2">
          {pendingProducts.length === 0 && (
            <p className="text-sm text-muted">Nothing to review.</p>
          )}
          {pendingProducts.map((p) => (
            <div key={p.id} className="card flex flex-wrap items-center justify-between gap-3 p-4">
              <div>
                <p className="font-medium">{p.title}</p>
                <p className="text-sm text-muted">
                  by {p.seller.displayName} · {formatPrice(p.priceCents)}
                </p>
              </div>
              <div className="flex items-center gap-2">
                <StatusPill status={p.status} />
                <form action={setProductStatus} className="flex gap-2">
                  <input type="hidden" name="id" value={p.id} />
                  <button name="status" value="PUBLISHED" className="btn-primary">
                    Publish
                  </button>
                  <button name="status" value="REJECTED" className="btn-ghost">
                    Reject
                  </button>
                </form>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Payouts */}
      <section className="mt-8">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-bold">
            Payout requests ({pendingPayouts.length})
          </h2>
          <form action={releaseEscrowAction}>
            <button className="btn-ghost text-sm">Release due escrow</button>
          </form>
        </div>
        <div className="mt-3 space-y-2">
          {pendingPayouts.length === 0 && (
            <p className="text-sm text-muted">No payout requests.</p>
          )}
          {pendingPayouts.map((p) => (
            <div key={p.id} className="card flex items-center justify-between p-4">
              <div>
                <p className="font-medium">{formatPrice(p.amountCents)}</p>
                <p className="text-sm text-muted">
                  {p.seller.displayName} · {p.method} · {timeAgo(p.requestedAt)}
                </p>
              </div>
              <form action={processPayout}>
                <input type="hidden" name="id" value={p.id} />
                <button className="btn-primary">Mark paid</button>
              </form>
            </div>
          ))}
        </div>
      </section>

      {/* Disputes */}
      <section className="mt-8">
        <h2 className="text-lg font-bold">Open disputes ({openDisputes.length})</h2>
        <div className="mt-3 space-y-2">
          {openDisputes.length === 0 && (
            <p className="text-sm text-muted">No open disputes.</p>
          )}
          {openDisputes.map((d) => (
            <div key={d.id} className="card p-4">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div>
                  <p className="font-medium">
                    Order #{d.order.id.slice(0, 8)} ·{" "}
                    {formatPrice(d.order.totalCents)}
                  </p>
                  <p className="text-sm text-muted">{d.reason}</p>
                </div>
                <form action={resolveDispute} className="flex gap-2">
                  <input type="hidden" name="id" value={d.id} />
                  <button name="decision" value="refund" className="btn-primary">
                    Refund buyer
                  </button>
                  <button name="decision" value="reject" className="btn-ghost">
                    Reject
                  </button>
                </form>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}

function Stat({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="card p-5">
      <p className="text-2xl font-bold">{value}</p>
      <p className="text-sm text-muted">{label}</p>
    </div>
  );
}
