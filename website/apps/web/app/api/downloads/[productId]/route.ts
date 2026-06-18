import { NextResponse } from "next/server";
import prisma from "@brothercraft/db";
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

  // If a real file asset exists and storage is configured, redirect to a signed URL.
  if (version?.file && process.env.STORAGE_DRIVER === "s3") {
    // Placeholder for S3/R2 signed-URL issuance.
    return NextResponse.redirect(
      `${process.env.STORAGE_ENDPOINT}/${version.file.storageKey}`
    );
  }

  // Demo artifact.
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
