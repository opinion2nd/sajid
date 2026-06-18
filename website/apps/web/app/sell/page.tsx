import { redirect } from "next/navigation";
import { Store, TrendingUp, ShieldCheck } from "lucide-react";
import prisma from "@brothercraft/db";
import { getCurrentUser } from "@/lib/session";
import { applyAsSeller } from "@/lib/actions";

export const metadata = { title: "Become a seller" };

export default async function SellPage() {
  const user = await getCurrentUser();
  if (user) {
    const seller = await prisma.sellerProfile.findUnique({
      where: { userId: user.id },
    });
    if (seller) redirect("/seller");
  }

  return (
    <div className="container-page py-12">
      <div className="mx-auto max-w-2xl text-center">
        <span className="grid mx-auto h-12 w-12 place-items-center rounded-xl bg-accent/15 text-accent">
          <Store className="h-6 w-6" />
        </span>
        <h1 className="mt-4 text-3xl font-bold">Sell on Brother Craft</h1>
        <p className="mt-2 text-muted">
          Reach thousands of server owners. List plugins, configs, builds and
          services — we handle payments, licensing and delivery.
        </p>
      </div>

      <div className="mx-auto mt-8 grid max-w-3xl gap-4 sm:grid-cols-3">
        {[
          { icon: <TrendingUp className="h-5 w-5" />, t: "Keep 90%", d: "Low 10% platform fee." },
          { icon: <ShieldCheck className="h-5 w-5" />, t: "License tools", d: "Auto-minted keys + validation API." },
          { icon: <Store className="h-5 w-5" />, t: "Your storefront", d: "A branded page for your products." },
        ].map((f) => (
          <div key={f.t} className="card p-4 text-center">
            <span className="grid mx-auto h-10 w-10 place-items-center rounded-lg bg-accent/15 text-accent">
              {f.icon}
            </span>
            <h3 className="mt-2 font-semibold">{f.t}</h3>
            <p className="text-sm text-muted">{f.d}</p>
          </div>
        ))}
      </div>

      {user ? (
        <form
          action={applyAsSeller}
          className="card mx-auto mt-8 max-w-md space-y-4 p-6"
        >
          <h2 className="text-lg font-semibold">Apply to sell</h2>
          <div>
            <label className="label">Store / display name</label>
            <input name="displayName" required minLength={3} className="input" />
          </div>
          <div>
            <label className="label">Short bio</label>
            <textarea name="bio" rows={3} className="input" />
          </div>
          <button className="btn-primary w-full">Submit application</button>
          <p className="text-center text-xs text-muted">
            Applications are reviewed by an admin before your store goes live.
          </p>
        </form>
      ) : (
        <div className="card mx-auto mt-8 max-w-md p-6 text-center">
          <p className="text-muted">Sign in to apply as a seller.</p>
          <a href="/login" className="btn-primary mt-4">
            Sign in
          </a>
        </div>
      )}
    </div>
  );
}
