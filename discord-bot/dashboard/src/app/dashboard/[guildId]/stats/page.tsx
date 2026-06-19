import { requireGuildAccess } from "@/lib/auth";
import { getStats, getLeaderboard } from "@/lib/db";
import { Nav } from "../../Nav";

export default async function StatsPage({ params }: { params: Promise<{ guildId: string }> }) {
  const { guildId } = await params;
  const session = await requireGuildAccess(guildId);
  const stats = getStats(guildId);
  const leaderboard = getLeaderboard(guildId, 10);
  const maxXp = leaderboard[0]?.xp ?? 1;

  return (
    <div>
      <Nav session={session} guildId={guildId} />
      <div className="container">
        <h2>Stats</h2>

        <div className="grid">
          <div className="card stat">
            <div className="value">{stats.openTickets}</div>
            <div className="label">Open Tickets</div>
          </div>
          <div className="card stat">
            <div className="value">{stats.closedTickets}</div>
            <div className="label">Closed Tickets</div>
          </div>
          <div className="card stat">
            <div className="value">{stats.totalWarnings}</div>
            <div className="label">Warnings Issued</div>
          </div>
          <div className="card stat">
            <div className="value">{stats.trackedMembers}</div>
            <div className="label">Members With XP</div>
          </div>
          <div className="card stat">
            <div className="value">{stats.activeGiveaways}</div>
            <div className="label">Active Giveaways</div>
          </div>
        </div>

        <div className="card">
          <h3>Top 10 Leaderboard</h3>
          {leaderboard.length === 0 ? (
            <p style={{ color: "#b5bac1" }}>No leveling activity yet.</p>
          ) : (
            leaderboard.map((entry, i) => (
              <div key={entry.user_id} style={{ marginBottom: 10 }}>
                <div style={{ display: "flex", justifyContent: "space-between", fontSize: 13, marginBottom: 4 }}>
                  <span>
                    #{i + 1} — {entry.user_id} (Level {entry.level})
                  </span>
                  <span>{entry.xp} XP</span>
                </div>
                <div className="bar">
                  <div className="bar-fill" style={{ width: `${Math.max(4, (entry.xp / maxXp) * 100)}%` }} />
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
