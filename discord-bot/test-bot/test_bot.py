"""Tester bot (Python/discord.py port): run defensive-feature tests against
your own server with a slash command instead of a CLI script. Use a
SEPARATE bot account from your main bot -- the main bot is exempt from its
own anti-nuke punishment, so it can't usefully test itself.

Every create/delete burst below fires concurrently (asyncio.gather) instead
of looping with sleeps, so each test runs as fast as Discord's API allows.
There is no artificial delay anywhere in this file -- the only floor left
is Discord's own network latency and rate limits, which no client can skip.

Setup: copy .env.example to .env and fill in TEST_BOT_TOKEN, TEST_GUILD_ID,
TEST_OWNER_ID. Enable the "Server Members Intent" in the Bot tab for this
application (needed for automod-mentions/ghostping/nuke-kicks/nuke-bans).
Invite it with Manage Channels, Manage Roles, Manage Webhooks, Manage
Emojis and Stickers, Kick Members, Ban Members, and Send Messages, then:
  pip install -r requirements.txt
  python test_bot.py
Commands sync to TEST_GUILD_ID automatically on startup. Then in Discord:
/test nuke-channels, /test automod-caps, etc.
"""

import asyncio
import os
from typing import Optional

import discord
from discord import app_commands
from dotenv import load_dotenv

load_dotenv()

TEST_BOT_TOKEN = os.environ["TEST_BOT_TOKEN"]
TEST_GUILD_ID = int(os.environ["TEST_GUILD_ID"])
TEST_OWNER_ID = int(os.environ["TEST_OWNER_ID"])

GUILD = discord.Object(id=TEST_GUILD_ID)

intents = discord.Intents.default()
intents.guilds = True
intents.guild_messages = True
intents.members = True

client = discord.Client(intents=intents)
tree = app_commands.CommandTree(client)


async def nuke_channels(guild: discord.Guild) -> list[str]:
    lines = ["Creating 4 channels concurrently..."]
    channels = await asyncio.gather(*(guild.create_text_channel(f"nuke-test-{i + 1}") for i in range(4)))
    lines.append("Deleting all of them concurrently...")
    results = await asyncio.gather(*(ch.delete(reason="nuke-test") for ch in channels), return_exceptions=True)
    for ch, result in zip(channels, results):
        lines.append(f"Failed to delete #{ch.name}: {result}" if isinstance(result, Exception) else f"Deleted #{ch.name}")
    return lines


async def nuke_roles(guild: discord.Guild) -> list[str]:
    lines = ["Creating 4 roles concurrently..."]
    roles = await asyncio.gather(*(guild.create_role(name=f"nuke-test-role-{i + 1}") for i in range(4)))
    lines.append("Deleting all of them concurrently...")
    results = await asyncio.gather(*(role.delete(reason="nuke-test") for role in roles), return_exceptions=True)
    for role, result in zip(roles, results):
        lines.append(f"Failed to delete {role.name}: {result}" if isinstance(result, Exception) else f"Deleted role {role.name}")
    return lines


async def nuke_webhooks(channel: discord.TextChannel) -> list[str]:
    lines = ["Creating 4 webhooks concurrently..."]
    hooks = await asyncio.gather(*(channel.create_webhook(name=f"nuke-test-hook-{i + 1}") for i in range(4)))
    lines.append("Deleting all of them concurrently...")
    results = await asyncio.gather(*(hook.delete(reason="nuke-test") for hook in hooks), return_exceptions=True)
    for hook, result in zip(hooks, results):
        lines.append(f"Failed to delete webhook {hook.name}: {result}" if isinstance(result, Exception) else f"Deleted webhook {hook.name}")
    return lines


async def nuke_permissions(guild: discord.Guild) -> list[str]:
    lines = ["Creating 4 channels concurrently..."]
    channels = await asyncio.gather(*(guild.create_text_channel(f"perm-test-{i + 1}") for i in range(4)))
    lines.append("Denying @everyone's View Channel permission on all of them concurrently...")
    everyone = guild.default_role
    await asyncio.gather(*(ch.set_permissions(everyone, view_channel=False, reason="nuke-test") for ch in channels))
    lines.append("Cleaning up the temporary channels concurrently...")
    results = await asyncio.gather(*(ch.delete(reason="nuke-test") for ch in channels), return_exceptions=True)
    for ch, result in zip(channels, results):
        if isinstance(result, Exception):
            lines.append(f"Failed to delete #{ch.name}: {result}")
    return lines


