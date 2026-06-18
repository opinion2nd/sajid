import prisma from "@brothercraft/db";
import { mintLicense } from "@brothercraft/license-core";
import { sendEmail, receiptEmail } from "@brothercraft/email";
import { formatPrice } from "@/lib/utils";

// Marketplace fee in basis points (default 10%). Configurable via env.
const PLATFORM_FEE_BPS =
  Math.round(Number(process.env.PLATFORM_FEE_PERCENT ?? 10) * 100) || 1000;

export type CheckoutItem = { productId: string };

/** Creates a PENDING order + transaction for the given products. */
export async function createOrder(
  buyerId: string,
  items: CheckoutItem[],
  provider: "MOCK" | "SSLCOMMERZ"
) {
  const products = await prisma.product.findMany({
    where: { id: { in: items.map((i) => i.productId) }, status: "PUBLISHED" },
    select: { id: true, priceCents: true, sellerId: true },
  });
  if (products.length === 0) throw new Error("No valid products in cart");

  const subtotal = products.reduce((s, p) => s + p.priceCents, 0);

  return prisma.order.create({
    data: {
      buyerId,
      status: "PENDING",
      subtotalCents: subtotal,
      totalCents: subtotal,
      items: {
        create: products.map((p) => ({
          productId: p.id,
          sellerId: p.sellerId,
          unitPriceCents: p.priceCents,
        })),
      },
      transaction: {
        create: {
          provider: provider === "SSLCOMMERZ" ? "SSLCOMMERZ" : "WALLET",
          amountCents: subtotal,
          status: "PENDING",
        },
      },
    },
    include: { items: true },
  });
}

/**
 * Fulfils a paid order. Idempotent: a duplicate webhook for the same providerRef
 * is a no-op. Mints licenses for license-gated products, holds seller funds in
 * escrow, and credits the seller wallet ledger (HOLD entry).
 */
export async function fulfillOrder(orderId: string, providerRef: string) {
  const result = await prisma.$transaction(async (tx) => {
    // Idempotency guard.
    const seen = await tx.webhookEvent.findUnique({ where: { providerRef } });
    if (seen) return { alreadyProcessed: true };

    const order = await tx.order.findUnique({
      where: { id: orderId },
      include: { items: { include: { product: true } } },
    });
    if (!order) throw new Error("Order not found");
    if (order.status !== "PENDING") {
      await tx.webhookEvent.create({
        data: { provider: "payment", providerRef, payload: { orderId } },
      });
      return { alreadyProcessed: true };
    }

    // Mark transaction + order paid. Digital goods auto-complete so downloads
    // are immediately available; funds still pass through escrow for the seller.
    await tx.transaction.update({
      where: { orderId },
      data: { status: "VERIFIED", providerRef, verifiedAt: new Date() },
    });
    await tx.order.update({
      where: { id: orderId },
      data: { status: "COMPLETED", paidAt: new Date() },
    });

    // Per-seller escrow holds + wallet ledger credit (held).
    const bySeller = new Map<string, number>();
    for (const item of order.items) {
      bySeller.set(
        item.sellerId,
        (bySeller.get(item.sellerId) ?? 0) + item.unitPriceCents
      );
    }
    let platformFeeTotal = 0;
    for (const [sellerId, gross] of bySeller) {
      const fee = Math.round((gross * PLATFORM_FEE_BPS) / 10000);
      const net = gross - fee;
      platformFeeTotal += fee;
      await tx.escrowHold.create({
        data: {
          orderId,
          sellerId,
          amountCents: net,
          state: "HELD",
          holdUntil: new Date(Date.now() + 72 * 3600 * 1000),
        },
      });
      const sellerProfile = await tx.sellerProfile.findUnique({
        where: { id: sellerId },
        select: { userId: true },
      });
      if (sellerProfile) {
        const wallet = await tx.wallet.upsert({
          where: { userId: sellerProfile.userId },
          update: {},
          create: { userId: sellerProfile.userId },
        });
        await tx.walletEntry.create({
          data: {
            walletId: wallet.id,
            type: "HOLD",
            amountCents: net,
            refType: "order",
            refId: orderId,
            balanceAfter: wallet.balanceCents,
          },
        });
        await tx.sellerProfile.update({
          where: { id: sellerId },
          data: { salesCount: { increment: 1 } },
        });
      }
    }

    // Platform's 10% fee → the marketplace owner's wallet (paid out to the
    // configured bKash/Nagad number). Tracked as CREDIT entries on the first
    // ADMIN account so the admin dashboard can total platform earnings.
    if (platformFeeTotal > 0) {
      const admin = await tx.user.findFirst({
        where: { role: "ADMIN" },
        select: { id: true },
      });
      if (admin) {
        const wallet = await tx.wallet.upsert({
          where: { userId: admin.id },
          update: { balanceCents: { increment: platformFeeTotal } },
          create: { userId: admin.id, balanceCents: platformFeeTotal },
        });
        await tx.walletEntry.create({
          data: {
            walletId: wallet.id,
            type: "CREDIT",
            amountCents: platformFeeTotal,
            refType: "platform_fee",
            refId: orderId,
            balanceAfter: wallet.balanceCents,
          },
        });
      }
    }

    // Mint licenses for license-gated products + bump download counters.
    for (const item of order.items) {
      if (item.product.licenseGated) {
        await mintLicense({
          productId: item.productId,
          orderId,
          buyerId: order.buyerId,
          prefix: item.product.licensePrefix,
          maxActivations: item.product.maxActivations,
        });
      }
      await tx.product.update({
        where: { id: item.productId },
        data: { downloadsCount: { increment: 1 } },
      });
    }

    await tx.webhookEvent.create({
      data: { provider: "payment", providerRef, payload: { orderId } },
    });

    return { alreadyProcessed: false };
  });

  // Send the receipt + license keys after the transaction commits.
  if (!result.alreadyProcessed) {
    await sendReceipt(orderId).catch((e) =>
      console.error("[email] receipt failed", e)
    );
  }
  return result;
}

async function sendReceipt(orderId: string) {
  const order = await prisma.order.findUnique({
    where: { id: orderId },
    include: {
      buyer: { select: { email: true, handle: true } },
      items: { include: { product: { select: { title: true } } } },
      licenses: { include: { product: { select: { title: true } } } },
    },
  });
  if (!order?.buyer?.email) return;
  const { subject, html } = receiptEmail({
    buyerName: order.buyer.handle,
    items: order.items.map((i) => ({
      title: i.product.title,
      priceLabel: formatPrice(i.unitPriceCents),
    })),
    totalLabel: formatPrice(order.totalCents),
    licenses: order.licenses.map((l) => ({
      title: l.product.title,
      key: l.key,
    })),
  });
  await sendEmail({ to: order.buyer.email, subject, html });
}

/** Has this buyer completed an order containing this product? */
export async function hasPurchased(buyerId: string, productId: string) {
  const item = await prisma.orderItem.findFirst({
    where: {
      productId,
      order: { buyerId, status: { in: ["PAID", "COMPLETED", "ESCROW_HELD"] } },
    },
  });
  return Boolean(item);
}
