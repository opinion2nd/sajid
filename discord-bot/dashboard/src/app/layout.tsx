import type { ReactNode } from "react";
import "./globals.css";

export const metadata = {
  title: "Flaming Bot Dashboard",
  description: "Manage your Discord server's bot configuration",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
