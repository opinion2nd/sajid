import { NextResponse } from "next/server";
import { clearSessionCookie } from "@/lib/session";

export async function GET() {
  await clearSessionCookie();
  const origin = new URL(process.env.DISCORD_REDIRECT_URI!).origin;
  return NextResponse.redirect(new URL("/", origin));
}
