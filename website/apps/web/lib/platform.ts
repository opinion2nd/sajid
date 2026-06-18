import prisma from "@brothercraft/db";

/** Total platform fee (10% of sales) collected for the marketplace owner. */
export async function getPlatformEarnings(): Promise<number> {
  const agg = await prisma.walletEntry.aggregate({
    where: { type: "CREDIT", refType: "platform_fee" },
    _sum: { amountCents: true },
  });
  return agg._sum.amountCents ?? 0;
}

export function getPlatformPayout() {
  return {
    feePercent: Number(process.env.PLATFORM_FEE_PERCENT ?? 10),
    bkash: process.env.PLATFORM_BKASH_NUMBER ?? "01811799277",
    nagad: process.env.PLATFORM_NAGAD_NUMBER ?? "01811799277",
  };
}
