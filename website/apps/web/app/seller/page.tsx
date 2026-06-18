import Link from "next/link";
import { redirect } from "next/navigation";
import { Plus, Package, TrendingUp, Wallet } from "lucide-react";
import prisma from "@brothercraft/db";
import { requireUser } from "@/lib/session";
import { formatPrice } from "@/lib/utils";
import { StatusPill, EmptyState } from "@/components/ui";

export const metadata = { title: "Seller dashboard" };

export default async function SellerDashboard() {
  const user = await requireUser();
  const seller = await prisma.sellerProfile.findUnique({
    where: { userId: user.id },
    include: {
      products: { orderBy: { createdAt: "desc" } },
    },
  });
  if (!seller) redirect("/sell");

  const salesAgg = await prisma.orderItem.aggregate({
    where: {
      sellerId: seller.id,
      order: { status: { in: ["PAID", "COMPLETED", "ESCROW_HELD"] } },
    },
    _sum: { unitPriceCents: true },
    _count: true,
  });

  const stats = [
    { icon: <Package className="h-5 w-5" />, label: "Products", value: seller.products.length },
    { icon: <TrendingUp className="h-5 w-5" />, label: "Sales", value: salesAgg._count },
    {
      icon: <Wallet className="h-5 w-5" />,
      label: "Gross revenue",
      value: formatPrice(salesAgg._sum.unitPriceCents ?? 0),
    },
  ];

  return (
    <div className="container-page py-8">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold">{seller.displayName}</h1>
          <div className="mt-1 flex items-center gap-2">
            <StatusPill status={seller.status} />
            <Link
              href={`/store/${seller.slug}`}
              className="text-sm text-accent hover:underline"
            >
              View storefront
            </Link>
          </div>
        </div>
        <div className="flex gap-2">
          <Link href="/seller/payouts" className="btn-ghost">
            <Wallet className="h-4 w-4" /> Payouts
          </Link>
          <Link href="/seller/new" className="btn-primary">
            <Plus className="h-4 w-4" /> New product
          </Link>
        </div>
      </div>

      {seller.status === "PENDING" && (
        <div className="mt-4 rounded-lg border border-yellow-500/40 bg-yellow-500/10 px-4 py-3 text-sm text-yellow-300">
          Your seller application is pending review. You can create products now —
          they&apos;ll publish once approved.
        </div>
      )}

      <div className="mt-6 grid gap-4 sm:grid-cols-3">
        {stats.map((s) => (
          <div key={s.label} className="card p-5">
            <span className="grid h-10 w-10 place-items-center rounded-lg bg-accent/15 text-accent">
              {s.icon}
            </span>
            <p className="mt-3 text-2xl font-bold">{s.value}</p>
            <p className="text-sm text-muted">{s.label}</p>
          </div>
        ))}
      </div>

      <h2 className="mt-8 text-lg font-bold">Your products</h2>
      <div className="mt-3 space-y-2">
        {seller.products.length === 0 && (
          <EmptyState
            title="No products yet"
            hint="Create your first listing to start selling."
            action={
              <Link href="/seller/new" className="btn-primary mt-2">
                <Plus className="h-4 w-4" /> New product
              </Link>
            }
          />
        )}
        {seller.products.map((p) => (
          <div
            key={p.id}
            className="card flex flex-wrap items-center justify-between gap-3 p-4"
          >
            <div>
              <Link href={`/product/${p.slug}`} className="font-medium hover:text-accent">
                {p.title}
              </Link>
              <p className="text-sm text-muted">
                {p.downloadsCount} downloads · {p.tags.join(", ") || "no tags"}
              </p>
            </div>
            <div className="flex items-center gap-3">
              <StatusPill status={p.status} />
              <span className="font-semibold">{formatPrice(p.priceCents)}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
