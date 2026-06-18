import Link from "next/link";
import { Blocks } from "lucide-react";

export function Footer() {
  return (
    <footer className="mt-16 border-t border-border bg-surface">
      <div className="container-page grid gap-8 py-10 sm:grid-cols-2 md:grid-cols-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="grid h-8 w-8 place-items-center rounded-lg bg-accent text-accent-fg">
              <Blocks className="h-4 w-4" />
            </span>
            <span className="font-bold">
              Brother<span className="text-accent">Craft</span>
            </span>
          </div>
          <p className="mt-3 text-sm text-muted">
            The Minecraft marketplace for plugins, configs, builds and services.
          </p>
        </div>
        <FooterCol
          title="Marketplace"
          links={[
            ["Browse", "/browse"],
            ["Become a seller", "/sell"],
            ["Cart", "/cart"],
          ]}
        />
        <FooterCol
          title="Developers"
          links={[
            ["License API", "/docs/license-api"],
            ["Health", "/api/health"],
          ]}
        />
        <FooterCol
          title="Legal"
          links={[
            ["Terms of Service", "/legal/terms"],
            ["Privacy Policy", "/legal/privacy"],
            ["Refund Policy", "/legal/refund"],
          ]}
        />
      </div>
      <div className="border-t border-border">
        <div className="container-page flex flex-col items-center justify-between gap-2 py-5 text-sm text-muted sm:flex-row">
          <span>© {new Date().getFullYear()} Brother Craft. All rights reserved.</span>
          <span>Payments via bKash &amp; Nagad</span>
        </div>
      </div>
    </footer>
  );
}

function FooterCol({
  title,
  links,
}: {
  title: string;
  links: [string, string][];
}) {
  return (
    <div>
      <h4 className="mb-3 text-sm font-semibold">{title}</h4>
      <ul className="space-y-2">
        {links.map(([label, href]) => (
          <li key={href}>
            <Link href={href} className="text-sm text-muted hover:text-text">
              {label}
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
}
