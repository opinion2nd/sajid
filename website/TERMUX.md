# Running Brother Craft on Termux (Android)

Run the whole marketplace on your phone with Termux. This runs it **locally**
(only your phone can see `localhost:3000`). To share it online, add a free tunnel
(Cloudflare Tunnel / ngrok) — see the bottom.

> Needs a decent phone (4GB+ RAM) and patience — the first install + build takes
> a few minutes. Demo mode: no bKash/Discord/cloud accounts required.

## 1. Install Termux

Install **Termux** from F-Droid (https://f-droid.org/packages/com.termux/) —
the Play Store version is outdated.

## 2. Get the project

If you have the ZIP, move it into Termux and unzip:

```bash
pkg install -y unzip
termux-setup-storage          # allow storage access (tap Allow)
cd ~
unzip /sdcard/Download/brother-craft.zip -d brother-craft
cd brother-craft/website
```

(Or clone with git: `pkg install git && git clone <your-repo> && cd sajid/website`)

## 3. One command to set everything up

```bash
bash scripts/termux-setup.sh
```

This installs Node + PostgreSQL, creates the database, installs dependencies,
runs migrations, and seeds demo data.

## 4. Start the site

```bash
bash scripts/termux-run.sh
```

Open **http://localhost:3000** in your phone's browser. 🎉

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
