import { requireGuildAccess } from "@/lib/auth";
import { getGuildConfig } from "@/lib/db";
import { fetchGuildChannels, fetchGuildRoles } from "@/lib/discord";
import { Nav } from "../Nav";
import { updateModerationSettings, updateSecuritySettings, updateChannelSettings, updateRoleSettings } from "./actions";

export default async function GuildSettingsPage({ params }: { params: Promise<{ guildId: string }> }) {
  const { guildId } = await params;
  const session = await requireGuildAccess(guildId);
  const config = getGuildConfig(guildId);
  const [{ text: textChannels, categories }, roles] = await Promise.all([
    fetchGuildChannels(guildId),
    fetchGuildRoles(guildId),
  ]);

  function channelSelect(name: string, current: string | null) {
    return (
      <select name={name} defaultValue={current ?? ""}>
        <option value="">— None —</option>
        {textChannels.map((c) => (
          <option key={c.id} value={c.id}>
            #{c.name}
          </option>
        ))}
      </select>
    );
  }

  return (
    <div>
      <Nav session={session} guildId={guildId} />
      <div className="container">
        <h2>Server Settings</h2>

        <form className="card" action={updateChannelSettings}>
          <input type="hidden" name="guildId" value={guildId} />
          <h3>Channels & Messages</h3>
          <label>Mod Log Channel</label>
          {channelSelect("mod_log_channel", config.mod_log_channel)}
          <label>Welcome Channel</label>
          {channelSelect("welcome_channel", config.welcome_channel)}
          <label>Welcome Message (use {"{user}"}, {"{username}"}, {"{server}"}, {"{memberCount}"})</label>
          <textarea name="welcome_message" defaultValue={config.welcome_message ?? ""} rows={2} />
          <label>Leave Channel</label>
          {channelSelect("leave_channel", config.leave_channel)}
          <label>Leave Message</label>
          <textarea name="leave_message" defaultValue={config.leave_message ?? ""} rows={2} />
          <label>Level-Up Channel</label>
          {channelSelect("levelup_channel", config.levelup_channel)}
          <label>Ticket Category</label>
          <select name="ticket_category" defaultValue={config.ticket_category ?? ""}>
            <option value="">— None —</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
          <label>Ticket Log Channel</label>
          {channelSelect("ticket_log_channel", config.ticket_log_channel)}
          <label>Suggestion Channel</label>
          {channelSelect("suggestion_channel", config.suggestion_channel)}
          <div style={{ marginTop: 16 }}>
            <button className="btn" type="submit">
              Save Channels
            </button>
          </div>
        </form>

        <form className="card" action={updateRoleSettings}>
          <input type="hidden" name="guildId" value={guildId} />
          <h3>Roles</h3>
          <label>Ticket Support Role</label>
          <select name="ticket_support_role" defaultValue={config.ticket_support_role ?? ""}>
            <option value="">— None —</option>
            {roles.map((r) => (
              <option key={r.id} value={r.id}>
                {r.name}
              </option>
            ))}
          </select>
          <label>Verification Role</label>
          <select name="verify_role" defaultValue={config.verify_role ?? ""}>
            <option value="">— None —</option>
            {roles.map((r) => (
              <option key={r.id} value={r.id}>
                {r.name}
              </option>
            ))}
          </select>
          <div style={{ marginTop: 16 }}>
            <button className="btn" type="submit">
              Save Roles
            </button>
          </div>
        </form>

        <form className="card" action={updateModerationSettings}>
          <input type="hidden" name="guildId" value={guildId} />
          <h3>Automod</h3>
          <label>
            <input type="checkbox" name="automod_enabled" defaultChecked={Boolean(config.automod_enabled)} /> Enabled
          </label>
          <label>
            <input type="checkbox" name="automod_anti_invite" defaultChecked={Boolean(config.automod_anti_invite)} /> Block invite links
          </label>
          <label>
            <input type="checkbox" name="automod_anti_caps" defaultChecked={Boolean(config.automod_anti_caps)} /> Block excessive caps
          </label>
          <label>
            <input type="checkbox" name="automod_anti_spam" defaultChecked={Boolean(config.automod_anti_spam)} /> Block duplicate-message spam
          </label>
          <label>Max Mentions Per Message</label>
          <input type="number" name="automod_max_mentions" defaultValue={config.automod_max_mentions} min={0} max={50} />
          <div style={{ marginTop: 16 }}>
            <button className="btn" type="submit">
              Save Automod
            </button>
          </div>
        </form>

        <form className="card" action={updateSecuritySettings}>
          <input type="hidden" name="guildId" value={guildId} />
          <h3>Security</h3>
          <label>
            <input type="checkbox" name="anti_raid_enabled" defaultChecked={Boolean(config.anti_raid_enabled)} /> Anti-Raid Enabled
          </label>
          <label>Join Threshold</label>
          <input type="number" name="raid_join_threshold" defaultValue={config.raid_join_threshold} min={2} max={50} />
          <label>Window (seconds)</label>
          <input type="number" name="raid_window_seconds" defaultValue={config.raid_window_seconds} min={2} max={300} />
          <label>Auto-kick accounts younger than (days)</label>
          <input type="number" name="raid_account_age_days" defaultValue={config.raid_account_age_days} min={0} max={365} />

          <label style={{ marginTop: 20 }}>
            <input type="checkbox" name="anti_nuke_enabled" defaultChecked={Boolean(config.anti_nuke_enabled)} /> Anti-Nuke Enabled
          </label>
          <label>Action Threshold</label>
          <input type="number" name="nuke_threshold" defaultValue={config.nuke_threshold} min={2} max={50} />
          <label>Window (seconds)</label>
          <input type="number" name="nuke_window_seconds" defaultValue={config.nuke_window_seconds} min={5} max={600} />

          <label style={{ marginTop: 20 }}>
            <input type="checkbox" name="anti_ghostping_enabled" defaultChecked={Boolean(config.anti_ghostping_enabled)} /> Ghost-Ping Detection
          </label>
          <div style={{ marginTop: 16 }}>
            <button className="btn" type="submit">
              Save Security
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
