import { randomBytes } from 'crypto';
import prisma from '../db/client';

const BASE32_CHARS = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'; // no 0,O,1,I

function generateKey(): string {
  const bytes = randomBytes(10); // 80 bits
  let result = '';
  let bits = 0;
  let bitBuf = 0;

  for (const byte of bytes) {
    bitBuf = (bitBuf << 8) | byte;
    bits += 8;
    while (bits >= 5) {
      bits -= 5;
      result += BASE32_CHARS[(bitBuf >> bits) & 0x1f];
    }
  }
  if (bits > 0) {
    result += BASE32_CHARS[(bitBuf << (5 - bits)) & 0x1f];
  }

  // Format as AFC-XXXX-XXXX-XXXX-XXXX (16 chars = 4 groups of 4)
  const trimmed = result.slice(0, 16).padEnd(16, 'A');
  return `AFC-${trimmed.slice(0, 4)}-${trimmed.slice(4, 8)}-${trimmed.slice(8, 12)}-${trimmed.slice(12, 16)}`;
}

export type ValidateResult = {
  status: 'VALID' | 'INVALID' | 'MISMATCH' | 'EXPIRED';
  message: string;
  expiresAt: string | null;
  boundServerId: string | null;
};

export const LicenseService = {
  async validate(key: string, product: string, serverId: string, pluginVersion: string, ipAddress: string): Promise<ValidateResult> {
    const license = await prisma.license.findUnique({ where: { key } });

    if (!license) {
      await logAudit(null, 'VALIDATE_FAIL_NOT_FOUND', serverId, ipAddress);
      return { status: 'INVALID', message: 'License key not found', expiresAt: null, boundServerId: null };
    }

    if (license.product !== product) {
      await logAudit(license.id, 'VALIDATE_FAIL_PRODUCT_MISMATCH', serverId, ipAddress);
      return { status: 'INVALID', message: 'License key is not valid for this product', expiresAt: null, boundServerId: null };
    }

    if (license.revokedAt) {
      await logAudit(license.id, 'VALIDATE_FAIL_REVOKED', serverId, ipAddress);
      return { status: 'INVALID', message: 'License key has been revoked', expiresAt: null, boundServerId: null };
    }

    if (license.expiresAt && license.expiresAt < new Date()) {
      await logAudit(license.id, 'VALIDATE_FAIL_EXPIRED', serverId, ipAddress);
      return { status: 'EXPIRED', message: 'License key has expired', expiresAt: license.expiresAt.toISOString(), boundServerId: license.serverId };
    }

    // Bind-on-first-use: if no serverId bound yet, bind now
    if (!license.serverId) {
      await prisma.license.update({
        where: { key },
        data: { serverId, pluginVersion, lastValidAt: new Date() }
      });
      await logAudit(license.id, 'VALIDATE_BOUND', serverId, ipAddress);
      return { status: 'VALID', message: 'License activated and bound to this server', expiresAt: license.expiresAt?.toISOString() ?? null, boundServerId: serverId };
    }

    if (license.serverId !== serverId) {
      await logAudit(license.id, 'VALIDATE_FAIL_MISMATCH', serverId, ipAddress);
      return { status: 'MISMATCH', message: 'License key is bound to a different server', expiresAt: null, boundServerId: license.serverId };
    }

    await prisma.license.update({ where: { key }, data: { lastValidAt: new Date(), pluginVersion } });
    await logAudit(license.id, 'VALIDATE_SUCCESS', serverId, ipAddress);
    return { status: 'VALID', message: 'License valid', expiresAt: license.expiresAt?.toISOString() ?? null, boundServerId: license.serverId };
  },

  async generate(product: string, notes?: string, expiresAt?: string): Promise<string> {
    const key = generateKey();
    await prisma.license.create({
      data: {
        key,
        product,
        notes: notes ?? null,
        expiresAt: expiresAt ? new Date(expiresAt) : null
      }
    });
    return key;
  },

  async revoke(key: string): Promise<void> {
    await prisma.license.update({ where: { key }, data: { revokedAt: new Date() } });
  },

  async unbind(key: string): Promise<void> {
    await prisma.license.update({ where: { key }, data: { serverId: null } });
  },

  async listAll(product?: string) {
    return prisma.license.findMany({
      where: product ? { product } : undefined,
      orderBy: { createdAt: 'desc' }
    });
  },

  async findByKey(key: string) {
    return prisma.license.findUnique({ where: { key } });
  }
};

async function logAudit(licenseId: string | null, event: string, serverId: string, ipAddress: string) {
  if (!licenseId) return;
  await prisma.auditLog.create({ data: { licenseId, event, serverId, ipAddress } });
}