async def nuke_emojis(guild: discord.Guild) -> list[str]:
    lines = ["Creating 4 temporary emojis (using the bot's own avatar) concurrently..."]
    avatar_bytes = await client.user.display_avatar.read()
    emojis = await asyncio.gather(*(guild.create_custom_emoji(name=f"nuke_test_{i + 1}", image=avatar_bytes) for i in range(4)))
    lines.append("Deleting all of them concurrently...")
    results = await asyncio.gather(*(e.delete(reason="nuke-test") for e in emojis), return_exceptions=True)
    for emoji, result in zip(emojis, results):
        lines.append(f"Failed to delete emoji {emoji.name}: {result}" if isinstance(result, Exception) else f"Deleted emoji {emoji.name}")
    return lines


async def nuke_kicks(guild: discord.Guild, targets: list[discord.User]) -> list[str]:
    if not targets:
        return ["No target members provided."]
    lines = [f"Kicking {len(targets)} member(s) concurrently..."]
    results = await asyncio.gather(*(guild.kick(user, reason="nuke-test") for user in targets), return_exceptions=True)
    for user, result in zip(targets, results):
        lines.append(f"Failed to kick {user}: {result}" if isinstance(result, Exception) else f"Kicked {user}")
    lines.append("Kicks can't be auto-undone -- send them a fresh invite link to rejoin.")
    return lines


async def nuke_bans(guild: discord.Guild, targets: list[discord.User]) -> list[str]:
    if not targets:
        return ["No target members provided."]
    lines = [f"Banning {len(targets)} member(s) concurrently..."]
    ban_results = await asyncio.gather(*(guild.ban(user, reason="nuke-test") for user in targets), return_exceptions=True)
    for user, result in zip(targets, ban_results):
        lines.append(f"Failed to ban {user}: {result}" if isinstance(result, Exception) else f"Banned {user}")
    lines.append("Unbanning everyone concurrently...")
    unban_results = await asyncio.gather(*(guild.unban(user, reason="nuke-test cleanup") for user in targets), return_exceptions=True)
    for user, result in zip(targets, unban_results):
        if isinstance(result, Exception):
            lines.append(f"Failed to unban {user}: {result}")
    lines.append("Unbanned everyone. They'll still need a fresh invite link to rejoin.")
    return lines


async def automod_invite(channel: discord.TextChannel) -> list[str]:
    await channel.send("hey check out my server discord.gg/fake-invite-test")
    return ["Sent a fake invite link. Should get deleted/punished if anti_invite is on."]


async def automod_caps(channel: discord.TextChannel) -> list[str]:
    await channel.send("THIS IS A LOUD ALL CAPS MESSAGE TO TEST AUTOMOD CAPS DETECTION")
    return ["Sent an all-caps message. Should get flagged if anti_caps is on."]


async def automod_spam(channel: discord.TextChannel) -> list[str]:
    await asyncio.gather(*(channel.send("spam test message") for _ in range(4)))
    return ["Sent 4 copies of the same message concurrently. Should get flagged if anti_spam is on."]


async def automod_mentions(guild: discord.Guild, channel: discord.TextChannel) -> list[str]:
    members = [m async for m in guild.fetch_members(limit=None) if not m.bot]
    targets = members[:6]
    if len(targets) < 2:
        return ["Not enough non-bot members in this server to test mention spam."]
    mention_text = " ".join(m.mention for m in targets)
    await channel.send(f"{mention_text} mention spam test")
    return [f"Mentioned {len(targets)} members. Should get flagged if max_mentions is lower than that count."]


async def ghostping(guild: discord.Guild, channel: discord.TextChannel) -> list[str]:
    members = [m async for m in guild.fetch_members(limit=None) if not m.bot]
    if not members:
        return ["No non-bot member found to ghost-ping."]
    target = members[0]
    msg = await channel.send(target.mention)
    await msg.delete()
    return [f"Pinged {target} then deleted the message. Should get flagged if anti-ghostping is on."]


class TestGroup(app_commands.Group):
    async def interaction_check(self, interaction: discord.Interaction) -> bool:
        if interaction.user.id != TEST_OWNER_ID:
            await interaction.response.send_message("You're not allowed to use this command.", ephemeral=True)
            return False
        return True


test_group = TestGroup(
    name="test",
    description="Run a defensive-feature test against this server (anti-nuke / automod / ghost-ping)",
    default_permissions=discord.Permissions(administrator=True),
)


async def _run(interaction: discord.Interaction, coro) -> None:
    await interaction.response.defer(ephemeral=True)
    try:
        lines = await coro
        lines.append("Check the main bot's modlog channel for results.")
        await interaction.followup.send("\n".join(lines))
    except Exception as err:  # noqa: BLE001 - surfaced to the test runner, not swallowed
        await interaction.followup.send(f"Test failed: {err}")


