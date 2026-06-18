import { redirect } from "next/navigation";
import prisma from "@brothercraft/db";
import { requireUser } from "@/lib/session";
import { formatPrice } from "@/lib/utils";
import { StatusPill } from "@/components/ui";
import { approveSeller, setProductStatus } from "@/lib/actions";

export const metadata = { title: "Admin" };

export default async function AdminPage() {
  const user = await requireUser();
  if (user.role !== "ADMIN") redirect("/dashboard");

  const [pendingSellers, pendingProducts, totals] = await Promise.all([
    prisma.sellerProfile.findMany({
      where: { status: "PENDING" },
      include: { user: { select: { email: true } } },
    }),
    prisma.product.findMany({
      where: { status: "PENDING_REVIEW" },
      include: { seller: { select: { displayName: true } } },
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

  return (
    <div className="container-page py-8">
      <h1 className="text-2xl font-bold">Admin panel</h1>

      <div className="mt-6 grid gap-4 sm:grid-cols-4">
        <Stat label="Users" value={userCount} />
        <Stat label="Live products" value={productCount} />
        <Stat label="Orders" value={orderAgg._count} />
        <Stat label="GMV" value={formatPrice(orderAgg._sum.totalCents ?? 0)} />
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
