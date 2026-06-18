import prisma from "@brothercraft/db";
import { Download, KeyRound, ShieldCheck } from "lucide-react";
import { requireUser } from "@/lib/session";
import { EmptyState } from "@/components/ui";

export default async function DownloadsPage() {
  const user = await requireUser();

  // Distinct purchased products from completed/paid orders.
  const items = await prisma.orderItem.findMany({
    where: {
      order: { buyerId: user.id, status: { in: ["PAID", "COMPLETED", "ESCROW_HELD"] } },
    },
    include: { product: { include: { versions: { orderBy: { createdAt: "desc" }, take: 1 } } } },
  });
  const licenses = await prisma.license.findMany({
    where: { buyerId: user.id },
    select: { productId: true, key: true, status: true },
  });
  const licenseByProduct = new Map(licenses.map((l) => [l.productId, l]));

  // De-duplicate by product.
  const seen = new Set<string>();
  const products = items
    .filter((i) => {
      if (seen.has(i.productId)) return false;
      seen.add(i.productId);
      return true;
    })
    .map((i) => i.product);

  return (
    <div>
      <h1 className="text-2xl font-bold">Downloads &amp; Licenses</h1>
      <p className="mt-1 text-muted">
        Your purchased products. Downloads include all future updates.
      </p>

      <div className="mt-6 space-y-3">
        {products.length === 0 && (
          <EmptyState
            title="Nothing purchased yet"
            hint="Buy a product and it will appear here with its download and license key."
          />
        )}
        {products.map((p) => {
          const license = licenseByProduct.get(p.id);
          return (
            <div key={p.id} className="card p-4">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h3 className="font-semibold">{p.title}</h3>
                  <p className="text-sm text-muted">
                    Latest version {p.versions[0]?.semver ?? "1.0.0"}
                  </p>
                </div>
                <a href={`/api/downloads/${p.id}`} className="btn-primary">
                  <Download className="h-4 w-4" /> Download
                </a>
              </div>

              {p.licenseGated && (
                <div className="mt-3 rounded-lg border border-accent/30 bg-accent/5 p-3">
                  <div className="flex items-center gap-2 text-sm font-medium text-accent">
                    <ShieldCheck className="h-4 w-4" /> License key
                  </div>
                  {license ? (
                    <div className="mt-1 flex items-center gap-2">
                      <KeyRound className="h-4 w-4 text-muted" />
                      <code className="rounded bg-surface px-2 py-1 font-mono text-sm">
                        {license.key}
                      </code>
                      <span className="text-xs text-muted">({license.status})</span>
                    </div>
                  ) : (
                    <p className="mt-1 text-sm text-muted">
                      Key is being generated…
                    </p>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
