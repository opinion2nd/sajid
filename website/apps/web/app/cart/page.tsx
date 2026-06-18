"use client";

import { useState } from "react";
import Link from "next/link";
import { Trash2, ShoppingCart, Loader2, Package } from "lucide-react";
import { useCart } from "@/components/cart";
import { formatPrice } from "@/lib/utils";

export default function CartPage() {
  const { items, remove, totalCents } = useCart();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function checkout() {
    setError(null);
    setLoading(true);
    const res = await fetch("/api/checkout", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ productIds: items.map((i) => i.productId) }),
    });
    if (res.status === 401) {
      window.location.href = "/login";
      return;
    }
    if (!res.ok) {
      const j = await res.json().catch(() => ({}));
      setError(j.error ?? "Checkout failed");
      setLoading(false);
      return;
    }
    const { redirectUrl } = await res.json();
    window.location.href = redirectUrl;
  }

  return (
    <div className="container-page py-8">
      <h1 className="text-2xl font-bold">Your cart</h1>

      {items.length === 0 ? (
        <div className="card mt-6 flex flex-col items-center gap-3 p-12 text-center">
          <ShoppingCart className="h-10 w-10 text-muted" />
          <p className="text-lg font-semibold">Your cart is empty</p>
          <Link href="/browse" className="btn-primary">
            Browse marketplace
          </Link>
        </div>
      ) : (
        <div className="mt-6 grid gap-6 lg:grid-cols-[1fr_320px]">
          <div className="space-y-3">
            {items.map((item) => (
              <div
                key={item.productId}
                className="card flex items-center gap-4 p-4"
              >
                <div className="grid h-14 w-14 shrink-0 place-items-center rounded-lg bg-surface">
                  <Package className="h-6 w-6 text-muted" />
                </div>
                <div className="min-w-0 flex-1">
                  <Link
                    href={`/product/${item.slug}`}
                    className="font-medium hover:text-accent"
                  >
                    {item.title}
                  </Link>
                  <p className="text-sm text-muted">by {item.sellerName}</p>
                </div>
                <span className="font-semibold">
                  {formatPrice(item.priceCents)}
                </span>
                <button
                  onClick={() => remove(item.productId)}
                  className="rounded-lg border border-border bg-surface p-2 text-muted hover:text-red-400"
                  aria-label="Remove"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            ))}
          </div>

          <aside className="lg:sticky lg:top-20 lg:h-fit">
            <div className="card p-5">
              <h2 className="font-semibold">Order summary</h2>
              <div className="mt-4 space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-muted">Subtotal</span>
                  <span>{formatPrice(totalCents)}</span>
                </div>
                <div className="flex justify-between border-t border-border pt-2 text-base font-bold">
                  <span>Total</span>
                  <span>{formatPrice(totalCents)}</span>
                </div>
              </div>
              {error && (
                <p className="mt-3 rounded-lg border border-red-500/40 bg-red-500/10 px-3 py-2 text-sm text-red-400">
                  {error}
                </p>
              )}
              <button
                onClick={checkout}
                disabled={loading}
                className="btn-primary mt-4 w-full"
              >
                {loading && <Loader2 className="h-4 w-4 animate-spin" />}
                Pay with bKash / Nagad
              </button>
              <p className="mt-2 text-center text-xs text-muted">
                Demo mode — payment is simulated instantly.
              </p>
            </div>
          </aside>
        </div>
      )}
    </div>
  );
}
