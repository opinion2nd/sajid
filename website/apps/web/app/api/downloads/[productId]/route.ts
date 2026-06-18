import { NextResponse } from "next/server";
import prisma from "@brothercraft/db";
import { getStorage } from "@brothercraft/storage";
import { getCurrentUser } from "@/lib/session";
import { hasPurchased } from "@/lib/orders";

export const runtime = "nodejs";
export const dynamic = "force-dynamic";

// Entitlement-gated download. In production this issues a short-lived signed URL
// to private object storage. In demo mode (no storage configured) we generate a
// placeholder artifact so the end-to-end flow is verifiable.
export async function GET(
  _req: Request,
  { params }: { params: Promise<{ productId: string }> }
) {
  const user = await getCurrentUser();
  if (!user) {
    return NextResponse.json({ error: "Not signed in" }, { status: 401 });
  }
  const { productId } = await params;

  const owned = await hasPurchased(user.id, productId);
  if (!owned) {
    return NextResponse.json(
      { error: "You have not purchased this product" },
      { status: 403 }
    );
  }

  const product = await prisma.product.findUnique({
    where: { id: productId },
    include: {
      versions: { orderBy: { createdAt: "desc" }, take: 1, include: { file: true } },
    },
  });
  if (!product) {
    return NextResponse.json({ error: "Not found" }, { status: 404 });
  }

  const version = product.versions[0];

  // Real uploaded file: serve via storage driver.
  if (version?.file) {
    const storage = getStorage();
    // S3/R2 → short-lived signed URL (60s).
    const signed = await storage.signedUrl(version.file.storageKey, 60);
    if (signed) return NextResponse.redirect(signed);
    // Local driver → stream the bytes through this entitlement-checked route.
    const bytes = await storage.read(version.file.storageKey);
    if (bytes) {
      const name = version.file.storageKey.split("/").pop() ?? "download";
      return new NextResponse(new Uint8Array(bytes), {
        headers: {
          "Content-Type": version.file.contentType,
          "Content-Disposition": `attachment; filename="${name}"`,
        },
      });
    }
  }

  // Demo artifact (no file uploaded).
  const license = await prisma.license.findFirst({
    where: { productId, buyerId: user.id },
    select: { key: true },
  });
  const body = [
    `Brother Craft — ${product.title}`,
    `Version: ${version?.semver ?? "1.0.0"}`,
    `Purchased by: ${user.handle}`,
    license ? `License key: ${license.key}` : "",
    "",
    "This is a demo artifact. In production this endpoint streams your purchased",
    "file from private object storage via a short-lived signed URL.",
    "",
    product.licenseGated
      ? "Activate your plugin by validating the key above against:"
      : "",
    product.licenseGated ? "POST /api/v1/license/validate" : "",
  ].join("\n");

  const filename = `${product.slug}-${version?.semver ?? "1.0.0"}.txt`;
  return new NextResponse(body, {
    headers: {
      "Content-Type": "text/plain; charset=utf-8",
      "Content-Disposition": `attachment; filename="${filename}"`,
    },
  });
}
