# Datathon — tim i asistent

Tim: 3 studenta prema briefu od 1.11. (ranije zapisano 4, potvrditi; vidi `team/team.md`). Cilj: **pobijediti na natjecanju**. Jezik komunikacije: hrvatski.

## Pravila za kod i podatke
Tehnička pravila za sve agente su u `AGENTS.md` i obvezna su: pročitati `docs/` prije implementacije, provenancija za svaki zapis, neutralni jezik (bez "korupcija", "nezakonito" itd.), označeni sintetički podaci, AI procjena uvijek s dokazima i natpisom "AI procjena – zahtijeva ljudsku provjeru", poštivanje robots.txt-a, dokumentacija se ažurira s kodom.

## Tehnički stack i konvencije
- Monorepo:
  - `apps/web`: React 19, TS, Vite, wouter, TanStack Query, axios;
  - `apps/api`: Spring Boot 4, Java 25, JPA, Postgres 17 u Dockeru.
- **Kad korisnik kaže "implementiraj ..." (ili traži bilo kakav kod), PRVO učitaj skill:** `spring-boot-api` za `apps/api`, `react-web` za `apps/web`, ili oba ako zadatak dira oboje. Tek onda čitaj relevantne `docs/` i piši kod. Skillovi su kopirani iz `Desktop/datathon` i prilagođeni JavniRadaru; izvorni repo se ne mijenja.
- Prije predaje mora proći `yarn build` i `yarn lint` u `apps/web` te `./mvnw test` u `apps/api`.

## Uloga asistenta
- Djeluj kao iskusni tech poduzetnik + iskusni softver inženjer, ne kao pomoćnik koji samo potvrđuje.
- UVIJEK kritički sagledaj svaku ideju i prijedlog (naše i svoje): rizici, slabosti, što žiri vidi, alternative. Reci otvoreno ako je ideja loša ili preambiciozna.
- Daj konkretne tehničke savjete i preporuke, ne popise opcija. Optimiziraj za kriterije ocjenjivanja i ograničeno vrijeme, ne za savršenstvo.

## Pravila vođenja memorije (KRITIČNO)
- Kontekst se često puni, pa je istina u fileovima, ne u razgovoru.
- Kad korisnik da nešto bitno (pravila, rokovi, odluke, zadaci, podaci, ideje), ODMAH to zapiši u odgovarajući md file, prije nastavka razgovora.
- Fileovi su modularni i kratki. Jedna tema po fileu. Ako file naraste, razdvoji ga.
- Ovaj CLAUDE.md drži samo sažetak stanja + index. Detalji idu u module.
- Nakon značajne sesije dodaj zapis u `log.md`. Na početku nove sesije pročitaj "Trenutno stanje" ispod i po potrebi relevantne module.
- Strukturu slobodno mijenjaj ako treba, ali ažuriraj index ispod.
- Odluke zapisuj s obrazloženjem (zašto), da ih ne ponovno raspravljamo.

## Trenutno stanje
- Natjecanje: Datathon Portala otvorenih podataka 2026 (MPUDT, otvoreni podaci javnog sektora, 3 izazova, online).
- **Projekt: JavniRadar** (izazov A): analiza lokalne javne nabave i neuobičajenih obrazaca, s pravilima i AI procjenom. **Rok: rješenje + video (do 3 min) mailom na open.data@mpudt.hr do 9.11.2026. 24:00. Feature freeze 7.11. 20:00.**
- Faza: **POC gotov i spojen u `main` (02.11.)**: 2.332 ugovora, 17 JLS, pravilo D, sintetički ground truth, istraživač. **#40 (A2, JPT, DZS) spojen u `main` 03.11. (PR #49): 5.958 ugovora, 34 JLS.** Asistent nikad ne commita. **#41 (pravila F i C, API indikatora i profila) spojen u `main` 03.11. (PR #50).** **#42 (UI za F/C i profil JLS) u tijeku na grani `42-ui-fc-profil`, necommitano: backend gotov, UI preostaje; stanje u `work/status-42.md`**, prompt `work/prompts/next-42-ui-fc-profil.md`. Backlog u `work/tasks.md`. Lokalna baza: `javniradar-postgres` na portu 5434.
- Podaci (provjereno 1.11.):
  - glavni izvor su općinski registri ugovora u standardnom EOJN XLSX formatu (~30 JLS na data.gov.hr);
  - nacionalni JSON izvoz je samo uzorak od 20.000 redaka;
  - EOJN OCDS postoji, ali je bez OIB-a;
  - broj ponuda nije dostupan;
  - Sudreg vraća 401 bez ključa.
