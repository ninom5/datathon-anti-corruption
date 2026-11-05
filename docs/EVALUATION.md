# Evaluacija AI procjene

Metoda je u `docs/ANOMALY_DETECTION.md` §4.6. Ovdje su **samo izmjerene brojke**, s datumom, modelom, verzijom prompta, n i troškom. Brojke koje još nisu izmjerene označene su kao takve.

- **Model:** `claude-haiku-5-5` (effort `medium`), strukturirani izlaz, validator iz §4.4 i §4.7.
- **Mjerenje:** "oznaka" znači `oznaka ≠ nema_oznake`. Odgovor koji validator odbaci računa se kao "bez oznake", jer korisnik tada ne vidi ništa.
- **Sirovi rezultati** (svaki slučaj, razlozi, trošak) su u `work/eval/`.

## 1. Sintetički skup (40 slučajeva)

**Skup:** `apps/api/src/main/resources/ai/eval/synthetic-cases-v4.json`, generator `work/eval/generate_synthetic_cases.py`.
- 21 slučaj treba označiti, 19 ne.
- Očekivana oznaka je zapisana prije svakog runa.
- Scenariji:
  - nesklad djelatnosti (NKD) i mlada tvrtka;
  - jednostavna nabava iznad praga;
  - generički predmet, sam i uz pravilo F;
  - CPV koji ne odgovara predmetu;
  - obrazloženje plaćanja koje proturječi iznosima;
  - odstupanje od usporedne skupine;
  - uobičajeni ugovori;
  - pravilo s kontekstom koji ga objašnjava (in-house, opskrba energijom, objavljen razlog izmjene, ponavljajuće gorivo);
  - mali ugovori s malo podataka.
- Sve su organizacije fiktivne (SINTETIČKI PODACI ZA DEMONSTRACIJU). NKD i datum osnivanja su fixture jer Sudreg još nije spojen.

**Ispravci skupa (v1 → v4), samo greške u podacima, bez promjene prompta:**
- v1 → v2: roba je imala `vrsta_predmeta: "usluge"`, a jedan CPV nije odgovarao predmetu. Model je ta proturječja ispravno označio, pa su očekivane oznake u v1 bile pogrešne za 4 slučaja.
- v2 → v3: kontekst je dobio prag jednostavne nabave.
- v3 → v4: pojedinačni ugovor iz skupine pravila F bio je iznad praga.

Nakon v4 skup se više ne mijenja.

| Datum | Prompt | Skup | n | Preciznost | Odziv | Lažno pozitivni (od 19) | Točna oznaka | Odbačeno validatorom | Trošak |
|---|---|---|---|---|---|---|---|---|---|
| 2026-11-03 | ai-flag-v2 | v1 | 40 | 0,833 | 0,952 | 0,211 | 0,800 | 0 | 0,045 USD |
| 2026-11-03 | ai-flag-v2 | v2 | 40 | 0,952 | 0,952 | 0,053 | 0,875 | 0 | 0,042 USD |
| 2026-11-04 | ai-flag-v2 | v3 | 40 | 0,909 | 0,952 | 0,105 | 0,850 | 0 | 0,046 USD |
| 2026-11-04 | ai-flag-v2 | v4, run 1 | 40 | 0,955 | 1,000 | 0,053 | 0,875 | 0 | 0,046 USD |
| 2026-11-04 | ai-flag-v2 | v4, run 2 | 40 | 0,952 | 0,952 | 0,053 | 0,900 | 0 | 0,045 USD |
| **2026-11-04** | **ai-flag-v3** | **v4, run 1** | **40** | **1,000** | **0,905** | **0,000** | **0,875** | **0** | **0,045 USD** |
| **2026-11-04** | **ai-flag-v3** | **v4, run 2** | **40** | **1,000** | **0,952** | **0,000** | **0,900** | **0** | **0,045 USD** |

