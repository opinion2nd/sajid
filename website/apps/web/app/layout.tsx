import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: {
    default: "Brother Craft — Minecraft Marketplace",
    template: "%s · Brother Craft",
  },
  description:
    "Buy and sell Minecraft plugins, configs, builds and services. License-gated downloads, escrow-protected payments via bKash & Nagad.",
  openGraph: {
    title: "Brother Craft — Minecraft Marketplace",
    description:
      "Buy and sell Minecraft plugins, configs, builds and services.",
    type: "website",
  },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
