# AntiFreecam — Plugin ta kivabe kaj korbe (Bangla)

> Short e: **Y20 er niche je jinis tumi sotti kore dekhte parba seta vanilla thakbe,
> ar je jinis pathor er bhitore luka — seta freecam/x-ray e "void" dekhabe.**
> Kono fake stone na. Kono detection na. Kono bug na.

---

## 1. Keno "freecam detect" kora jay na

Freecam akta **client-side** mod. Tumi freecam on korle server kichui jane na —
server er kache shudhu tomar normal nora-chora (movement) jay. Tai "freecam on
korle X koro, off thakle Y koro" — eta **server theke kora ekebare osombhob**.

Tomar age er plugin gula erkomi try korto (movement diye guess) — tai eto bug,
eto fake stone. Amra **detect korar cheshtai korbo na**.

## 2. Tahole kivabe? — Occlusion (ja dekhar kotha na, seta pathai-i na)

Asol buddhi simple: **freecam shudhu oi block gula dekhate pare ja server age
theke client ke pathaise.** Tai amra Y20 er niche **luka block gula client ke
pathai-i na** — bodole **air/void** pathai.

Y20 er niche protita block er jonno:

| Block ta kemon? | Ki pathabo? | Keno |
|---|---|---|
| Charpashe (6 dik) puro pathor — kono cave/air er sathe laga nai (luka ore) | **air (void)** | Tumi onnitei eta dekhte parba na; freecam diye dhukleo void dekhbe |
| Cave / tunnel / tomar khora jaygar gaye laga | **asol block** | Tumi sotti dekho — tai vanilla rakha lagbe |

> Mane: **tomar cave-er deyal gula asol-i thake** (oigula air er sathe laga),
> ar deyal-er pichoner luka pathor void hoye jay. Single block deyal diye seleo
> void dekhbe na — karon oi deyal block ta nije asol e pathano.

## 3. Result (tumi ja chao)

- 🟢 **Tomar cave / mine = 100% vanilla.** Fake stone kothao nai. Mine dekhte paro,
  cave dekhte paro — normal er motoi.
- 🟢 **Freecam / x-ray pathor er bhitore dhukle = shudhu void.** Luka ore vein dekhbe na,
  karon oi block gula kokhono asol e pathano hoy nai.
- 🟢 Y20 er **upore sob purai normal** vanilla.

## 4. Bug keno hobe na (mine korar somoy) — "reveal on mine"

Boro bishoy: tumi akta block bhangle, tar pichoner luka block ta ekhon dekha jaoa
uchit — kintu client er kache oita ekhono **air (void)** ase. Tahole void gorto
dekhbe! (Eta-i purono plugin er boro bug hoto.)

Eta thekano: tumi block bhanglei, plugin sathe sathe oi 6 ta pasher (Y20-er niche,
solid) block er **asol roop** pathie dey (`sendBlockChange`). Tai —

> Tumi pathor khure ore er kache pouchaleo, ore ta thik jokhon ber hobe tokhon-i
> dekhabe — **kono void gorto thakbe na.** Hubohu vanilla mining.

Break, place, ar TNT/creeper bisfaron — sob handle kora.

## 5. "Stone" na, "void" keno

Purono plugin luka jayga **STONE** diye bhorto (tai pathor dekhte). Amra **air**
diye bhori — tai freecam e pathor na, **void** dekhabe. Ei ekta-i tumi cheyechile.

## 6. Setting (config.yml)

- `hideBelowY: 20` — kon Y-er niche kaj korbe (tumi chaile poriborton korte parba)
- `enabled: true` — on/off
- `/antifreecam bypass` — staff/admin der jonno (tara sob asol dekhe)

## 7. Ekta choto limitation (sotyi bola dorkar)

Chunk gula 16x16. Chunk er border-er gaye 1-block-potota pathor freecam e ektu
dekha jete pare (ekta jhapsa grid er moto) — kintu **luka ore vein dekhabe na**.
Eta pore aro tight kora jabe (pasher chunk-er data pore). Tomar normal khelay eta
**kichui poriborton kore na** — eta shudhu freecam er chokhe sামান্য.

---

**TL;DR:** Detect na — bodole Y20-er niche luka block gula client ke **air/void**
pathai, ar cave-er gaye-laga block asol pathai. Cave vanilla, freecam void,
mine korle bug nai.
