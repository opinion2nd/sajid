/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  experimental: {
    // Allow product file uploads through server actions (default is 1MB).
    serverActions: { bodySizeLimit: "25mb" },
  },
  // Prisma client lives in a workspace package — keep it server-external.
  serverExternalPackages: [
    "@prisma/client",
    "@brothercraft/db",
    "@aws-sdk/client-s3",
    "@aws-sdk/s3-request-presigner",
  ],
  transpilePackages: [
    "@brothercraft/license-core",
    "@brothercraft/payments",
    "@brothercraft/storage",
    "@brothercraft/email",
  ],
};

export default nextConfig;
