import { NextResponse } from "next/server";
import { randomBytes } from "crypto";
import prisma from "@brothercraft/db";
import { getCurrentUser } from "@/lib/session";

export const runtime = "nodejs";

// Issues a one-time code the user types into Discord via `/link <code>`.
export async function POST() {
  const user = await getCurrentUser();
  if (!user) return NextResponse.json({ error: "Not signed in" }, { status: 401 });

  // 6-char unambiguous code.
  const alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  const code = Array.from(randomBytes(6))
    .map((b) => alphabet[b % alphabet.length])
    .join("");

  await prisma.discordLinkCode.create({
    data: {
      code,
      userId: user.id,
      expiresAt: new Date(Date.now() + 10 * 60 * 1000),
    },
  });

  return NextResponse.json({ code, expiresInMinutes: 10 });
}
