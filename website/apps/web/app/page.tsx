export default function HomePage() {
  return (
    <main style={{ maxWidth: 960, margin: "0 auto", padding: "4rem 1.5rem" }}>
      <header style={{ marginBottom: "3rem" }}>
        <span
          style={{
            display: "inline-block",
            padding: "0.25rem 0.75rem",
            borderRadius: 999,
            background: "var(--panel)",
            border: "1px solid var(--border)",
            color: "var(--muted)",
            fontSize: 13,
          }}
        >
          Phase 0 · Foundation
        </span>
        <h1 style={{ fontSize: "3rem", margin: "1rem 0 0.5rem", lineHeight: 1.1 }}>
          Brother Craft
        </h1>
        <p style={{ fontSize: "1.25rem", color: "var(--muted)", maxWidth: 620 }}>
          The Minecraft marketplace for plugins, configs, builds and services.
          License-gated downloads, escrow-protected payments via bKash &amp;
          Nagad, and a Discord-native community.
        </p>
      </header>

      <section
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
          gap: "1rem",
        }}
      >
        {[
          {
            t: "Sell anything Minecraft",
            d: "Plugins, configs, builds, services — versioned with changelogs.",
          },
          {
            t: "License-gated plugins",
            d: "Every purchase mints a key your plugin validates against our API.",
          },
          {
            t: "bKash & Nagad checkout",
            d: "Escrow-held payments with seller payouts and buyer protection.",
          },
          {
            t: "Discord integration",
            d: "Link accounts, auto-assign roles, get receipts in your DMs.",
          },
        ].map((f) => (
          <div
            key={f.t}
            style={{
              background: "var(--panel)",
              border: "1px solid var(--border)",
              borderRadius: 12,
              padding: "1.25rem",
            }}
          >
            <h3 style={{ margin: "0 0 0.5rem" }}>{f.t}</h3>
            <p style={{ margin: 0, color: "var(--muted)", fontSize: 14 }}>{f.d}</p>
          </div>
        ))}
      </section>

      <footer
        style={{
          marginTop: "3rem",
          paddingTop: "1.5rem",
          borderTop: "1px solid var(--border)",
          color: "var(--muted)",
          fontSize: 13,
        }}
      >
        License API live at <code>/api/v1/license/validate</code> · Health at{" "}
        <code>/api/health</code>
      </footer>
    </main>
  );
}
