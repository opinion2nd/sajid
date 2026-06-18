import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { BadgeCheck, Package } from "lucide-react";
import prisma from "@brothercraft/db";
import { ProductCard, type ProductCardData } from "@/components/product-card";
import { Rating, EmptyState } from "@/components/ui";

async function getStore(slug: string) {
  return prisma.sellerProfile.findUnique({
    where: { slug },
    include: {
      products: {
        where: { status: "PUBLISHED" },
        include: {
          seller: { select: { displayName: true } },
          category: { select: { name: true } },
        },
        orderBy: { downloadsCount: "desc" },
      },
    },
  });
}

export async function generateMetadata({
  params,
}: {
  params: Promise<{ slug: string }>;
}): Promise<Metadata> {
  const { slug } = await params;
  const store = await getStore(slug);
  return { title: store ? store.displayName : "Store not found" };
}

export default async function StorePage({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;
  const store = await getStore(slug);
  if (!store) notFound();

  const cards: ProductCardData[] = store.products.map((p) => ({
    slug: p.slug,
    title: p.title,
    summary: p.summary,
    priceCents: p.priceCents,
    currency: p.currency,
    ratingAvg: p.ratingAvg,
    downloadsCount: p.downloadsCount,
    licenseGated: p.licenseGated,
    tags: p.tags,
    sellerName: p.seller.displayName,
    categoryName: p.category.name,
  }));

  return (
    <div className="container-page py-8">
      {/* Banner */}
      <div className="card overflow-hidden p-0">
        <div className="h-28 bg-gradient-to-r from-brand/30 to-accent/30" />
        <div className="flex flex-wrap items-center gap-4 p-5">
          <div className="-mt-12 grid h-20 w-20 place-items-center rounded-xl border-4 border-panel bg-surface">
            <Package className="h-8 w-8 text-accent" />
          </div>
          <div className="flex-1">
            <h1 className="flex items-center gap-2 text-2xl font-bold">
              {store.displayName}
              {store.verified && <BadgeCheck className="h-5 w-5 text-accent" />}
            </h1>
            {store.bio && <p className="text-muted">{store.bio}</p>}
          </div>
          <div className="flex gap-6 text-center">
            <div>
              <Rating value={store.ratingAvg} />
              <p className="text-xs text-muted">rating</p>
            </div>
            <div>
              <p className="font-bold">{store.salesCount.toLocaleString()}</p>
              <p className="text-xs text-muted">sales</p>
            </div>
          </div>
        </div>
      </div>

      <h2 className="mt-8 text-lg font-bold">Products ({cards.length})</h2>
      <div className="mt-4">
        {cards.length === 0 ? (
          <EmptyState title="No products yet" />
        ) : (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {cards.map((p) => (
              <ProductCard key={p.slug} product={p} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
