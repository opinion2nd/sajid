import bcrypt from "bcryptjs";
import prisma from "../src/index";

// Demo seed so the marketplace looks alive out of the box.
// Login credentials (all password: "password123"):
//   admin@brothercraft.dev   (ADMIN)
//   seller@brothercraft.dev  (SELLER — "TheWindows Studios")
//   buyer@brothercraft.dev   (BUYER — owns one purchased product)

async function main() {
  const password = await bcrypt.hash("password123", 10);

  // ── Categories ──────────────────────────────────────
  const catData = [
    { name: "Plugins", slug: "plugins" },
    { name: "Configs", slug: "configs" },
    { name: "Builds", slug: "builds" },
    { name: "Services", slug: "services" },
  ];
  const categories: Record<string, string> = {};
  for (const c of catData) {
    const cat = await prisma.category.upsert({
      where: { slug: c.slug },
      update: {},
      create: c,
    });
    categories[c.slug] = cat.id;
  }

  // ── Users ───────────────────────────────────────────
  const admin = await prisma.user.upsert({
    where: { email: "admin@brothercraft.dev" },
    update: {},
    create: {
      email: "admin@brothercraft.dev",
      handle: "admin",
      passwordHash: password,
      role: "ADMIN",
      wallet: { create: {} },
    },
  });

  const sellerUser = await prisma.user.upsert({
    where: { email: "seller@brothercraft.dev" },
    update: {},
    create: {
      email: "seller@brothercraft.dev",
      handle: "TheWindows",
      passwordHash: password,
      role: "SELLER",
      wallet: { create: {} },
    },
  });

  const buyer = await prisma.user.upsert({
    where: { email: "buyer@brothercraft.dev" },
    update: {},
    create: {
      email: "buyer@brothercraft.dev",
      handle: "steve",
      passwordHash: password,
      role: "BUYER",
      wallet: { create: {} },
    },
  });

  const seller = await prisma.sellerProfile.upsert({
    where: { userId: sellerUser.id },
    update: {},
    create: {
      userId: sellerUser.id,
      displayName: "TheWindows Studios",
      slug: "thewindows",
      bio: "Premium Minecraft plugins & minigames. Maker of Pillars and AntiFreecam.",
      status: "APPROVED",
      verified: true,
      ratingAvg: 4.8,
      salesCount: 1240,
    },
  });

  // ── Products ────────────────────────────────────────
  const products = [
    {
      slug: "pillars-minigame",
      title: "Pillars — PvP Minigame",
      summary: "Fast-paced PvP minigame plugin with NPCs, maps, stats & market.",
      cat: "plugins",
      priceCents: 49900,
      licenseGated: true,
      prefix: "PIL",
      tags: ["pvp", "minigame", "pocketmine"],
      rating: 4.9,
      downloads: 870,
      description:
        "## Pillars\nA complete PvP minigame for your server.\n\n- NPC-driven join system\n- Multi-map support with auto-reset\n- Per-player stats & scoreboards\n- In-game market & cosmetics\n\nWorks on PocketMine-MP 5.0.0+.",
    },
    {
      slug: "antifreecam",
      title: "AntiFreecam — Block Masker",
      summary: "Stops freecam/x-ray cheats by masking hidden blocks. Paper 1.21.x.",
      cat: "plugins",
      priceCents: 29900,
      licenseGated: true,
      prefix: "AFC",
      tags: ["anticheat", "paper", "fabric"],
      rating: 4.8,
      downloads: 2100,
      description:
        "## AntiFreecam\nMasks blocks a player should not be able to see, defeating freecam and x-ray.\n\n- Paper, Fabric & NeoForge builds\n- Configurable mask radius\n- Negligible performance impact\n- License-protected",
    },
    {
      slug: "survival-spawn-hub",
      title: "Survival Spawn Hub",
      summary: "A polished 150×150 spawn build with portals, shops & parkour.",
      cat: "builds",
      priceCents: 79900,
      licenseGated: false,
      prefix: "BC",
      tags: ["build", "spawn", "schematic"],
      rating: 4.7,
      downloads: 430,
      description:
        "## Survival Spawn Hub\nDrop-in spawn schematic (WorldEdit .schem) with themed portal areas, NPC shop stalls and a hidden parkour course.",
    },
    {
      slug: "essentials-config-pack",
      title: "EssentialsX Config Pack",
      summary: "Battle-tested EssentialsX + LuckPerms config for survival servers.",
      cat: "configs",
      priceCents: 14900,
      licenseGated: false,
      prefix: "BC",
      tags: ["config", "essentials", "luckperms"],
      rating: 4.6,
      downloads: 980,
      description:
        "## EssentialsX Config Pack\nReady-to-use config bundle: ranks, kits, warps, economy and chat formatting. Just drop into your plugins folder.",
    },
    {
      slug: "custom-plugin-development",
      title: "Custom Plugin Development",
      summary: "Hire us to build a bespoke plugin for your server. Per-project.",
      cat: "services",
      priceCents: 500000,
      licenseGated: false,
      prefix: "BC",
      tags: ["service", "development", "custom"],
      rating: 5.0,
      downloads: 60,
      description:
        "## Custom Plugin Development\nTell us what you need — we scope, build, test and deliver a production-ready plugin with source and support.",
    },
    {
      slug: "skyblock-starter",
      title: "SkyBlock Starter Kit",
      summary: "Island generator config + GUI menus + quests for SkyBlock servers.",
      cat: "configs",
      priceCents: 24900,
      licenseGated: false,
      prefix: "BC",
      tags: ["skyblock", "config", "gui"],
      rating: 4.5,
      downloads: 720,
      description:
        "## SkyBlock Starter Kit\nEverything to launch a SkyBlock server: island schematic, generator tiers, quest GUI and a level system config.",
    },
    {
      slug: "anticheat-lite",
      title: "AntiCheat Lite",
      summary: "Lightweight movement & combat checks. Low false-positive rate.",
      cat: "plugins",
      priceCents: 39900,
      licenseGated: true,
      prefix: "ACL",
      tags: ["anticheat", "paper"],
      rating: 4.4,
      downloads: 510,
      description:
        "## AntiCheat Lite\nCovers fly, speed, reach and autoclicker with tunable thresholds and a clean admin alert feed.",
    },
    {
      slug: "medieval-city-build",
      title: "Medieval City Build",
      summary: "Sprawling 400×400 medieval city schematic with interiors.",
      cat: "builds",
      priceCents: 129900,
      licenseGated: false,
      prefix: "BC",
      tags: ["build", "medieval", "city"],
      rating: 4.9,
      downloads: 290,
      description:
        "## Medieval City Build\nA detailed walled city: castle, market square, docks and fully furnished interiors. WorldEdit .schem + screenshots included.",
    },
  ];

  const created: { id: string; slug: string; priceCents: number }[] = [];
  for (const p of products) {
    const product = await prisma.product.upsert({
      where: { slug: p.slug },
      update: {},
      create: {
        sellerId: seller.id,
        categoryId: categories[p.cat],
        title: p.title,
        slug: p.slug,
        summary: p.summary,
        description: p.description,
        priceCents: p.priceCents,
        currency: "BDT",
        type: p.cat === "services" ? "SERVICE" : "DIGITAL",
        status: "PUBLISHED",
        licenseGated: p.licenseGated,
        licensePrefix: p.prefix,
        maxActivations: p.licenseGated ? 1 : 1,
        tags: p.tags,
        ratingAvg: p.rating,
        downloadsCount: p.downloads,
        versions: {
          create: {
            semver: "1.0.0",
            changelog: "Initial release.",
            status: "PUBLISHED",
          },
        },
      },
    });
    created.push({ id: product.id, slug: p.slug, priceCents: p.priceCents });
  }

  // ── A completed order for the buyer (so dashboard shows data) ──
  const pillars = created.find((c) => c.slug === "pillars-minigame")!;
  const existingOrder = await prisma.order.findFirst({
    where: { buyerId: buyer.id },
  });
  if (!existingOrder) {
    const order = await prisma.order.create({
      data: {
        buyerId: buyer.id,
        status: "COMPLETED",
        subtotalCents: pillars.priceCents,
        totalCents: pillars.priceCents,
        paidAt: new Date(),
        items: {
          create: {
            productId: pillars.id,
            sellerId: seller.id,
            unitPriceCents: pillars.priceCents,
          },
        },
        transaction: {
          create: {
            provider: "WALLET",
            amountCents: pillars.priceCents,
            status: "VERIFIED",
            verifiedAt: new Date(),
            providerRef: "seed-demo-txn",
          },
        },
        licenses: {
          create: {
            key: "PIL-DEMO-7K2M-9XQ4-ABCD",
            productId: pillars.id,
            buyerId: buyer.id,
            maxActivations: 1,
          },
        },
      },
    });

    // A review from the buyer.
    await prisma.review.create({
      data: {
        productId: pillars.id,
        buyerId: buyer.id,
        orderId: order.id,
        rating: 5,
        body: "Best minigame plugin I've used. Setup took 5 minutes.",
        sellerReply: "Thank you! Glad it's working well for your server.",
      },
    });
  }

  console.log("Seed complete:");
  console.log("  admin@brothercraft.dev  / password123  (ADMIN)");
  console.log("  seller@brothercraft.dev / password123  (SELLER)");
  console.log("  buyer@brothercraft.dev  / password123  (BUYER)");
  console.log(`  ${created.length} products, 4 categories`);
}

main()
  .then(() => prisma.$disconnect())
  .catch(async (e) => {
    console.error(e);
    await prisma.$disconnect();
    process.exit(1);
  });
