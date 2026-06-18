import { NextRequest, NextResponse } from "next/server";
import { z } from "zod";
import { validateLicense } from "@brothercraft/license-core";

export const runtime = "nodejs";
export const dynamic = "force-dynamic";

// Request body matches the existing plugin client (LicenseValidator.java):
// { key, serverId, pluginVersion }. The response shape is preserved exactly.
const bodySchema = z.object({
  key: z.string().min(1),
  serverId: z.string().min(1),
  pluginVersion: z.string().optional(),
});

export async function POST(req: NextRequest) {
  let parsed;
  try {
    parsed = bodySchema.parse(await req.json());
  } catch {
    return NextResponse.json(
      {
        status: "INVALID",
        message: "Malformed request",
        expiresAt: null,
        boundServerId: null,
      },
      { status: 400 }
    );
  }

  const ipAddress =
    req.headers.get("x-forwarded-for")?.split(",")[0]?.trim() ?? "unknown";

  const result = await validateLicense(
    parsed.key,
    parsed.serverId,
    parsed.pluginVersion ?? "unknown",
    ipAddress
  );

  return NextResponse.json(result, { status: 200 });
}
