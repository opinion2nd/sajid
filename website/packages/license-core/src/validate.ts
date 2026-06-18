import prisma from "@brothercraft/db";

// Wire contract preserved from anti-freecam/license-server so existing
// plugin clients (LicenseValidator.java / ServerIdentity.java) keep working
// with ZERO changes. Do not alter the shape of ValidateResult.
export type ValidateStatus = "VALID" | "INVALID" | "MISMATCH" | "EXPIRED";

export type ValidateResult = {
  status: ValidateStatus;
  message: string;
  expiresAt: string | null;
  boundServerId: string | null;
};

/**
 * Validates a license key for a given server.
 *
 * Generalizes the original single-server `serverId` bind into N activations
 * capped by `License.maxActivations`:
 *   - revoked  -> INVALID
 *   - expired  -> EXPIRED
 *   - server already activated -> VALID (refresh lastValidAt)
 *   - new server, under cap     -> VALID (create activation = bind-on-first-use)
 *   - new server, over cap      -> MISMATCH
 */
export async function validateLicense(
  key: string,
  serverId: string,
  pluginVersion: string,
  ipAddress: string
): Promise<ValidateResult> {
  const license = await prisma.license.findUnique({
    where: { key },
    include: { activations: true },
  });

  if (!license) {
    return {
      status: "INVALID",
      message: "License key not found",
      expiresAt: null,
      boundServerId: null,
    };
  }

  if (license.status === "REVOKED" || license.revokedAt) {
    await logAudit(license.id, "VALIDATE_FAIL_REVOKED", serverId, ipAddress);
    return {
      status: "INVALID",
      message: "License key has been revoked",
      expiresAt: null,
      boundServerId: null,
    };
  }

  if (license.expiresAt && license.expiresAt < new Date()) {
    await logAudit(license.id, "VALIDATE_FAIL_EXPIRED", serverId, ipAddress);
    return {
      status: "EXPIRED",
      message: "License key has expired",
      expiresAt: license.expiresAt.toISOString(),
      boundServerId: license.activations[0]?.serverId ?? null,
    };
  }

  const existing = license.activations.find((a) => a.serverId === serverId);
  const expiresAt = license.expiresAt?.toISOString() ?? null;

  if (existing) {
    await prisma.licenseActivation.update({
      where: { id: existing.id },
      data: { lastValidAt: new Date(), pluginVersion, ipAddress },
    });
    await prisma.license.update({
      where: { id: license.id },
      data: { lastValidAt: new Date() },
    });
    await logAudit(license.id, "VALIDATE_SUCCESS", serverId, ipAddress);
    return {
      status: "VALID",
      message: "License valid",
      expiresAt,
      boundServerId: serverId,
    };
  }

  // New server — bind if there is an activation slot free.
  if (license.activations.length >= license.maxActivations) {
    await logAudit(license.id, "VALIDATE_FAIL_MISMATCH", serverId, ipAddress);
    return {
      status: "MISMATCH",
      message: "License activation limit reached for other servers",
      expiresAt: null,
      boundServerId: license.activations[0]?.serverId ?? null,
    };
  }

  await prisma.licenseActivation.create({
    data: { licenseId: license.id, serverId, pluginVersion, ipAddress },
  });
  await prisma.license.update({
    where: { id: license.id },
    data: { lastValidAt: new Date(), pluginVersion },
  });
  await logAudit(license.id, "VALIDATE_BOUND", serverId, ipAddress);
  return {
    status: "VALID",
    message: "License activated and bound to this server",
    expiresAt,
    boundServerId: serverId,
  };
}

async function logAudit(
  licenseId: string,
  event: string,
  serverId: string,
  ipAddress: string
): Promise<void> {
  await prisma.auditLog.create({
    data: { licenseId, event, serverId, ipAddress },
  });
}
