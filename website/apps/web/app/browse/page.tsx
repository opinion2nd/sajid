import type { Metadata } from "next";
import Link from "next/link";
import { browseProducts, getCategories, type BrowseFilters } from "@/lib/queries";
import { ProductCard } from "@/components/product-card";
import { EmptyState } from "@/components/ui";
import { cn } from "@/lib/utils";

export const metadata: Metadata = { title: "Browse" };

const sorts: { key: NonNullable<BrowseFilters["sort"]>; label: string }[] = [
  { key: "popular", label: "Popular" },
  { key: "newest", label: "Newest" },
  { key: "rating", label: "Top rated" },
  { key: "price_asc", label: "Price ↑" },
  { key: "price_desc", label: "Price ↓" },
];

export default async function BrowsePage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | undefined>>;
}) {
  const sp = await searchParams;
  const filters: BrowseFilters = {
    q: sp.q,
    category: sp.category,
    sort: (sp.sort as BrowseFilters["sort"]) ?? "popular",
  };
  const [products, categories] = await Promise.all([
    browseProducts(filters),
    getCategories(),
  ]);

  const buildHref = (patch: Partial<Record<string, string>>) => {
    const params = new URLSearchParams();
    const merged = { ...sp, ...patch };
    for (const [k, v] of Object.entries(merged)) if (v) params.set(k, v);
    return `/browse?${params.toString()}`;
  };

  return (
    <div className="container-page py-8">
      <h1 className="text-2xl font-bold">Browse marketplace</h1>

      {/* Search */}
      <form action="/browse" className="mt-4 flex gap-2">
        {sp.category && <input type="hidden" name="category" value={sp.category} />}
        <input
          name="q"
          defaultValue={sp.q ?? ""}
          placeholder="Search plugins, configs, builds…"
          className="input"
        />
        <button className="btn-primary">Search</button>
      </form>

      <div className="mt-6 grid gap-6 lg:grid-cols-[220px_1fr]">
        {/* Filters sidebar — collapses above the grid on mobile */}
        <aside className="space-y-5">
          <div>
            <h3 className="mb-2 text-sm font-semibold">Categories</h3>
            <div className="flex flex-wrap gap-2 lg:flex-col">
              <Link
                href={buildHref({ category: undefined })}
                className={cn(
                  "rounded-lg border border-border bg-surface px-3 py-1.5 text-sm hover:border-accent/50",
                  !sp.category && "border-accent/50 text-accent"
                )}
              >
                All
              </Link>
              {categories.map((c) => (
                <Link
                  key={c.slug}
                  href={buildHref({ category: c.slug })}
                  className={cn(
                    "flex items-center justify-between gap-2 rounded-lg border border-border bg-surface px-3 py-1.5 text-sm hover:border-accent/50",
                    sp.category === c.slug && "border-accent/50 text-accent"
                  )}
                >
                  {c.name}
                  <span className="text-xs text-muted">{c._count.products}</span>
                </Link>
              ))}
            </div>
          </div>
          <div>
            <h3 className="mb-2 text-sm font-semibold">Sort by</h3>
            <div className="flex flex-wrap gap-2 lg:flex-col">
              {sorts.map((s) => (
                <Link
                  key={s.key}
                  href={buildHref({ sort: s.key })}
                  className={cn(
                    "rounded-lg border border-border bg-surface px-3 py-1.5 text-sm hover:border-accent/50",
                    filters.sort === s.key && "border-accent/50 text-accent"
                  )}
                >
                  {s.label}
                </Link>
              ))}
            </div>
          </div>
        </aside>

        {/* Results */}
        <div>
          <p className="mb-3 text-sm text-muted">
            {products.length} result{products.length === 1 ? "" : "s"}
            {sp.q && ` for “${sp.q}”`}
          </p>
          {products.length === 0 ? (
            <EmptyState
              title="No products found"
              hint="Try a different search term or category."
            />
          ) : (
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
              {products.map((p) => (
                <ProductCard key={p.slug} product={p} />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
