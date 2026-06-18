import Link from "next/link";
import { Package, ShieldCheck, Download } from "lucide-react";
import { formatPrice } from "@/lib/utils";
import { Rating } from "./ui";

export type ProductCardData = {
  slug: string;
  title: string;
  summary: string;
  priceCents: number;
  currency: string;
  ratingAvg: number;
  downloadsCount: number;
  licenseGated: boolean;
  tags: string[];
  sellerName: string;
  categoryName: string;
};

export function ProductCard({ product }: { product: ProductCardData }) {
  return (
    <Link
      href={`/product/${product.slug}`}
      className="card group flex flex-col overflow-hidden p-0 transition hover:border-accent/50"
    >
      <div className="relative flex aspect-[16/9] items-center justify-center bg-gradient-to-br from-surface to-panel">
        <Package className="h-10 w-10 text-muted transition group-hover:scale-110 group-hover:text-accent" />
        <span className="absolute left-3 top-3 badge">{product.categoryName}</span>
        {product.licenseGated && (
          <span className="absolute right-3 top-3 inline-flex items-center gap-1 rounded-full border border-accent/40 bg-bg/70 px-2 py-0.5 text-xs text-accent">
            <ShieldCheck className="h-3 w-3" /> Licensed
          </span>
        )}
      </div>
      <div className="flex flex-1 flex-col p-4">
        <h3 className="font-semibold leading-tight group-hover:text-accent">
          {product.title}
        </h3>
        <p className="mt-1 line-clamp-2 flex-1 text-sm text-muted">
          {product.summary}
        </p>
        <div className="mt-3 flex items-center justify-between text-xs text-muted">
          <span>by {product.sellerName}</span>
          <span className="inline-flex items-center gap-1">
            <Download className="h-3 w-3" />
            {product.downloadsCount.toLocaleString()}
          </span>
        </div>
        <div className="mt-3 flex items-center justify-between border-t border-border pt-3">
          <Rating value={product.ratingAvg} />
          <span className="text-base font-bold text-text">
            {formatPrice(product.priceCents, product.currency)}
          </span>
        </div>
      </div>
    </Link>
  );
}
