import prisma from "@brothercraft/db";
import type { ProductCardData } from "@/components/product-card";

type ProductWithRels = {
  slug: string;
  title: string;
  summary: string;
  priceCents: number;
  currency: string;
  ratingAvg: number;
  downloadsCount: number;
  licenseGated: boolean;
  tags: string[];
  seller: { displayName: string };
  category: { name: string };
};

function toCard(p: ProductWithRels): ProductCardData {
  return {
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
  };
}

const cardSelect = {
  slug: true,
  title: true,
  summary: true,
  priceCents: true,
  currency: true,
  ratingAvg: true,
  downloadsCount: true,
  licenseGated: true,
  tags: true,
  seller: { select: { displayName: true } },
  category: { select: { name: true } },
} as const;

export async function getFeaturedProducts(limit = 4): Promise<ProductCardData[]> {
  const rows = await prisma.product.findMany({
    where: { status: "PUBLISHED" },
    orderBy: { downloadsCount: "desc" },
    take: limit,
    select: cardSelect,
  });
  return rows.map(toCard);
}

export type BrowseFilters = {
  q?: string;
  category?: string;
  sort?: "popular" | "newest" | "price_asc" | "price_desc" | "rating";
};

export async function browseProducts(
  filters: BrowseFilters
): Promise<ProductCardData[]> {
  const orderBy =
    filters.sort === "newest"
      ? { createdAt: "desc" as const }
      : filters.sort === "price_asc"
        ? { priceCents: "asc" as const }
        : filters.sort === "price_desc"
          ? { priceCents: "desc" as const }
          : filters.sort === "rating"
            ? { ratingAvg: "desc" as const }
            : { downloadsCount: "desc" as const };

  const rows = await prisma.product.findMany({
    where: {
      status: "PUBLISHED",
      ...(filters.category ? { category: { slug: filters.category } } : {}),
      ...(filters.q
        ? {
            OR: [
              { title: { contains: filters.q, mode: "insensitive" } },
              { summary: { contains: filters.q, mode: "insensitive" } },
              { tags: { has: filters.q.toLowerCase() } },
            ],
          }
        : {}),
    },
    orderBy,
    select: cardSelect,
  });
  return rows.map(toCard);
}

export async function getCategories() {
  return prisma.category.findMany({
    where: { parentId: null },
    orderBy: { name: "asc" },
    select: { name: true, slug: true, _count: { select: { products: true } } },
  });
}
