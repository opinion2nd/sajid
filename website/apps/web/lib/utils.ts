import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/** Format integer cents (BDT) as a Taka string. */
export function formatPrice(cents: number, currency = "BDT"): string {
  if (cents === 0) return "Free";
  const amount = cents / 100;
  if (currency === "BDT") {
    return `৳${amount.toLocaleString("en-US", { maximumFractionDigits: 2 })}`;
  }
  return `${currency} ${amount.toFixed(2)}`;
}

export function timeAgo(date: Date | string): string {
  const d = typeof date === "string" ? new Date(date) : date;
  const secs = Math.floor((Date.now() - d.getTime()) / 1000);
  const units: [number, string][] = [
    [60, "s"],
    [60, "m"],
    [24, "h"],
    [30, "d"],
    [12, "mo"],
    [Number.POSITIVE_INFINITY, "y"],
  ];
  let value = secs;
  let unit = "s";
  let divisor = 1;
  for (const [step, label] of units) {
    if (value < step) {
      unit = label;
      break;
    }
    divisor *= step;
    value = Math.floor(secs / divisor);
    unit = label;
  }
  return value <= 0 ? "just now" : `${value}${unit} ago`;
}
