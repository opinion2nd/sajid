import { redirect } from "next/navigation";
import { getSession } from "@/lib/session";
import { Nav } from "./Nav";

export default async function DashboardPage() {
  const session = await getSession();
  if (!session) redirect("/");

  return (
    <div>
      <Nav session={session} />
      <div className="container">
        <h2>Your Servers</h2>
        {session.guilds.length === 0 && (
          <p style={{ color: "#b5bac1" }}>No manageable servers found where this bot is installed.</p>
        )}
        <div className="grid">
          {session.guilds.map((guild) => (
            <a key={guild.id} className="card guild-card" href={`/dashboard/${guild.id}`}>
              {guild.icon ? (
                <img
                  src={`https://cdn.discordapp.com/icons/${guild.id}/${guild.icon}.png`}
                  alt=""
                  width={40}
                  height={40}
                  style={{ borderRadius: "50%" }}
                />
              ) : (
                <div style={{ width: 40, height: 40, borderRadius: "50%", background: "#5865f2" }} />
              )}
              <span>{guild.name}</span>
            </a>
          ))}
        </div>
      </div>
    </div>
  );
}
