"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState } from "react";
import {
  Blocks,
  Menu,
  X,
  ShoppingCart,
  Search,
  LayoutDashboard,
  Store,
  Shield,
  LogOut,
} from "lucide-react";
import { useCart } from "./cart";
import { cn } from "@/lib/utils";

type SessionInfo = {
  handle: string;
  role: "BUYER" | "SELLER" | "ADMIN";
} | null;

const navLinks = [
  { href: "/browse", label: "Browse" },
  { href: "/sell", label: "Sell" },
  { href: "/docs/license-api", label: "Docs" },
];

export function Navbar({ session }: { session: SessionInfo }) {
  const [open, setOpen] = useState(false);
  const pathname = usePathname();
  const { items } = useCart();

  return (
    <header className="sticky top-0 z-40 border-b border-border bg-bg/80 backdrop-blur">
      <nav className="container-page flex h-16 items-center justify-between gap-4">
        <Link href="/" className="flex shrink-0 items-center gap-2">
          <span className="grid h-9 w-9 place-items-center rounded-lg bg-accent text-accent-fg">
            <Blocks className="h-5 w-5" />
          </span>
          <span className="text-lg font-bold tracking-tight">
            Brother<span className="text-accent">Craft</span>
          </span>
        </Link>

        {/* Desktop links */}
        <div className="hidden items-center gap-1 md:flex">
          {navLinks.map((l) => (
            <Link
              key={l.href}
              href={l.href}
              className={cn(
                "rounded-lg px-3 py-2 text-sm font-medium text-muted transition hover:bg-panel hover:text-text",
                pathname.startsWith(l.href) && "text-text"
              )}
            >
              {l.label}
            </Link>
          ))}
        </div>

        <div className="flex items-center gap-2">
          <Link
            href="/browse"
            className="hidden rounded-lg border border-border bg-surface p-2 text-muted hover:text-text sm:block"
            aria-label="Search"
          >
            <Search className="h-4 w-4" />
          </Link>
          <Link
            href="/cart"
            className="relative rounded-lg border border-border bg-surface p-2 text-muted hover:text-text"
            aria-label="Cart"
          >
            <ShoppingCart className="h-4 w-4" />
            {items.length > 0 && (
              <span className="absolute -right-1.5 -top-1.5 grid h-4 min-w-4 place-items-center rounded-full bg-accent px-1 text-[10px] font-bold text-accent-fg">
                {items.length}
              </span>
            )}
          </Link>

          {session ? (
            <Link href="/dashboard" className="btn-ghost hidden sm:inline-flex">
              {session.handle}
            </Link>
          ) : (
            <Link href="/login" className="btn-primary hidden sm:inline-flex">
              Sign in
            </Link>
          )}

          <button
            onClick={() => setOpen((o) => !o)}
            className="rounded-lg border border-border bg-surface p-2 text-muted md:hidden"
            aria-label="Menu"
          >
            {open ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </button>
        </div>
      </nav>

      {/* Mobile drawer */}
      {open && (
        <div className="border-t border-border bg-surface md:hidden">
          <div className="container-page flex flex-col gap-1 py-3">
            {navLinks.map((l) => (
              <Link
                key={l.href}
                href={l.href}
                onClick={() => setOpen(false)}
                className="rounded-lg px-3 py-2.5 text-sm font-medium text-muted hover:bg-panel hover:text-text"
              >
                {l.label}
              </Link>
            ))}
            <div className="my-1 h-px bg-border" />
            {session ? (
              <>
                <MobileLink href="/dashboard" icon={<LayoutDashboard className="h-4 w-4" />} onClick={() => setOpen(false)}>
                  Dashboard
                </MobileLink>
                <MobileLink href="/seller" icon={<Store className="h-4 w-4" />} onClick={() => setOpen(false)}>
                  Seller area
                </MobileLink>
                {session.role === "ADMIN" && (
                  <MobileLink href="/admin" icon={<Shield className="h-4 w-4" />} onClick={() => setOpen(false)}>
                    Admin
                  </MobileLink>
                )}
                <MobileLink href="/api/auth/signout" icon={<LogOut className="h-4 w-4" />} onClick={() => setOpen(false)}>
                  Sign out
                </MobileLink>
              </>
            ) : (
              <Link
                href="/login"
                onClick={() => setOpen(false)}
                className="btn-primary mt-1"
              >
                Sign in
              </Link>
            )}
          </div>
        </div>
      )}
    </header>
  );
}

function MobileLink({
  href,
  icon,
  children,
  onClick,
}: {
  href: string;
  icon: React.ReactNode;
  children: React.ReactNode;
  onClick: () => void;
}) {
  return (
    <Link
      href={href}
      onClick={onClick}
      className="flex items-center gap-2 rounded-lg px-3 py-2.5 text-sm font-medium text-muted hover:bg-panel hover:text-text"
    >
      {icon}
      {children}
    </Link>
  );
}
