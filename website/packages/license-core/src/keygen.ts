import { randomBytes } from "crypto";

// Crockford-style base32 alphabet — no ambiguous chars (0/O, 1/I).
// Ported verbatim from anti-freecam/license-server.
const BASE32_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

/**
 * Generates a license key with 80 bits of CSPRNG entropy, formatted as
 * `<PREFIX>-XXXX-XXXX-XXXX-XXXX`. The prefix is per-product (e.g. "AFC" for
 * AntiFreecam) so the marketplace can mint keys for any seller's product while
 * staying compatible with the original AFC-prefixed format.
 */
export function generateKey(prefix = "BC"): string {
  const bytes = randomBytes(10); // 80 bits
  let result = "";
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

  const trimmed = result.slice(0, 16).padEnd(16, "A");
  return `${prefix}-${trimmed.slice(0, 4)}-${trimmed.slice(4, 8)}-${trimmed.slice(8, 12)}-${trimmed.slice(12, 16)}`;
}
