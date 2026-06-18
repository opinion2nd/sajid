"use client";

import { useRouter } from "next/navigation";
import { Check, ShoppingCart } from "lucide-react";
import { useCart, type CartItem } from "./cart";

export function AddToCartButton({ item }: { item: CartItem }) {
  const { add, has } = useCart();
  const router = useRouter();
  const inCart = has(item.productId);

  return (
    <div className="flex flex-col gap-2 sm:flex-row">
      <button
        onClick={() => add(item)}
        disabled={inCart}
        className="btn-primary flex-1"
      >
        {inCart ? (
          <>
            <Check className="h-4 w-4" /> In cart
          </>
        ) : (
          <>
            <ShoppingCart className="h-4 w-4" /> Add to cart
          </>
        )}
      </button>
      <button
        onClick={() => {
          add(item);
          router.push("/cart");
        }}
        className="btn-ghost flex-1"
      >
        Buy now
      </button>
    </div>
  );
}
