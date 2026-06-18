import {
  S3Client,
  PutObjectCommand,
  GetObjectCommand,
} from "@aws-sdk/client-s3";
import { getSignedUrl } from "@aws-sdk/s3-request-presigner";
import type { StorageDriver, PutResult } from "./index";
import { sha256 } from "./index";

type S3Config = {
  endpoint: string;
  bucket: string;
  accessKey: string;
  secretKey: string;
};

/** S3-compatible storage (Cloudflare R2, Backblaze B2, AWS S3). Buckets are
 *  private; downloads go through short-lived signed URLs. */
export class S3Driver implements StorageDriver {
  readonly id = "s3" as const;
  private client: S3Client;

  constructor(private config: S3Config) {
    this.client = new S3Client({
      region: "auto",
      endpoint: config.endpoint,
      credentials: {
        accessKeyId: config.accessKey,
        secretAccessKey: config.secretKey,
      },
    });
  }

  async put(key: string, data: Buffer, contentType: string): Promise<PutResult> {
    await this.client.send(
      new PutObjectCommand({
        Bucket: this.config.bucket,
        Key: key,
        Body: data,
        ContentType: contentType,
      })
    );
    return {
      storageKey: key,
      sizeBytes: data.byteLength,
      checksumSha256: sha256(data),
      contentType,
    };
  }

  async signedUrl(key: string, ttlSeconds: number): Promise<string> {
    return getSignedUrl(
      this.client,
      new GetObjectCommand({ Bucket: this.config.bucket, Key: key }),
      { expiresIn: ttlSeconds }
    );
  }

  async read(): Promise<Buffer | null> {
    // S3 objects are served via signedUrl, not streamed through the app.
    return null;
  }
}
