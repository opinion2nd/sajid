"use server";

import { revalidatePath } from "next/cache";
import { redirect } from "next/navigation";
import prisma from "@brothercraft/db";
import { getStorage } from "@brothercraft/storage";
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

  const product = await prisma.product.create({
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
    include: { versions: true },
  });

  // Optional product file upload — stored privately, served via the
  // entitlement-gated download route.
  const file = formData.get("file");
  if (file instanceof File && file.size > 0) {
    const buffer = Buffer.from(await file.arrayBuffer());
    const storage = getStorage();
    const key = `products/${product.id}/1.0.0/${file.name}`;
    const put = await storage.put(
      key,
      buffer,
      file.type || "application/octet-stream"
    );
    const asset = await prisma.fileAsset.create({
      data: {
        storageKey: put.storageKey,
        sizeBytes: put.sizeBytes,
        checksumSha256: put.checksumSha256,
        contentType: put.contentType,
        virusScanStatus: "SKIPPED",
      },
    });
    await prisma.version.update({
      where: { id: product.versions[0].id },
      data: { fileId: asset.id },
    });
  }

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

// ── Payouts & escrow ────────────────────────────────

/** Releases all escrow holds past their hold window into seller wallets.
 *  Normally run on a schedule; admin can also trigger it. Idempotent. */
export async function releaseDueEscrow() {
  const due = await prisma.escrowHold.findMany({
    where: { state: "HELD", holdUntil: { lte: new Date() } },
  });
  for (const hold of due) {
    await creditSellerFromHold(hold.id);
  }
  return due.length;
}

async function creditSellerFromHold(holdId: string) {
  await prisma.$transaction(async (tx) => {
    const hold = await tx.escrowHold.findUnique({ where: { id: holdId } });
    if (!hold || hold.state !== "HELD") return;
    const seller = await tx.sellerProfile.findUnique({
      where: { id: hold.sellerId },
      select: { userId: true },
    });
    if (!seller) return;
    const wallet = await tx.wallet.upsert({
      where: { userId: seller.userId },
      update: {},
      create: { userId: seller.userId },
    });
    const balanceAfter = wallet.balanceCents + hold.amountCents;
    await tx.wallet.update({
      where: { id: wallet.id },
      data: { balanceCents: balanceAfter },
    });
    await tx.walletEntry.create({
      data: {
        walletId: wallet.id,
        type: "RELEASE",
        amountCents: hold.amountCents,
        refType: "escrow",
        refId: hold.id,
        balanceAfter,
      },
    });
    await tx.escrowHold.update({
      where: { id: hold.id },
      data: { state: "RELEASED", releasedAt: new Date() },
    });
  });
}

/** Admin releases a single hold early (e.g. delivery confirmed). */
export async function releaseEscrow(formData: FormData) {
  await requireAdmin();
  await creditSellerFromHold(String(formData.get("id")));
  revalidatePath("/admin");
  revalidatePath("/seller/payouts");
}

export async function requestPayout(formData: FormData) {
  const user = await requireUser();
  const seller = await prisma.sellerProfile.findUnique({
    where: { userId: user.id },
  });
  if (!seller) redirect("/sell");

  const amountCents = Math.round(Number(formData.get("amount") ?? 0) * 100);
  const method = String(formData.get("method") ?? "bkash");
  const wallet = await prisma.wallet.upsert({
    where: { userId: user.id },
    update: {},
    create: { userId: user.id },
  });
  if (amountCents <= 0 || amountCents > wallet.balanceCents) {
    throw new Error("Invalid payout amount");
  }

  await prisma.$transaction(async (tx) => {
    const balanceAfter = wallet.balanceCents - amountCents;
    await tx.wallet.update({
      where: { id: wallet.id },
      data: { balanceCents: balanceAfter },
    });
    const payout = await tx.payout.create({
      data: { sellerId: seller.id, amountCents, method, status: "REQUESTED" },
    });
    await tx.walletEntry.create({
      data: {
        walletId: wallet.id,
        type: "PAYOUT",
        amountCents,
        refType: "payout",
        refId: payout.id,
        balanceAfter,
      },
    });
  });
  revalidatePath("/seller/payouts");
}

export async function processPayout(formData: FormData) {
  await requireAdmin();
  await prisma.payout.update({
    where: { id: String(formData.get("id")) },
    data: { status: "PROCESSED", processedAt: new Date() },
  });
  revalidatePath("/admin");
}

// ── Disputes & refunds ──────────────────────────────

export async function openDispute(formData: FormData) {
  const user = await requireUser();
  const orderId = String(formData.get("orderId"));
  const reason = String(formData.get("reason") ?? "").trim() || "Not specified";
  const order = await prisma.order.findFirst({
    where: { id: orderId, buyerId: user.id },
  });
  if (!order) throw new Error("Order not found");

  await prisma.dispute.upsert({
    where: { orderId },
    update: { reason, status: "OPEN" },
    create: { orderId, openedBy: user.id, reason, status: "OPEN" },
  });
  await prisma.order.update({
    where: { id: orderId },
    data: { status: "DISPUTED" },
  });
  revalidatePath("/dashboard/orders");
}

export async function resolveDispute(formData: FormData) {
  await requireAdmin();
  const id = String(formData.get("id"));
  const refund = formData.get("decision") === "refund";
  const dispute = await prisma.dispute.findUnique({ where: { id } });
  if (!dispute) return;

  if (refund) {
    await prisma.$transaction(async (tx) => {
      // Reverse any escrow for this order; claw back already-released funds.
      const holds = await tx.escrowHold.findMany({
        where: { orderId: dispute.orderId },
      });
      for (const hold of holds) {
        if (hold.state === "RELEASED") {
          const seller = await tx.sellerProfile.findUnique({
            where: { id: hold.sellerId },
            select: { userId: true },
          });
          if (seller) {
            const wallet = await tx.wallet.findUnique({
              where: { userId: seller.userId },
            });
            if (wallet) {
              const balanceAfter = wallet.balanceCents - hold.amountCents;
              await tx.wallet.update({
                where: { id: wallet.id },
                data: { balanceCents: balanceAfter },
              });
              await tx.walletEntry.create({
                data: {
                  walletId: wallet.id,
                  type: "REFUND",
                  amountCents: hold.amountCents,
                  refType: "escrow",
                  refId: hold.id,
                  balanceAfter,
                },
              });
            }
          }
        }
        await tx.escrowHold.update({
          where: { id: hold.id },
          data: { state: "REFUNDED" },
        });
      }
      await tx.transaction.updateMany({
        where: { orderId: dispute.orderId },
        data: { status: "REFUNDED" },
      });
      await tx.order.update({
        where: { id: dispute.orderId },
        data: { status: "REFUNDED" },
      });
      await tx.dispute.update({
        where: { id },
        data: { status: "RESOLVED", resolution: "Refunded to buyer" },
      });
    });
  } else {
    await prisma.order.update({
      where: { id: dispute.orderId },
      data: { status: "COMPLETED" },
    });
    await prisma.dispute.update({
      where: { id },
      data: { status: "REJECTED", resolution: "Dispute rejected" },
    });
  }
  revalidatePath("/admin");
}
