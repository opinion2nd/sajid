import Link from "next/link";
import { ShoppingBag, KeyRound, Download, Wallet } from "lucide-react";
import prisma from "@brothercraft/db";
import { requireUser } from "@/lib/session";
import { formatPrice } from "@/lib/utils";

export default async function DashboardOverview() {
  const user = await requireUser();

  const [orderCount, licenseCount, completedSpend] = await Promise.all([
    prisma.order.count({ where: { buyerId: user.id } }),
    prisma.license.count({ where: { buyerId: user.id } }),
    prisma.order.aggregate({
      where: { buyerId: user.id, status: "COMPLETED" },
      _sum: { totalCents: true },
    }),
  ]);

  const stats = [
    {
      icon: <ShoppingBag className="h-5 w-5" />,
      label: "Orders",
      value: orderCount,
    },
    {
      icon: <KeyRound className="h-5 w-5" />,
      label: "License keys",
      value: licenseCount,
    },
    {
      icon: <Wallet className="h-5 w-5" />,
      label: "Total spent",
      value: formatPrice(completedSpend._sum.totalCents ?? 0),
    },
  ];

  return (
    <div>
      <h1 className="text-2xl font-bold">Welcome, {user.handle}</h1>
      <p className="mt-1 text-muted">Here&apos;s your account at a glance.</p>

      <div className="mt-6 grid gap-4 sm:grid-cols-3">
        {stats.map((s) => (
          <div key={s.label} className="card p-5">
            <span className="grid h-10 w-10 place-items-center rounded-lg bg-accent/15 text-accent">
              {s.icon}
            </span>
            <p className="mt-3 text-2xl font-bold">{s.value}</p>
            <p className="text-sm text-muted">{s.label}</p>
          </div>
        ))}
      </div>

      <div className="mt-6 flex flex-col gap-2 sm:flex-row">
        <Link href="/dashboard/downloads" className="btn-primary">
          <Download className="h-4 w-4" /> View downloads
        </Link>
        <Link href="/browse" className="btn-ghost">
          Browse marketplace
        </Link>
      </div>
    </div>
  );
}
