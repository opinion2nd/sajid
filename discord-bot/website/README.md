# Brother Craft — Landing Page & Legal Pages

A self-contained static website for the Brother Craft Discord bot. No build
step — just three HTML files:

- `index.html` — landing page (features + invite button)
- `terms.html` — Terms of Service
- `privacy.html` — Privacy Policy

## Before you publish

1. In `index.html`, replace `YOUR_CLIENT_ID` in the "Add to Discord" link with
   your application's real client ID (Discord Developer Portal → your app →
   General Information → Application ID).
2. (Optional) Adjust the contact email in `terms.html` / `privacy.html`.

## Hosting (free options)

Any static host works. Two easy, free choices:

### GitHub Pages
1. Push this repo to GitHub.
2. Repo → Settings → Pages → Source: deploy from a branch, folder
   `/discord-bot/website` (or copy these files to a `/docs` folder).
3. Your pages will be live at
   `https://<user>.github.io/<repo>/index.html`.

### Cloudflare Pages / Netlify / Vercel
Point the project at this folder; set the build command to "none" and the
output/publish directory to `discord-bot/website`.

## Discord verification checklist (needed once the bot is in 100+ servers)

Discord requires verified bots to provide these. The pages above cover the
URLs; the rest you do in the Developer Portal:

- [ ] **Terms of Service URL** → `https://<your-domain>/terms.html`
- [ ] **Privacy Policy URL** → `https://<your-domain>/privacy.html`
      (add both under Developer Portal → your app → General Information)
- [ ] **Enable 2FA** on your Discord account (required to submit for
      verification): User Settings → My Account → Enable Two-Factor Auth.
- [ ] Make sure the bot's **Privileged Gateway Intents** match what it uses
      (Server Members + Message Content).
- [ ] Submit for verification once the bot reaches ~75 servers (Discord opens
      the application around then).