@test_group.command(name="nuke-channels", description="Create + rapidly delete channels (tests /security nuke)")
async def nuke_channels_cmd(interaction: discord.Interaction):
    await _run(interaction, nuke_channels(interaction.guild))


@test_group.command(name="nuke-roles", description="Create + rapidly delete roles (tests /security nuke)")
async def nuke_roles_cmd(interaction: discord.Interaction):
    await _run(interaction, nuke_roles(interaction.guild))


@test_group.command(name="nuke-webhooks", description="Create + rapidly delete webhooks in a channel (tests webhook-nuke detection)")
@app_commands.describe(channel="Channel to create webhooks in")
async def nuke_webhooks_cmd(interaction: discord.Interaction, channel: discord.TextChannel):
    await _run(interaction, nuke_webhooks(channel))


@test_group.command(
    name="nuke-permissions",
    description="Create temp channels and rapidly lock everyone out of them (tests permission-nuke detection)",
)
async def nuke_permissions_cmd(interaction: discord.Interaction):
    await _run(interaction, nuke_permissions(interaction.guild))


@test_group.command(name="nuke-emojis", description="Create + rapidly delete emojis (tests emoji-nuke detection)")
async def nuke_emojis_cmd(interaction: discord.Interaction):
    await _run(interaction, nuke_emojis(interaction.guild))


@test_group.command(
    name="nuke-kicks",
    description="Rapidly kick real members at once (tests kick-nuke detection). Only use on consenting accounts.",
)
@app_commands.describe(target1="Member to kick", target2="Member to kick", target3="Member to kick", target4="Member to kick")
async def nuke_kicks_cmd(
    interaction: discord.Interaction,
    target1: discord.User,
    target2: Optional[discord.User] = None,
    target3: Optional[discord.User] = None,
    target4: Optional[discord.User] = None,
):
    targets = [t for t in (target1, target2, target3, target4) if t is not None]
    await _run(interaction, nuke_kicks(interaction.guild, targets))


@test_group.command(
    name="nuke-bans",
    description="Rapidly ban then auto-unban real members at once (tests ban-nuke detection). Only use on consenting accounts.",
)
@app_commands.describe(target1="Member to ban", target2="Member to ban", target3="Member to ban", target4="Member to ban")
async def nuke_bans_cmd(
    interaction: discord.Interaction,
    target1: discord.User,
    target2: Optional[discord.User] = None,
    target3: Optional[discord.User] = None,
    target4: Optional[discord.User] = None,
):
    targets = [t for t in (target1, target2, target3, target4) if t is not None]
    await _run(interaction, nuke_bans(interaction.guild, targets))


@test_group.command(name="automod-invite", description="Send a fake invite link (tests automod anti_invite)")
@app_commands.describe(channel="Channel to post in")
async def automod_invite_cmd(interaction: discord.Interaction, channel: discord.TextChannel):
    await _run(interaction, automod_invite(channel))


@test_group.command(name="automod-caps", description="Send an ALL-CAPS message (tests automod anti_caps)")
@app_commands.describe(channel="Channel to post in")
async def automod_caps_cmd(interaction: discord.Interaction, channel: discord.TextChannel):
    await _run(interaction, automod_caps(channel))


@test_group.command(name="automod-spam", description="Send the same message 4x quickly (tests automod anti_spam)")
@app_commands.describe(channel="Channel to post in")
async def automod_spam_cmd(interaction: discord.Interaction, channel: discord.TextChannel):
    await _run(interaction, automod_spam(channel))


@test_group.command(
    name="automod-mentions",
    description="Ping several real members at once (tests automod max_mentions). Will actually ping people.",
)
@app_commands.describe(channel="Channel to post in")
async def automod_mentions_cmd(interaction: discord.Interaction, channel: discord.TextChannel):
    await _run(interaction, automod_mentions(interaction.guild, channel))


@test_group.command(
    name="ghostping",
    description="Ping a member then delete the message fast (tests ghost-ping detection). Will actually ping someone.",
)
@app_commands.describe(channel="Channel to post in")
async def ghostping_cmd(interaction: discord.Interaction, channel: discord.TextChannel):
    await _run(interaction, ghostping(interaction.guild, channel))


tree.add_command(test_group, guild=GUILD)


@client.event
async def on_ready():
    await tree.sync(guild=GUILD)
    print(f"[test-bot] Logged in as {client.user}")


client.run(TEST_BOT_TOKEN)
