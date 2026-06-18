"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";

export type NavItem = { href: string; label: string };

export function SideNav({ items, title }: { items: NavItem[]; title: string }) {
  const pathname = usePathname();
  return (
    <nav className="lg:sticky lg:top-20 lg:h-fit">
      <p className="mb-2 px-3 text-xs font-semibold uppercase tracking-wide text-muted">
        {title}
      </p>
      <div className="flex gap-1 overflow-x-auto lg:flex-col">
        {items.map((it) => {
          const active =
            pathname === it.href ||
            (it.href !== "/dashboard" &&
              it.href !== "/seller" &&
              it.href !== "/admin" &&
              pathname.startsWith(it.href));
          return (
            <Link
              key={it.href}
              href={it.href}
              className={cn(
                "whitespace-nowrap rounded-lg px-3 py-2 text-sm font-medium text-muted transition hover:bg-panel hover:text-text",
                active && "bg-panel text-text"
              )}
            >
              {it.label}
            </Link>
          );
        })}
      </div>
    </nav>
  );
}
