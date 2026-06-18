import prisma from "../src/index";

// Minimal seed: a category tree so the catalog has something to browse.
async function main() {
  const categories = [
    { name: "Plugins", slug: "plugins" },
    { name: "Configs", slug: "configs" },
    { name: "Builds", slug: "builds" },
    { name: "Services", slug: "services" },
  ];

  for (const c of categories) {
    await prisma.category.upsert({
      where: { slug: c.slug },
      update: {},
      create: c,
    });
  }

  console.log(`Seeded ${categories.length} categories.`);
}

main()
  .then(() => prisma.$disconnect())
  .catch(async (e) => {
    console.error(e);
    await prisma.$disconnect();
    process.exit(1);
  });
