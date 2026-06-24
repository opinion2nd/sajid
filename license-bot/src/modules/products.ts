import { getStore, save, type Product } from "../db.js";

export function createProduct(input: {
  guildId: string;
  name: string;
  customerRoleId: string | null;
  defaultIpCap: number;
  defaultHwidCap: number;
  createdBy: string;
}): Product {
  const product: Product = {
    guildId: input.guildId,
    name: input.name,
    customerRoleId: input.customerRoleId,
    defaultIpCap: input.defaultIpCap,
    defaultHwidCap: input.defaultHwidCap,
    createdBy: input.createdBy,
    createdAt: Date.now(),
  };
  getStore().products.push(product);
  save();
  return product;
}

export function getProduct(guildId: string, name: string): Product | undefined {
  return getStore().products.find(
    (p) => p.guildId === guildId && p.name.toLowerCase() === name.toLowerCase(),
  );
}

export function listProducts(guildId: string): Product[] {
  return getStore().products.filter((p) => p.guildId === guildId);
}

export function deleteProduct(guildId: string, name: string, cascade: boolean): boolean {
  const store = getStore();
  const index = store.products.findIndex(
    (p) => p.guildId === guildId && p.name.toLowerCase() === name.toLowerCase(),
  );
  if (index === -1) return false;
  const [removed] = store.products.splice(index, 1);
  if (cascade) {
    store.licenses = store.licenses.filter(
      (l) => !(l.guildId === guildId && l.productName.toLowerCase() === removed.name.toLowerCase()),
    );
  }
  save();
  return true;
}
