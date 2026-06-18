-- CreateTable
CREATE TABLE "discord_link_codes" (
    "code" TEXT NOT NULL,
    "userId" TEXT NOT NULL,
    "expiresAt" TIMESTAMP(3) NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "discord_link_codes_pkey" PRIMARY KEY ("code")
);
