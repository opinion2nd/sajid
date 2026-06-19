# Running Brother Craft on Termux (Android)

Run the whole marketplace on your phone. This runs it **locally**
(`localhost:3000`); add a free tunnel to share it online (see the bottom).

> **Important:** the database engine (Prisma) does **not** run on bare Termux
> (Android/bionic libc). So we install a small **Debian** inside Termux — that
> gives a normal Linux where everything works. Needs a decent phone (4GB+ RAM)
> and patience. Demo mode: no bKash/Discord/cloud accounts required.

## 1. Install Termux + storage

Install **Termux** from F-Droid (https://f-droid.org/packages/com.termux/) —
the Play Store version is outdated. Then:

```bash
termux-setup-storage     # tap Allow
```

(Download the project ZIP from chat — it lands in `/sdcard/Download/`.)

## 2. Install Debian inside Termux

```bash
pkg update -y && pkg install -y proot-distro
proot-distro install debian
proot-distro login debian
```

You are now **inside Debian** (prompt shows `root@localhost`). Do the rest here.

## 3. Get the project + set everything up

```bash
cd ~
apt-get update && apt-get install -y unzip
unzip -o /sdcard/Download/brother-craft.zip -d brother-craft
cd brother-craft
bash scripts/debian-setup.sh
```

`debian-setup.sh` installs Node + PostgreSQL, creates the database, installs
dependencies, migrates and seeds demo data.

## 4. Start the site

```bash
bash scripts/debian-run.sh
```

Open **http://localhost:3000** in your phone's browser. 🎉

> Next time: `proot-distro login debian` → `cd ~/brother-craft` →
> `bash scripts/debian-run.sh`.

Demo logins (password `password123`):
`admin@brothercraft.dev` · `seller@brothercraft.dev` · `buyer@brothercraft.dev`

To stop: press `Ctrl + C` in Termux. To start again later, just run
`bash scripts/termux-run.sh` (no need to set up again).

## 5. (Optional) Put it online with a free tunnel

While the site is running, open a **second Termux session** (swipe from the left
edge → New session) and run a Cloudflare tunnel:

```bash
pkg install -y cloudflared
cloudflared tunnel --url http://localhost:3000
```

It prints a public `https://....trycloudflare.com` link anyone can open. Keep
both Termux sessions running. (ngrok works too.)

> Note: the phone must stay on and connected for the link to work. For a
> permanent always-on site, a small VPS or Railway/Render is better — see DEPLOY.md.
