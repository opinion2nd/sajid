import { NextRequest, NextResponse } from "next/server";
import { exchangeCodeForToken, fetchDiscordUser, getManageableGuilds } from "@/lib/discord";
import { setSessionCookie } from "@/lib/session";

export async function GET(request: NextRequest) {
  const code = request.nextUrl.searchParams.get("code");
  if (!code) {
    return NextResponse.redirect(new URL("/?error=missing_code", request.url));
  }

  try {
    const accessToken = await exchangeCodeForToken(code);
    const [user, guilds] = await Promise.all([fetchDiscordUser(accessToken), getManageableGuilds(accessToken)]);

    await setSessionCookie({
      userId: user.id,
      username: user.username,
      avatar: user.avatar,
      guilds,
      expiresAt: Date.now() + 1000 * 60 * 60 * 24 * 7,
    });

    return NextResponse.redirect(new URL("/dashboard", request.url));
  } catch (error) {
    console.error("OAuth callback failed:", error);
    return NextResponse.redirect(new URL("/?error=auth_failed", request.url));
  }
}