**Čitanje:**
- n = 40 je malo, pa jedan slučaj pomiče odziv za oko 0,05. Isti prompt na istom skupu dao je u dva runa različite promašaje.
- Razliku između v2 i v3 na sintetici treba čitati kao "u istom rasponu".
- v3 je uveden zbog **stvarnih** ugovora (§2), ne zbog sintetike.
- Ponovljeni promašaj v3 je "mlada tvrtka" (S39, S40): tvrtka osnovana nekoliko mjeseci prije ugovora od 180.000–240.000 € dobiva "nema_oznake".

## 2. Stvarni uzorak (60 ugovora)

**Uzorak:** `work/eval/real-sample-seed44.csv`, ponovljiv odabir (`md5(id + "44")`) iz stvarnih ugovora klase A. Obrti, fizičke osobe i dobavljači bez OIB-a nisu u uzorku. Upute za označavanje su u `work/eval/README.md`.

**Ručne oznake: još ih nema.** Zato **preciznost, odziv, stopa lažno pozitivnih i slaganje dvaju ocjenjivača nisu izmjereni** i ne objavljuju se.

Izmjereno bez ručnih oznaka (2026-11-04, `ai-flag-v3`, n = 60, 0 odbijenih validatorom, 0,067 USD):

| Mjera | Vrijednost |
|---|---|
| Udio ugovora s oznakom "vrijedi provjeriti" | 12 / 60 (20 %) |
| Cohenova kappa: AI oznaka prema "ima indikator pravila *vrijedi provjeriti*" | 0,444 |

**Tijek po verzijama na istom uzorku:**

| Prompt i kontekst | AI oznaka | Kappa prema pravilima |
|---|---|---|
| ai-flag-v2 | 30 / 60 | 0,033 |
| ai-flag-v3 | 15 / 60 | 0,135 |
| ai-flag-v3 + vrijednost s PDV-om | 12 / 60 | 0,444 |
| ai-flag-v3 + PDV + Sudreg (NKD, datum osnivanja), 2026-11-05, 0,069 USD | 13 / 60 | 0,057 |

Ručnim pregledom oznaka v2 nađena su dva sustavna izvora oznaka bez osnove:
1. **"Nabava izuzeta od primjene ZJN" ispod praga.** To je uobičajeno jer se ispod praga ZJN ne primjenjuje. Popravljeno pravilima 12–14 u v3.
2. **Isplata veća od ugovorene vrijednosti za točno 25 %.** To je PDV, jer kontekst nije imao vrijednost s PDV-om. Popravljeno poljem `ugovor.vrijednost_s_pdv_eur`.

**Sa Sudreg podacima (05.11.):** 9 od 13 oznaka sada se temelji na neskladu registrirane djelatnosti (NKD) i predmeta. Dio njih je slab signal: glavna djelatnost iz registra ne ograničava druge djelatnosti tvrtke (npr. IT konzultant isporučuje kućišta za kamere). Model to navodi u legitimnim objašnjenjima, ali oznaka ostaje "vrijedi provjeriti". Bez ručnih oznaka nije poznato koliko je tih oznaka korisno; to je prvo pitanje za ručni pregled.

Kappa prema pravilima mjeri podudaranje, ne točnost. AI namjerno traži signale koje pravila ne mjere (prompt, pravilo 8), pa visoka kappa nije cilj.

## 3. Ukupni trošak razvoja i evaluacije

Do 2026-11-04 je potrošeno ukupno **0,66 USD** (izvor: `llm_usage`):
- 549 poziva;
- 3,09 M ulaznih i 0,71 M izlaznih tokena.

Jedan poziv stoji oko 0,0011–0,0016 USD. Svih 2.212 stvarnih ugovora koji ispunjavaju kriterije odabira stajalo bi oko 2,5–3,5 USD, ali ta procjena nije izmjerena punim runom.

## 4. Što slijedi
- Dva člana tima upisuju oznake u `work/eval/real-sample-seed44.csv`. Zatim `POST /api/admin/ai/eval/real` (CSV u tijelu zahtjeva) računa metrike iz §4.6 i one se upisuju ovdje.
- Kad se spoji Sudreg (#46), NKD i datum osnivanja postaju stvarni podaci. Tada se ponavljaju obje evaluacije.
