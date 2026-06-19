import { requireGuildAccess } from "@/lib/auth";
import { getTickets } from "@/lib/db";
import { Nav } from "../../Nav";
import { closeTicketAction } from "../actions";

export default async function TicketsPage({ params }: { params: Promise<{ guildId: string }> }) {
  const { guildId } = await params;
  const session = await requireGuildAccess(guildId);
  const tickets = getTickets(guildId);

  return (
    <div>
      <Nav session={session} guildId={guildId} />
      <div className="container">
        <h2>Tickets</h2>
        <div className="card">
          {tickets.length === 0 ? (
            <p style={{ color: "#b5bac1" }}>No tickets yet.</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>User</th>
                  <th>Status</th>
                  <th>Created</th>
                  <th>Closed</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {tickets.map((ticket) => (
                  <tr key={ticket.id}>
                    <td>#{ticket.id}</td>
                    <td>{ticket.user_id}</td>
                    <td>
                      <span className={`badge ${ticket.status}`}>{ticket.status}</span>
                    </td>
                    <td>{new Date(ticket.created_at).toLocaleString()}</td>
                    <td>{ticket.closed_at ? new Date(ticket.closed_at).toLocaleString() : "—"}</td>
                    <td>
                      {ticket.status === "open" && (
                        <form action={closeTicketAction}>
                          <input type="hidden" name="guildId" value={guildId} />
                          <input type="hidden" name="ticketId" value={ticket.id} />
                          <button className="btn secondary" type="submit" style={{ padding: "4px 10px", fontSize: 12 }}>
                            Mark Closed
                          </button>
                        </form>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
        <p style={{ color: "#80848e", fontSize: 13 }}>
          "Mark Closed" updates the record only — it doesn't delete the Discord channel. Close tickets normally with the
          in-Discord button to clean up channels too.
        </p>
      </div>
    </div>
  );
}
