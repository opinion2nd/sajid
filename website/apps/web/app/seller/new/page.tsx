import { redirect } from "next/navigation";
import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import prisma from "@brothercraft/db";
import { requireUser } from "@/lib/session";
import { createProduct } from "@/lib/actions";

export const metadata = { title: "New product" };

export default async function NewProductPage() {
  const user = await requireUser();
  const seller = await prisma.sellerProfile.findUnique({
    where: { userId: user.id },
  });
  if (!seller) redirect("/sell");
  const categories = await prisma.category.findMany({ orderBy: { name: "asc" } });

  return (
    <div className="container-page py-8">
      <Link
        href="/seller"
        className="inline-flex items-center gap-1 text-sm text-muted hover:text-text"
      >
        <ArrowLeft className="h-4 w-4" /> Back to dashboard
      </Link>
      <h1 className="mt-3 text-2xl font-bold">Create a product</h1>

      <form action={createProduct} className="card mt-6 max-w-2xl space-y-4 p-6">
        <div>
          <label className="label">Title</label>
          <input name="title" required minLength={3} className="input" placeholder="e.g. Ultimate AntiCheat" />
        </div>
        <div>
          <label className="label">Short summary</label>
          <input name="summary" className="input" placeholder="One line shown on cards" />
        </div>
        <div>
          <label className="label">Description (Markdown)</label>
          <textarea name="description" rows={6} className="input font-mono text-xs" placeholder="## Features..." />
        </div>
        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <label className="label">Category</label>
            <select name="category" className="input" required>
              {categories.map((c) => (
                <option key={c.slug} value={c.slug}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">Price (৳ BDT)</label>
            <input name="price" type="number" min="0" step="1" defaultValue={0} className="input" />
          </div>
        </div>
        <div>
          <label className="label">Tags (comma separated)</label>
          <input name="tags" className="input" placeholder="anticheat, paper, 1.21" />
        </div>
        <div>
          <label className="label">Product file (optional)</label>
          <input
            name="file"
            type="file"
            className="input file:mr-3 file:rounded file:border-0 file:bg-accent file:px-3 file:py-1 file:text-accent-fg"
          />
          <p className="mt-1 text-xs text-muted">
            The .jar / .zip / .schem buyers download. Stored privately and served
            via a signed, entitlement-checked link.
          </p>
        </div>
        <label className="flex items-center gap-2 text-sm">
          <input name="licenseGated" type="checkbox" className="h-4 w-4" />
          License-gate this product (mint a key per purchase)
        </label>
        <button className="btn-primary w-full">Create product</button>
        <p className="text-center text-xs text-muted">
          {seller.status === "APPROVED"
            ? "Publishes immediately."
            : "Saved as pending review until your store is approved."}
        </p>
      </form>
    </div>
  );
}
