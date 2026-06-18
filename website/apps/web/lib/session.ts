import { redirect } from "next/navigation";
import { auth } from "@/auth";

export async function getCurrentUser() {
  const session = await auth();
  return session?.user ?? null;
}

export async function requireUser() {
  const user = await getCurrentUser();
  if (!user) redirect("/login");
  return user;
}

export async function requireRole(role: "SELLER" | "ADMIN") {
  const user = await requireUser();
  if (role === "ADMIN" && user.role !== "ADMIN") redirect("/dashboard");
  if (role === "SELLER" && user.role === "BUYER") redirect("/sell");
  return user;
}
