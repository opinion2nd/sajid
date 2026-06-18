import type { Metadata } from "next";
import { notFound } from "next/navigation";
import Link from "next/link";
import { Package, ShieldCheck, Download, Tag, Clock } from "lucide-react";
import prisma from "@brothercraft/db";
import { formatPrice, timeAgo } from "@/lib/utils";
import { Rating } from "@/components/ui";
import { AddToCartButton } from "@/components/add-to-cart";

async function getProduct(slug: string) {
  return prisma.product.findUnique({
    where: { slug },
    include: {
      seller: { select: { displayName: true, slug: true, verified: true } },
      category: { select: { name: true, slug: true } },
      versions: { orderBy: { createdAt: "desc" } },
      reviews: {
        where: { status: "VISIBLE" },
        include: { buyer: { select: { handle: true } } },
        orderBy: { createdAt: "desc" },
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
  const product = await getProduct(slug);
  if (!product) return { title: "Not found" };
  return { title: product.title, description: product.summary };
}

export default async function ProductPage({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;
  const product = await getProduct(slug);
  if (!product || product.status !== "PUBLISHED") notFound();

  const jsonLd = {
    "@context": "https://schema.org",
    "@type": "Product",
    name: product.title,
    description: product.summary,
    offers: {
      "@type": "Offer",
      price: (product.priceCents / 100).toFixed(2),
      priceCurrency: product.currency,
    },
    aggregateRating: {
      "@type": "AggregateRating",
      ratingValue: product.ratingAvg,
      reviewCount: product.reviews.length,
    },
  };

  return (
    <div className="container-page py-8">
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />

      <nav className="mb-4 text-sm text-muted">
        <Link href="/browse" className="hover:text-text">
          Browse
        </Link>{" "}
        /{" "}
        <Link href={`/browse?category=${product.category.slug}`} className="hover:text-text">
          {product.category.name}
        </Link>
      </nav>

      <div className="grid gap-8 lg:grid-cols-[1fr_320px]">
        {/* Main */}
        <div>
          <div className="flex aspect-[16/8] items-center justify-center rounded-xl border border-border bg-gradient-to-br from-surface to-panel">
            <Package className="h-16 w-16 text-muted" />
          </div>

          <div className="mt-6">
            <div className="flex flex-wrap items-center gap-2">
              <span className="badge">{product.category.name}</span>
              {product.licenseGated && (
                <span className="inline-flex items-center gap-1 rounded-full border border-accent/40 bg-surface px-2.5 py-0.5 text-xs text-accent">
                  <ShieldCheck className="h-3 w-3" /> License-gated
                </span>
              )}
            </div>
            <h1 className="mt-3 text-3xl font-bold">{product.title}</h1>
            <p className="mt-2 text-muted">{product.summary}</p>
            <div className="mt-3 flex flex-wrap items-center gap-4 text-sm text-muted">
              <Rating value={product.ratingAvg} count={product.reviews.length} />
              <span className="inline-flex items-center gap-1">
                <Download className="h-4 w-4" />
                {product.downloadsCount.toLocaleString()} downloads
              </span>
              <span className="inline-flex items-center gap-1">
                <Clock className="h-4 w-4" />
                Updated {timeAgo(product.updatedAt)}
              </span>
            </div>
          </div>

          <article className="prose-mc mt-8">
            {product.description.split("\n").map((line, i) => {
              if (line.startsWith("## "))
                return <h2 key={i}>{line.slice(3)}</h2>;
              if (line.startsWith("- "))
                return (
                  <ul key={i}>
                    <li>{line.slice(2)}</li>
                  </ul>
                );
              if (line.trim() === "") return null;
              return <p key={i}>{line}</p>;
            })}
          </article>

          {/* Reviews */}
          <section className="mt-10">
            <h2 className="text-xl font-bold">
              Reviews ({product.reviews.length})
            </h2>
            <div className="mt-4 space-y-3">
              {product.reviews.length === 0 && (
                <p className="text-sm text-muted">No reviews yet.</p>
              )}
              {product.reviews.map((r) => (
                <div key={r.id} className="card p-4">
                  <div className="flex items-center justify-between">
                    <span className="font-medium">@{r.buyer.handle}</span>
                    <Rating value={r.rating} />
                  </div>
                  {r.body && <p className="mt-2 text-sm text-text/90">{r.body}</p>}
                  {r.sellerReply && (
                    <div className="mt-3 rounded-lg border border-border bg-surface p-3 text-sm">
                      <span className="font-medium text-accent">
                        {product.seller.displayName}
                      </span>
                      <p className="mt-1 text-muted">{r.sellerReply}</p>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </section>
        </div>

        {/* Buy sidebar */}
        <aside className="lg:sticky lg:top-20 lg:h-fit">
          <div className="card p-5">
            <div className="text-3xl font-bold">
              {formatPrice(product.priceCents, product.currency)}
            </div>
            <div className="mt-4">
              <AddToCartButton
                item={{
                  productId: product.id,
                  slug: product.slug,
                  title: product.title,
                  priceCents: product.priceCents,
                  sellerName: product.seller.displayName,
                }}
              />
            </div>
            <p className="mt-3 text-center text-xs text-muted">
              Instant download · Lifetime updates · Escrow protected
            </p>

            <div className="mt-5 space-y-2 border-t border-border pt-4 text-sm">
              <Row label="Seller">
                <Link
                  href={`/store/${product.seller.slug}`}
                  className="text-accent hover:underline"
                >
                  {product.seller.displayName}
                  {product.seller.verified && " ✓"}
                </Link>
              </Row>
              <Row label="Latest version">
                {product.versions[0]?.semver ?? "—"}
              </Row>
              <Row label="License">
                {product.licenseGated ? "Key per purchase" : "None required"}
              </Row>
            </div>

            {product.tags.length > 0 && (
              <div className="mt-4 flex flex-wrap gap-2 border-t border-border pt-4">
                {product.tags.map((t) => (
                  <Link
                    key={t}
                    href={`/browse?q=${encodeURIComponent(t)}`}
                    className="badge hover:border-accent/50"
                  >
                    <Tag className="h-3 w-3" /> {t}
                  </Link>
                ))}
              </div>
            )}
          </div>
        </aside>
      </div>
    </div>
  );
}

function Row({
  label,
  children,
}: {
  label: string;
  children: React.ReactNode;
}) {
  return (
    <div className="flex items-center justify-between">
      <span className="text-muted">{label}</span>
      <span className="font-medium">{children}</span>
    </div>
  );
}
