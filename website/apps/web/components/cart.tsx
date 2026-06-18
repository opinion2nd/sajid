"use client";

import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";

export type CartItem = {
  productId: string;
  slug: string;
  title: string;
  priceCents: number;
  coverImage?: string | null;
  sellerName: string;
};

type CartCtx = {
  items: CartItem[];
  add: (item: CartItem) => void;
  remove: (productId: string) => void;
  clear: () => void;
  has: (productId: string) => boolean;
  totalCents: number;
};

const Ctx = createContext<CartCtx | null>(null);
const KEY = "bc_cart_v1";

export function CartProvider({ children }: { children: React.ReactNode }) {
  const [items, setItems] = useState<CartItem[]>([]);

  useEffect(() => {
    try {
      const raw = localStorage.getItem(KEY);
      if (raw) setItems(JSON.parse(raw));
    } catch {
      /* ignore */
    }
  }, []);

  useEffect(() => {
    localStorage.setItem(KEY, JSON.stringify(items));
  }, [items]);

  const value = useMemo<CartCtx>(
    () => ({
      items,
      add: (item) =>
        setItems((prev) =>
          prev.some((p) => p.productId === item.productId)
            ? prev
            : [...prev, item]
        ),
      remove: (productId) =>
        setItems((prev) => prev.filter((p) => p.productId !== productId)),
      clear: () => setItems([]),
      has: (productId) => items.some((p) => p.productId === productId),
      totalCents: items.reduce((s, i) => s + i.priceCents, 0),
    }),
    [items]
  );

  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export function useCart() {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error("useCart must be used within CartProvider");
  return ctx;
}
