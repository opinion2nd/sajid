import Link from "next/link";
import {
  ShieldCheck,
  Wallet,
  MessageSquare,
  Package,
  ArrowRight,
  Search,
} from "lucide-react";
import { getFeaturedProducts, getCategories } from "@/lib/queries";
import { ProductCard } from "@/components/product-card";

export default async function HomePage() {
  const [featured, categories] = await Promise.all([
    getFeaturedProducts(4),
    getCategories(),
  ]);

  return (
    <>
      {/* Hero */}
      <section className="container-page py-14 sm:py-20">
        <div className="mx-auto max-w-3xl text-center">
          <span className="badge mx-auto">🟢 Now in beta · bKash &amp; Nagad ready</span>
          <h1 className="mt-5 text-4xl font-extrabold leading-tight tracking-tight sm:text-6xl">
            The Minecraft marketplace for{" "}
            <span className="text-accent">creators</span>
          </h1>
          <p className="mx-auto mt-5 max-w-xl text-lg text-muted">
            Buy and sell plugins, configs, builds and services. License-gated
            downloads, escrow-protected payments, and a Discord-native community.
          </p>
          <div className="mx-auto mt-8 flex max-w-md flex-col gap-3 sm:flex-row">
            <Link href="/browse" className="btn-primary flex-1">
              <Search className="h-4 w-4" /> Browse marketplace
            </Link>
            <Link href="/sell" className="btn-ghost flex-1">
              Start selling <ArrowRight className="h-4 w-4" />
            </Link>
          </div>
        </div>
      </section>

      {/* Feature strip */}
      <section className="container-page grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {[
          {
            icon: <Package className="h-5 w-5" />,
            t: "Sell anything Minecraft",
            d: "Plugins, configs, builds & services — versioned with changelogs.",
          },
          {
            icon: <ShieldCheck className="h-5 w-5" />,
            t: "License-gated plugins",
            d: "Every purchase mints a key your plugin validates against our API.",
          },
          {
            icon: <Wallet className="h-5 w-5" />,
            t: "Escrow-protected pay",
            d: "bKash & Nagad checkout with seller payouts and buyer protection.",
          },
          {
            icon: <MessageSquare className="h-5 w-5" />,
            t: "Discord integration",
            d: "Link accounts, auto-assign roles, get receipts in your DMs.",
          },
        ].map((f) => (
          <div key={f.t} className="card p-5">
            <span className="grid h-10 w-10 place-items-center rounded-lg bg-accent/15 text-accent">
              {f.icon}
            </span>
            <h3 className="mt-3 font-semibold">{f.t}</h3>
            <p className="mt-1 text-sm text-muted">{f.d}</p>
          </div>
        ))}
      </section>

      {/* Categories */}
      <section className="container-page mt-14">
        <h2 className="text-xl font-bold">Browse by category</h2>
        <div className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-4">
          {categories.map((c) => (
            <Link
              key={c.slug}
              href={`/browse?category=${c.slug}`}
              className="card flex items-center justify-between p-4 transition hover:border-accent/50"
            >
              <span className="font-medium">{c.name}</span>
              <span className="badge">{c._count.products}</span>
            </Link>
          ))}
        </div>
      </section>

      {/* Featured */}
      <section className="container-page mt-14">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-bold">Popular right now</h2>
          <Link
            href="/browse"
            className="inline-flex items-center gap-1 text-sm text-accent hover:underline"
          >
            View all <ArrowRight className="h-4 w-4" />
          </Link>
        </div>
        <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {featured.map((p) => (
            <ProductCard key={p.slug} product={p} />
          ))}
        </div>
      </section>
    </>
  );
}
