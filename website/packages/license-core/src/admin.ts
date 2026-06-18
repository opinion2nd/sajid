import prisma from "@brothercraft/db";
import { generateKey } from "./keygen";

// Admin license operations — ported from anti-freecam/keygen-cli, now backed by
// the marketplace database and surfaced through the admin panel.

export async function mintLicense(params: {
  productId: string;
  orderId?: string;
  buyerId?: string;
  prefix?: string;
  maxActivations?: number;
  expiresAt?: Date | null;
  notes?: string;
}): Promise<string> {
  const key = generateKey(params.prefix ?? "BC");
  await prisma.license.create({
    data: {
      key,
      productId: params.productId,
      orderId: params.orderId ?? null,
      buyerId: params.buyerId ?? null,
      maxActivations: params.maxActivations ?? 1,
      expiresAt: params.expiresAt ?? null,
      notes: params.notes ?? null,
    },
  });
  return key;
}

export async function revokeLicense(key: string): Promise<void> {
  await prisma.license.update({
    where: { key },
    data: { status: "REVOKED", revokedAt: new Date() },
  });
}

/** Frees all server bindings so the key can re-activate elsewhere. */
export async function unbindLicense(key: string): Promise<void> {
  const license = await prisma.license.findUnique({ where: { key } });
  if (!license) return;
  await prisma.licenseActivation.deleteMany({
    where: { licenseId: license.id },
  });
}

export async function listLicenses(productId?: string) {
  return prisma.license.findMany({
    where: productId ? { productId } : undefined,
    include: { activations: true },
    orderBy: { createdAt: "desc" },
  });
}
