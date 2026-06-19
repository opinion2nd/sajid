import type { SessionData } from "@/lib/session";

export function Nav({ session, guildId }: { session: SessionData; guildId?: string }) {
  return (
    <div className="nav">
      <div>
        <a href="/dashboard">Servers</a>
        {guildId && (
          <>
            <a href={`/dashboard/${guildId}`}>Settings</a>
            <a href={`/dashboard/${guildId}/tickets`}>Tickets</a>
            <a href={`/dashboard/${guildId}/stats`}>Stats</a>
          </>
        )}
      </div>
      <div>
        <span style={{ marginRight: 12, color: "#b5bac1" }}>{session.username}</span>
        <a className="btn secondary" href="/api/auth/logout">
          Logout
        </a>
      </div>
    </div>
  );
}
