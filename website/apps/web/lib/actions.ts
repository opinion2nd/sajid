"use server";

import { revalidatePath } from "next/cache";
import { redirect } from "next/navigation";
import prisma from "@brothercraft/db";
import { requireUser } from "@/lib/session";

function slugify(s: string) {
  return s
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/(^-|-$)/g, "")
    .slice(0, 60);
}

export async function applyAsSeller(formData: FormData) {
  const user = await requireUser();
  const displayName = String(formData.get("displayName") ?? "").trim();
  const bio = String(formData.get("bio") ?? "").trim();
  if (displayName.length < 3) throw new Error("Display name too short");

  let slug = slugify(displayName);
  const exists = await prisma.sellerProfile.findUnique({ where: { slug } });
  if (exists) slug = `${slug}-${Math.random().toString(36).slice(2, 6)}`;

  await prisma.sellerProfile.upsert({
    where: { userId: user.id },
    update: { displayName, bio },
    create: { userId: user.id, displayName, bio, slug, status: "PENDING" },
  });
  // First seller application auto-grants the SELLER role (admin still approves the profile).
  await prisma.user.update({
    where: { id: user.id },
    data: { role: user.role === "ADMIN" ? "ADMIN" : "SELLER" },
  });

  revalidatePath("/seller");
  redirect("/seller");
}

export async function createProduct(formData: FormData) {
  const user = await requireUser();
  const seller = await prisma.sellerProfile.findUnique({
    where: { userId: user.id },
  });
  if (!seller) redirect("/sell");

  const title = String(formData.get("title") ?? "").trim();
  const summary = String(formData.get("summary") ?? "").trim();
  const description = String(formData.get("description") ?? "").trim();
  const categorySlug = String(formData.get("category") ?? "");
  const priceTaka = Number(formData.get("price") ?? 0);
  const licenseGated = formData.get("licenseGated") === "on";
  const tags = String(formData.get("tags") ?? "")
    .split(",")
    .map((t) => t.trim().toLowerCase())
    .filter(Boolean);

  if (title.length < 3) throw new Error("Title too short");
  const category = await prisma.category.findUnique({
    where: { slug: categorySlug },
  });
  if (!category) throw new Error("Invalid category");

  let slug = slugify(title);
  if (await prisma.product.findUnique({ where: { slug } })) {
    slug = `${slug}-${Math.random().toString(36).slice(2, 6)}`;
  }

  await prisma.product.create({
    data: {
      sellerId: seller.id,
      categoryId: category.id,
      title,
      slug,
      summary: summary || title,
      description: description || summary || title,
      priceCents: Math.max(0, Math.round(priceTaka * 100)),
      type: category.slug === "services" ? "SERVICE" : "DIGITAL",
      status: seller.status === "APPROVED" ? "PUBLISHED" : "PENDING_REVIEW",
      licenseGated,
      licensePrefix: licenseGated ? "BC" : "BC",
      tags,
      versions: {
        create: { semver: "1.0.0", changelog: "Initial release.", status: "PUBLISHED" },
      },
    },
  });

  revalidatePath("/seller");
  redirect("/seller");
}

// ── Admin actions ───────────────────────────────────

async function requireAdmin() {
  const user = await requireUser();
  if (user.role !== "ADMIN") redirect("/dashboard");
  return user;
}

export async function approveSeller(formData: FormData) {
  await requireAdmin();
  const id = String(formData.get("id"));
  await prisma.sellerProfile.update({
    where: { id },
    data: { status: "APPROVED", verified: true },
  });
  revalidatePath("/admin");
}

export async function setProductStatus(formData: FormData) {
  await requireAdmin();
  const id = String(formData.get("id"));
  const status = String(formData.get("status")) as
    | "PUBLISHED"
    | "REJECTED"
    | "DELISTED";
  await prisma.product.update({ where: { id }, data: { status } });
  revalidatePath("/admin");
}
