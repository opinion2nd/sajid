/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  // Prisma client lives in a workspace package — keep it server-external.
  serverExternalPackages: ["@prisma/client", "@brothercraft/db"],
  transpilePackages: ["@brothercraft/license-core", "@brothercraft/payments"],
};

export default nextConfig;
