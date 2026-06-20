"""
Standalone test tool (Python) for trying out the main bot's defensive
features (anti-nuke, automod, ghost-ping detection) against a server you
own. Run this with a SEPARATE "tester" bot account's token -- never the
main bot's token, since the main bot is exempt from its own anti-nuke
punishment and can't usefully test itself.

Setup:
  1. pip install discord.py
  2. Create a second Discord application + bot ("Nuke Tester" or similar).
  3. In the Bot tab, enable the "Server Members Intent".
  4. Invite it to your server with: Manage Channels, Manage Roles,
     Send Messages.
  5. Run one of the tests below from your own computer (not the panel):

     TEST_BOT_TOKEN=xxx TEST_GUILD_ID=xxx python scripts/test_bot.py <test>

Available <test> values:
  nuke-channels     create + rapidly delete a few channels (tests /security nuke)
  nuke-roles        create + rapidly delete a few roles (tests /security nuke)
  automod-invite    sends a fake discord.gg invite link (tests anti_invite)
  automod-caps      sends a long ALL-CAPS message (tests anti_caps)
  automod-spam      sends the same message 4x quickly (tests anti_spam)
  automod-mentions  pings several real members at once (tests max_mentions)
  ghostping         sends a message pinging a member, then deletes it fast

automod-* and ghostping tests also need TEST_CHANNEL_ID (a text channel
the tester bot can send messages in). automod-mentions and ghostping will
actually ping real members of your server -- use a test server or warn
people first.
"""
import asyncio
import os
import sys

import discord

TESTS = [
    "nuke-channels",
    "nuke-roles",
    "automod-invite",
    "automod-caps",
    "automod-spam",
    "automod-mentions",
    "ghostping",
]


async def nuke_channels(guild: discord.Guild):
    print("[test] Creating 4 temporary channels...")
    channels = []
    for i in range(4):
        ch = await guild.create_text_channel(f"nuke-test-{i + 1}")
        channels.append(ch)
        print(f"[test] Created #{ch.name}")

    print("[test] Waiting 2s, then deleting all of them rapidly...")
    await asyncio.sleep(2)
    for ch in channels:
        try:
            await ch.delete(reason="nuke-test")
            print(f"[test] Deleted #{ch.name}")
        except discord.HTTPException as err:
            print(f"[test] Failed to delete {ch.name}: {err}")


async def nuke_roles(guild: discord.Guild):
    print("[test] Creating 4 temporary roles...")
    roles = []
    for i in range(4):
        role = await guild.create_role(name=f"nuke-test-role-{i + 1}")
        roles.append(role)
        print(f"[test] Created role {role.name}")

    print("[test] Waiting 2s, then deleting all of them rapidly...")
    await asyncio.sleep(2)
    for role in roles:
        try:
            await role.delete(reason="nuke-test")
            print(f"[test] Deleted role {role.name}")
        except discord.HTTPException as err:
            print(f"[test] Failed to delete {role.name}: {err}")


def require_channel_id() -> int:
    channel_id = os.environ.get("TEST_CHANNEL_ID")
    if not channel_id:
        print("This test needs TEST_CHANNEL_ID set to a text channel the tester bot can post in.")
        sys.exit(1)
    return int(channel_id)


async def automod_invite(guild: discord.Guild, client: discord.Client):
    channel = client.get_channel(require_channel_id())
    print("[test] Sending a message with a fake invite link...")
    await channel.send("hey check out my server discord.gg/fake-invite-test")
    print("[test] Sent. The message should get deleted/punished if anti_invite is on.")


async def automod_caps(guild: discord.Guild, client: discord.Client):
    channel = client.get_channel(require_channel_id())
    print("[test] Sending an all-caps message...")
    await channel.send("THIS IS A LOUD ALL CAPS MESSAGE TO TEST AUTOMOD CAPS DETECTION")
    print("[test] Sent. The message should get flagged if anti_caps is on.")


async def automod_spam(guild: discord.Guild, client: discord.Client):
    channel = client.get_channel(require_channel_id())
    print("[test] Sending the same message 4 times quickly...")
    for i in range(4):
        await channel.send("spam test message")
        print(f"[test] Sent copy {i + 1}/4")
        await asyncio.sleep(0.5)
    print("[test] Done. The last message should get flagged if anti_spam is on.")


async def automod_mentions(guild: discord.Guild, client: discord.Client):
    channel = client.get_channel(require_channel_id())
    print("[test] Fetching real members to mention (this WILL ping them)...")
    targets = []
    async for member in guild.fetch_members(limit=None):
        if not member.bot:
            targets.append(member)
        if len(targets) >= 6:
            break
    if len(targets) < 2:
        print("Not enough non-bot members in this server to test mention spam.")
        sys.exit(1)
    mention_text = " ".join(m.mention for m in targets)
    print(f"[test] Mentioning {len(targets)} members...")
    await channel.send(f"{mention_text} mention spam test")
    print("[test] Sent. Should get flagged if max_mentions is lower than the mention count.")


async def ghostping(guild: discord.Guild, client: discord.Client):
    channel = client.get_channel(require_channel_id())
    target = None
    async for member in guild.fetch_members(limit=None):
        if not member.bot:
            target = member
            break
    if not target:
        print("No non-bot member found to ghost-ping.")
        sys.exit(1)
    print(f"[test] Pinging {target} then deleting the message fast...")
    msg = await channel.send(target.mention)
    await asyncio.sleep(1)
    await msg.delete()
    print("[test] Done. Should get flagged if anti-ghostping is on.")


async def main():
    if len(sys.argv) < 2 or sys.argv[1] not in TESTS:
        print(f"Usage: TEST_BOT_TOKEN=xxx TEST_GUILD_ID=xxx [TEST_CHANNEL_ID=xxx] python scripts/test_bot.py <test>")
        print(f"<test> must be one of: {', '.join(TESTS)}")
        sys.exit(1)

    test = sys.argv[1]
    token = os.environ.get("TEST_BOT_TOKEN")
    guild_id = os.environ.get("TEST_GUILD_ID")
    if not token or not guild_id:
        print("Set TEST_BOT_TOKEN and TEST_GUILD_ID env vars before running.")
        sys.exit(1)

    intents = discord.Intents.default()
    intents.members = True
    client = discord.Client(intents=intents)

    @client.event
    async def on_ready():
        print(f"[test] Logged in as {client.user}")
        guild = await client.fetch_guild(int(guild_id))

        runners_no_client = {"nuke-channels": nuke_channels, "nuke-roles": nuke_roles}
        runners_with_client = {
            "automod-invite": automod_invite,
            "automod-caps": automod_caps,
            "automod-spam": automod_spam,
            "automod-mentions": automod_mentions,
            "ghostping": ghostping,
        }

        if test in runners_no_client:
            await runners_no_client[test](guild)
        else:
            await runners_with_client[test](guild, client)

        print("[test] Finished. Check the main bot's modlog channel for results.")
        await client.close()

    await client.start(token)


if __name__ == "__main__":
    asyncio.run(main())
