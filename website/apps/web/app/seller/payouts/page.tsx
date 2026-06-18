import { redirect } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, Wallet, Clock } from "lucide-react";
import prisma from "@brothercraft/db";
import { requireUser } from "@/lib/session";
import { getSellerWallet } from "@/lib/wallet";
import { requestPayout } from "@/lib/actions";
import { formatPrice, timeAgo } from "@/lib/utils";
import { StatusPill, EmptyState } from "@/components/ui";

export const metadata = { title: "Payouts" };

export default async function PayoutsPage() {
  const user = await requireUser();
  const seller = await prisma.sellerProfile.findUnique({
    where: { userId: user.id },
  });
  if (!seller) redirect("/sell");

  const [wallet, payouts] = await Promise.all([
    getSellerWallet(user.id, seller.id),
    prisma.payout.findMany({
      where: { sellerId: seller.id },
      orderBy: { requestedAt: "desc" },
    }),
  ]);

  return (
    <div className="container-page py-8">
      <Link
        href="/seller"
        className="inline-flex items-center gap-1 text-sm text-muted hover:text-text"
      >
        <ArrowLeft className="h-4 w-4" /> Back to dashboard
      </Link>
      <h1 className="mt-3 text-2xl font-bold">Payouts</h1>

      <div className="mt-6 grid gap-4 sm:grid-cols-2">
        <div className="card p-5">
          <span className="grid h-10 w-10 place-items-center rounded-lg bg-accent/15 text-accent">
            <Wallet className="h-5 w-5" />
          </span>
          <p className="mt-3 text-2xl font-bold">
            {formatPrice(wallet.availableCents)}
          </p>
          <p className="text-sm text-muted">Available to withdraw</p>
        </div>
        <div className="card p-5">
          <span className="grid h-10 w-10 place-items-center rounded-lg bg-yellow-500/15 text-yellow-400">
            <Clock className="h-5 w-5" />
          </span>
          <p className="mt-3 text-2xl font-bold">
            {formatPrice(wallet.pendingCents)}
          </p>
          <p className="text-sm text-muted">In escrow (clears after delivery)</p>
        </div>
      </div>

      {/* Request payout */}
      <form
        action={requestPayout}
        className="card mt-6 max-w-md space-y-4 p-6"
      >
        <h2 className="text-lg font-semibold">Request a payout</h2>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="label">Amount (৳)</label>
            <input
              name="amount"
              type="number"
              min="1"
              step="1"
              max={wallet.availableCents / 100}
              className="input"
              required
            />
          </div>
          <div>
            <label className="label">Method</label>
            <select name="method" className="input">
              <option value="bkash">bKash</option>
              <option value="nagad">Nagad</option>
            </select>
          </div>
        </div>
        <button
          className="btn-primary w-full"
          disabled={wallet.availableCents <= 0}
        >
          Request payout
        </button>
      </form>

      <h2 className="mt-8 text-lg font-bold">Payout history</h2>
      <div className="mt-3 space-y-2">
        {payouts.length === 0 && (
          <EmptyState title="No payouts yet" hint="Requested payouts appear here." />
        )}
        {payouts.map((p) => (
          <div
            key={p.id}
            className="card flex items-center justify-between p-4"
          >
            <div>
              <p className="font-medium">{formatPrice(p.amountCents)}</p>
              <p className="text-sm text-muted">
                {p.method} · {timeAgo(p.requestedAt)}
              </p>
            </div>
            <StatusPill status={p.status} />
          </div>
        ))}
      </div>
    </div>
  );
}
