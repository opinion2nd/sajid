import { mkdir, writeFile, readFile } from "fs/promises";
import { dirname, join, resolve } from "path";
import type { StorageDriver, PutResult } from "./index";
import { sha256 } from "./index";

/** Filesystem-backed storage for dev/demo. Files live under a private dir and
 *  are streamed through the app's entitlement-checked download route. */
export class LocalDriver implements StorageDriver {
  readonly id = "local" as const;
  private root: string;

  constructor(dir: string) {
    this.root = resolve(process.cwd(), dir);
  }

  private full(key: string) {
    // Prevent path traversal.
    const safe = key.replace(/\.\./g, "").replace(/^\/+/, "");
    return join(this.root, safe);
  }

  async put(key: string, data: Buffer, contentType: string): Promise<PutResult> {
    const path = this.full(key);
    await mkdir(dirname(path), { recursive: true });
    await writeFile(path, data);
    return {
      storageKey: key,
      sizeBytes: data.byteLength,
      checksumSha256: sha256(data),
      contentType,
    };
  }

  async signedUrl(): Promise<string | null> {
    // Local files are streamed by the app, not served via signed URL.
    return null;
  }

  async read(key: string): Promise<Buffer | null> {
    try {
      return await readFile(this.full(key));
    } catch {
      return null;
    }
  }
}
