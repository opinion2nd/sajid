import Link from "next/link";
import { Star } from "lucide-react";
import { cn } from "@/lib/utils";

export function Badge({
  children,
  className,
}: {
  children: React.ReactNode;
  className?: string;
}) {
  return <span className={cn("badge", className)}>{children}</span>;
}

export function Card({
  children,
  className,
}: {
  children: React.ReactNode;
  className?: string;
}) {
  return <div className={cn("card p-4", className)}>{children}</div>;
}

export function Rating({ value, count }: { value: number; count?: number }) {
  return (
    <span className="inline-flex items-center gap-1 text-sm text-muted">
      <Star className="h-3.5 w-3.5 fill-yellow-400 text-yellow-400" />
      <span className="font-medium text-text">{value.toFixed(1)}</span>
      {count !== undefined && <span className="text-muted">({count})</span>}
    </span>
  );
}

export function StatusPill({ status }: { status: string }) {
  const map: Record<string, string> = {
    PUBLISHED: "border-accent/40 text-accent",
    APPROVED: "border-accent/40 text-accent",
    ACTIVE: "border-accent/40 text-accent",
    PAID: "border-accent/40 text-accent",
    COMPLETED: "border-accent/40 text-accent",
    PENDING: "border-yellow-500/40 text-yellow-400",
    PENDING_REVIEW: "border-yellow-500/40 text-yellow-400",
    DRAFT: "border-border text-muted",
    REJECTED: "border-red-500/40 text-red-400",
    REVOKED: "border-red-500/40 text-red-400",
    SUSPENDED: "border-red-500/40 text-red-400",
  };
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full border bg-surface px-2.5 py-0.5 text-xs font-medium",
        map[status] ?? "border-border text-muted"
      )}
    >
      {status.replace(/_/g, " ").toLowerCase()}
    </span>
  );
}

export function EmptyState({
  title,
  hint,
  action,
}: {
  title: string;
  hint?: string;
  action?: React.ReactNode;
}) {
  return (
    <div className="card flex flex-col items-center justify-center gap-2 p-10 text-center">
      <p className="text-lg font-semibold">{title}</p>
      {hint && <p className="max-w-sm text-sm text-muted">{hint}</p>}
      {action}
    </div>
  );
}

export function LinkButton({
  href,
  children,
  variant = "primary",
  className,
}: {
  href: string;
  children: React.ReactNode;
  variant?: "primary" | "ghost" | "brand";
  className?: string;
}) {
  const v =
    variant === "ghost"
      ? "btn-ghost"
      : variant === "brand"
        ? "btn-brand"
        : "btn-primary";
  return (
    <Link href={href} className={cn(v, className)}>
      {children}
    </Link>
  );
}
