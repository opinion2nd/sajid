import type { Metadata } from "next";
import "./globals.css";
import { auth } from "@/auth";
import { CartProvider } from "@/components/cart";
import { Navbar } from "@/components/navbar";
import { Footer } from "@/components/footer";

export const metadata: Metadata = {
  metadataBase: new URL(process.env.APP_URL ?? "http://localhost:3000"),
  title: {
    default: "Brother Craft — Minecraft Marketplace",
    template: "%s · Brother Craft",
  },
  description:
    "Buy and sell Minecraft plugins, configs, builds and services. License-gated downloads, escrow-protected payments via bKash & Nagad.",
  icons: { icon: "/logo.jpg", apple: "/logo.jpg" },
  openGraph: {
    title: "Brother Craft — Minecraft Marketplace",
    description:
      "Buy and sell Minecraft plugins, configs, builds and services.",
    type: "website",
    images: ["/logo.jpg"],
  },
};

export default async function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const session = await auth();
  const sessionInfo = session?.user
    ? { handle: session.user.handle, role: session.user.role }
    : null;

  return (
    <html lang="en">
      <body className="flex min-h-screen flex-col">
        <CartProvider>
          <Navbar session={sessionInfo} />
          <main className="flex-1">{children}</main>
          <Footer />
        </CartProvider>
      </body>
    </html>
  );
}
