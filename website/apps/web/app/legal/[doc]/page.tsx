import { notFound } from "next/navigation";
import type { Metadata } from "next";

const docs: Record<string, { title: string; body: string[] }> = {
  terms: {
    title: "Terms of Service",
    body: [
      "Welcome to Brother Craft. By using our marketplace you agree to these terms.",
      "## Accounts",
      "You are responsible for activity on your account and for keeping your credentials secure.",
      "## Purchases",
      "Digital products are licensed, not sold. Payment is processed via bKash, Nagad and supported gateways. Funds for sellers are held in escrow until the delivery window passes.",
      "## Seller obligations",
      "Sellers must own the rights to everything they list and must not upload malware or infringing content. We may delist products and suspend stores that violate these terms.",
      "## Liability",
      "The service is provided “as is”. Brother Craft is not liable for indirect damages arising from use of the marketplace.",
    ],
  },
  privacy: {
    title: "Privacy Policy",
    body: [
      "This policy explains what we collect and why.",
      "## Data we collect",
      "Account details (email, username), order history, and license activation metadata (server identifier, IP) needed to operate license validation.",
      "## How we use it",
      "To process orders, deliver downloads, validate licenses, prevent fraud, and provide support. We do not sell your personal data.",
      "## Discord",
      "If you link Discord, we store your Discord ID to sync roles and send notifications. You can unlink at any time.",
      "## Contact",
      "For privacy requests, contact support through your dashboard.",
    ],
  },
  refund: {
    title: "Refund Policy",
    body: [
      "Because products are digital and delivered instantly, refunds are limited.",
      "## Eligible refunds",
      "You may request a refund within 14 days if a product is broken, materially not as described, or never delivered, and the seller cannot resolve the issue.",
      "## Not eligible",
      "Change of mind after download, or incompatibility you could reasonably have checked before buying.",
      "## Process",
      "Open a dispute from your order. Funds held in escrow are returned to you if the dispute is resolved in your favour.",
    ],
  },
};

export async function generateMetadata({
  params,
}: {
  params: Promise<{ doc: string }>;
}): Promise<Metadata> {
  const { doc } = await params;
  return { title: docs[doc]?.title ?? "Legal" };
}

export default async function LegalPage({
  params,
}: {
  params: Promise<{ doc: string }>;
}) {
  const { doc } = await params;
  const content = docs[doc];
  if (!content) notFound();

  return (
    <div className="container-page max-w-3xl py-12">
      <h1 className="text-3xl font-bold">{content.title}</h1>
      <p className="mt-1 text-sm text-muted">Last updated June 2026</p>
      <article className="prose-mc mt-6">
        {content.body.map((line, i) =>
          line.startsWith("## ") ? (
            <h2 key={i}>{line.slice(3)}</h2>
          ) : (
            <p key={i}>{line}</p>
          )
        )}
      </article>
    </div>
  );
}
