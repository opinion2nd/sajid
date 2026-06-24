import { redirect } from "next/navigation";
import { getSession } from "@/lib/session";

export default async function HomePage() {
  const session = await getSession();
  if (session) redirect("/dashboard");

  return (
    <div className="container" style={{ textAlign: "center", paddingTop: 80 }}>
      <h1>Flaming Bot Dashboard</h1>
      <p style={{ color: "#b5bac1" }}>Sign in with Discord to manage your server's settings, tickets, and stats.</p>
      <a className="btn" href="/api/auth/login">
        Login with Discord
      </a>
    </div>
  );
}
