// Pluggable object storage. LocalDriver writes to disk (dev/demo, no creds).
// S3Driver targets any S3-compatible bucket (Cloudflare R2, Backblaze B2, AWS)
// and issues short-lived signed download URLs. Switch via STORAGE_DRIVER.

import { createHash } from "crypto";

export type PutResult = {
  storageKey: string;
  sizeBytes: number;
  checksumSha256: string;
  contentType: string;
};

export interface StorageDriver {
  readonly id: "local" | "s3";
  put(key: string, data: Buffer, contentType: string): Promise<PutResult>;
  /** A signed, time-limited URL the buyer can download from (s3). */
  signedUrl(key: string, ttlSeconds: number): Promise<string | null>;
  /** Raw bytes — used by the local driver to stream through the app. */
  read(key: string): Promise<Buffer | null>;
}

export function sha256(data: Buffer): string {
  return createHash("sha256").update(data).digest("hex");
}

import { LocalDriver } from "./local";

let cached: StorageDriver | null = null;

export function getStorage(): StorageDriver {
  if (cached) return cached;
  const driver = (process.env.STORAGE_DRIVER ?? "local").toLowerCase();
  if (driver === "s3") {
    // Lazy-require so the AWS SDK is only loaded when actually used.
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const { S3Driver } = require("./s3") as typeof import("./s3");
    cached = new S3Driver({
      endpoint: process.env.STORAGE_ENDPOINT ?? "",
      bucket: process.env.STORAGE_BUCKET ?? "brothercraft-files",
      accessKey: process.env.STORAGE_ACCESS_KEY ?? "",
      secretKey: process.env.STORAGE_SECRET_KEY ?? "",
    });
  } else {
    cached = new LocalDriver(
      process.env.STORAGE_LOCAL_DIR ?? "./storage"
    );
  }
  return cached;
}

export { LocalDriver } from "./local";
