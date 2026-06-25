# AntiFreecam — Plugin ta kivabe kaj korbe (Bangla)

> Short e: **Y20 er niche je jinis tumi sotti kore dekhte parba seta vanilla thakbe,
> ar je jinis pathor er bhitore luka — seta freecam/x-ray e khujleo pabe na (shudhu
> nirot pathor/deepslate dekhbe).** Kono visible fake stone na. Kono detection na. Kono bug na.

---

## 1. Keno "freecam detect" kora jay na

Freecam akta **client-side** mod. Tumi freecam on korle server kichui jane na —
server er kache shudhu tomar normal nora-chora (movement) jay. Tai "freecam on
korle X koro, off thakle Y koro" — eta **server theke kora ekebare osombhob**.

Tomar age er plugin gula erkomi try korto (movement diye guess) — tai eto bug,
eto fake stone. Amra **detect korar cheshtai korbo na**.

## 2. Tahole kivabe? — Occlusion (ja dekhar kotha na, seta luka rakhi)

Asol buddhi simple: **freecam shudhu oi block gula dekhate pare ja server age
theke client ke pathaise.** Tai amra Y20 er niche **luka block gula client ke
asol roope pathai na** — bodole **deepslate** (akta nirot pathor) pathai.

Y20 er niche protita block er jonno:

| Block ta kemon? | Ki pathabo? | Keno |
|---|---|---|
| Charpashe (6 dik) puro pathor — kono cave/air er sathe laga nai (luka ore) | **deepslate** | Tumi onnitei eta dekhte parba na (deyal-er pichone); freecammer dhukleo shudhu nirot pathor pabe |
| Cave / tunnel / tomar khora jaygar gaye laga | **asol block** | Tumi sotti dekho — tai vanilla rakha lagbe |

> Mane: **tomar cave-er deyal gula asol-i thake** (oigula air er sathe laga),
> ar deyal-er pichoner luka pathor deepslate hoye jay. Kintu oi deepslate tumi
> kokhono dekho na — eta sob somoy deyal-er pichone luka.

## 3. Keno "void" na, "deepslate" — ekta joruri kotha

Tumi cheyechile freecam **void** dekhuk. Kintu ekta jinis bujhte hobe:

> **Server tomar asol camera ar freecam camera — dujon-ke EKI block data pathay.**
> Freecam shudhu camera-ta soray. Tai luka jayga jodi **air/void** kori, tahole
> **tumi nije-o** pathor er bhitor diye dekhte parba (see-through) — eta vanilla na,
> eta bug. (Eta-i age hoyechilo: tumi shudhu cave er kathamo dekhchile, block na.)

Tai tomar nijer view vanilla rakhte hole luka jayga **nirot (solid)** hotei hobe.
Deepslate dile —
- 🟢 Tumi cave-e thakle sob asol-i dekho (occlusion er karone deepslate chokhe pore na) → **100% vanilla**.
- 🟢 Freecammer pathore dhukle shudhu **nirot deepslate** dekhe — ore/cave kichu khuje pay na = **se hare gelo**.

Freecam "void" na "pathor" dekhlo — kaj ekii: **se kichui bujhte pare na.**

## 4. Result (tumi ja chao)

- 🟢 **Tomar cave / mine = 100% vanilla.** Visible fake stone kothao nai.
- 🟢 **Freecam / x-ray pathor er bhitore dhukle = shudhu nirot deepslate.** Luka
  ore vein dekhbe na.
- 🟢 Y20 er **upore sob purai normal** vanilla.
- 🟢 Normal player Y20-er niche namleo kono fake/abnormal kichu dekhbe na.

## 5. Bug keno hobe na (mine korar somoy) — "reveal on mine"

Tumi akta block bhangle, tar pichoner luka block ta ekhon dekha jaoa uchit — kintu
client er kache oita ekhono **deepslate** (mask). Tahole asol ore-er bodole pathor
dekhte. Eta thekano: tumi block bhanglei, plugin sathe sathe oi 6 ta pasher
(Y20-er niche, solid) block er **asol roop** pathie dey (`sendBlockChange`). Tai —

> Tumi pathor khure ore er kache pouchaleo, ore ta thik jokhon ber hobe tokhon-i
> asol dekhabe — **kono fake pathor/gorto thakbe na.** Hubohu vanilla mining.

Break, place, ar TNT/creeper bisfaron — sob handle kora.

## 6. Setting (config.yml)

- `hideBelowY: 20` — kon Y-er niche kaj korbe
- `maskBlock: DEEPSLATE` — luka jayga ki diye bhorbe (nirot block-i dite hobe; AIR
  diyo na, tahole see-through hobe)
- `enabled: true` — on/off
- `/antifreecam bypass` — staff/admin der jonno (tara sob asol dekhe)

## 7. Ekta choto limitation (sotyi bola dorkar)

Chunk gula 16x16. Chunk er border-er gaye 1-block-potota pathor freecam e ektu
dekha jete pare (ekta jhapsa grid er moto) — kintu **luka ore vein dekhabe na**.
Tomar normal khelay eta **kichui poriborton kore na** — eta shudhu freecam er chokhe samanyo.

---

**TL;DR:** Detect na — bodole Y20-er niche luka block gula client ke **deepslate**
pathai, ar cave-er gaye-laga block asol pathai. Cave 100% vanilla, freecammer shudhu
nirot pathor paay (ore/cave khuje pay na), mine korle bug nai. ("Void" possible na —
karon tahole tumi nije-o see-through dekhte.)