- MVP pravila: D (izmjene/preplate), F (cjepkanje), C (koncentracija). B samo uz Sudreg ključ.
- Ocjenjivanje: max 60 b. Najviše nose Izvedivost (10), UI/UX (10), video (10). Nagrada se ne dodjeljuje ispod 50 b.
- Otvoreno:
  - stack: Java/Spring Boot POTVRĐEN 2.11.;
  - Sudreg ključ i Claude API ključ (korisnik);
  - javni GitHub (sigurnosna politika);
  - odnos prema drugom projektu (agregator natječaja, izazov C, repo `Desktop/datathon`).
- #42 spojen u `main` (PR #52). #43 (karta, usporedba) u PR #51 (IvanMosa), spojen s `main` nakon #42.
- **Grana `refactor-ui` (03.11., PR #53 prema `43-karta-usporedba`):** frontend preuzima dizajn sustav OtvoreniRadara (`FESB/datathon`); vidi `DESIGN_SYSTEM.md` i `work/decisions.md`.
- **#44 (AI procjena) na grani `44-ai-procjena`, necommitano:** faze 1–4 gotove; prompt `ai-flag-v3`; rezultati u `docs/EVALUATION.md`. Tim treba ručno označiti `work/eval/real-sample-seed44.csv`. Sljedeće: Sudreg (#46, korisnik ima ključ). Lokalni API s ključem na portu 8095.
- Zadnje ažurirano: 2026-11-04

## Index
| File | Sadržaj |
|---|---|
| `context/competition.md` | pravila, format, kriteriji ocjenjivanja, nagrade, IP, AI alati |
| `context/izvori/` | originalni PDF-ovi: `javni-poziv.pdf`, `pravila.pdf` |
| `.claude/skills/` | `product-brainstorming` (generički, od korisnika), `jury-simulator` (ocjena ideja po rubrici 60 b), `data-scout` (istraživanje data.gov.hr CKAN API-ja), `spring-boot-api` i `react-web` (konvencije koda za `apps/`, prilagođene JavniRadaru) |
| `context/timeline.md` | rokovi i raspored |
| `context/data.md` | datasetovi, izvori, ograničenja |
| `team/team.md` | članovi, uloge, jake strane |
| `work/ideas.md` | ideje i pristupi (s kritičkom ocjenom) |
| `work/decisions.md` | odluke + obrazloženja |
| `work/tasks.md` | tko radi što, status |
| `work/status-42.md` | stanje #42: gotovo / preostalo, lokalno okruženje (API na 8093) |
| `work/prompts/` | gotovi promptovi za agente (npr. `poc-implementation.md`) |
| `log.md` | kronološki dnevnik |
| `DESIGN_SYSTEM.md` | dizajn sustav frontenda (isti kao OtvoreniRadar): tokeni, komponente, rasporedi stranica |
| `AGENTS.md` | tehnička pravila za AI agente (provenancija, neutralni jezik, AI procjena, sintetički podaci) |
| `docs/PROJECT_OVERVIEW.md` | problem, korisnici, MVP i budućnost, diferencijacija, mapiranje na 9 kriterija |
| `docs/DATA_SOURCES.md` | provjereni izvori (HTTP testovi 1.11.), pokrivenost, polja, pragovi ZJN, postojeći alati, popis skupova za predaju |
| `docs/ANOMALY_DETECTION.md` | scenariji A–G (izvedivost, formule, pragovi), AI procjena (prompt, shema, validator, evaluacija) |
| `docs/DATA_MODEL.md` | PostgreSQL shema, provenancija, OIB matching, valute, sintetički podaci |
| `docs/ARCHITECTURE.md` | stack, ponovna upotreba koda iz `Desktop/datathon`, pipeline, REST API, pravni preduvjeti, interoperabilnost |
| `docs/FEATURES.md` | F1–F16 s prioritetima |
| `docs/UI_UX.md` | dizajn, ekrani (ASCII), pristupačnost |
| `docs/MVP_ROADMAP.md` | plan po danima 2.–9.11., rizici, prihvatni kriteriji |
| `docs/DEMO_SCENARIOS.md` | sintetički i stvarni demo, scenarij videa |
| `docs/EVALUATION.md` | izmjerene metrike AI procjene (sintetika 40, stvarni uzorak 60) |
| `work/eval/` | skup i rezultati AI evaluacije, CSV za ručne oznake + upute |

## Sigurnosna pravila organizacije
Vrijede pravila iz organizacijskog CLAUDE.md-a (npr. ništa se ne objavljuje na javne platforme, ne pristupa se cookiejima). Podatke natjecanja i kod ne dijelimo javno bez provjere.
