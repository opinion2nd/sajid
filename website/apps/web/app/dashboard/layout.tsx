import { requireUser } from "@/lib/session";
import { SideNav, type NavItem } from "@/components/side-nav";

const items: NavItem[] = [
  { href: "/dashboard", label: "Overview" },
  { href: "/dashboard/orders", label: "Orders" },
  { href: "/dashboard/downloads", label: "Downloads & Licenses" },
  { href: "/dashboard/discord", label: "Discord" },
  { href: "/seller", label: "Seller area" },
];

export default async function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  await requireUser();
  return (
    <div className="container-page py-8">
      <div className="grid gap-6 lg:grid-cols-[220px_1fr]">
        <SideNav title="Account" items={items} />
        <div>{children}</div>
      </div>
    </div>
  );
}
