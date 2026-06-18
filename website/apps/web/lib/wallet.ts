import prisma from "@brothercraft/db";

export type SellerWallet = {
  walletId: string;
  availableCents: number;
  pendingCents: number;
};

/** Available (withdrawable) balance + funds still held in escrow for a seller. */
export async function getSellerWallet(
  userId: string,
  sellerId: string
): Promise<SellerWallet> {
  const wallet = await prisma.wallet.upsert({
    where: { userId },
    update: {},
    create: { userId },
  });
  const held = await prisma.escrowHold.aggregate({
    where: { sellerId, state: "HELD" },
    _sum: { amountCents: true },
  });
  return {
    walletId: wallet.id,
    availableCents: wallet.balanceCents,
    pendingCents: held._sum.amountCents ?? 0,
  };
}
