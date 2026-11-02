# Prompt: nakon POC-a — stari EOJN format (A2), JPT, pravila F i C, profil JLS

_Napisano 2026-11-02, nakon POC-a (grana `poc/vertical-slice`, commit 6b87381). Kopirati u novu sesiju ili nastaviti u postojećoj._

---

Nastavljamo JavniRadar nakon POC-a. Stanje je u `work/tasks.md`, `log.md` i `work/decisions.md`. Stack: Java/Spring Boot + React. Radi na grani `poc/vertical-slice`, a nakon svake faze napravi lokalni commit (ne pushaj).

## Prije koda
1. Pročitaj `CLAUDE.md`, `AGENTS.md`, `docs/ANOMALY_DETECTION.md` (§0, §2, scenariji C i F, §5), `docs/DATA_SOURCES.md` (stari EOJN izvještaj, JPT), `docs/DATA_MODEL.md` i `docs/UI_UX.md` (profil JLS, detalj indikatora).
2. Učitaj skillove `spring-boot-api` i `react-web`.
3. Daj kratak plan i čekaj potvrdu.

## Odluka iz POC-a (zapiši u `work/decisions.md`)
- Obrti ulaze u **brojke** sažetaka (broj, iznos). Isključeni su samo iz lista i prikaza **s nazivima** ("top dobavljači" i sl.). Naziv obrta i dalje se nigdje ne prikazuje. Ovime se zatvara otvoreno pitanje i uklanja razilaženje sažetka i filtra. Prilagodi testove.

## Faza 1: parser starog EOJN izvještaja (A2)
- Parser za 18-stupčani format (naziv i OIB u istoj ćeliji, bez EOJN URL-a i bez iznosa izmjene). Koristi isti tok: `raw_record`, provenancija, normalizacija i GDPR klasifikacija.
- **Deduplikacija između formata:** A2 nema EOJN URL. Ako naručitelj za istu godinu ima podatke u klasi A, A2 zapise za tu godinu ne uvozi i to broji u izvješću. Unutar A2 deduplicira po (naručitelj, dobavljač OIB, datum, iznos, predmet). Pravilo zapiši u `DATA_MODEL.md`.
- Pravilo D na A2 radi samo za preplate (D2). Za D1 nema podataka, pa izostaje bez greške.
- Ponovi provjeru točnosti kao u POC-u: 10 najvećih i 10 nasumičnih zapisa usporedi s izvornim ćelijama, te provjeri distribuciju iznosa (osobito HRK→EUR za godine prije 2023.).
- **STANI i daj izvješće:** broj novih naručitelja i ugovora po godini, ukupno JLS, odbačeni redci po razlogu, preskočeni zbog preklapanja s A, rezultat provjere točnosti.

## Faza 2: JPT (registar pravnih osoba javnog sektora)
- Ingest JPT-a prema `DATA_SOURCES.md`. Prvo napravi stvarni zahtjev i rezultat zapiši u docs.
- Spoji naručitelje i dobavljače s JPT-om po OIB-u. Cilj: znati je li dobavljač **pravna osoba u vlasništvu naručitelja** (komunalno poduzeće i sl.). To je kontekst "informativno" za C i F.
- Ako JPT ne daje podatke o osnivačima, nemoj ih izmišljati. Zapiši ograničenje, a pravila neka rade bez tog konteksta.

## Faza 3: pravila F (cjepkanje) i C (koncentracija)
- Implementiraj točno prema `ANOMALY_DETECTION.md` (formule, pragovi, pouzdanost, lažno pozitivni, "Što NE znači"). Pragovi idu u `thresholds.yaml` s izvorom i `valid_from`. Za F posebno: prozor koji prelazi 16.05.2026. koristi niži prag.
- Tri odvojene dimenzije, `rule_version`, tekstovi bez zabranjenih riječi.
- **Proširi sintetički ground truth** (isti XLSX tok, fiktivne općine, `000000000xx`, očekivani ishodi u fixture datoteci):
  - F: jasno pozitivni (npr. 3 ugovora istom dobavljaču u istoj CPV3 unutar 90 dana, svaki ≥ 70 % praga), jasno negativni (različiti dobavljači, razmak > 90 dana), rubni (točno 90 dana, prozor preko 16.05.2026., CPV podudaranje samo na 2 znamenke, CPV 09/65);
  - C: jasno pozitivni (top1 ≥ 70 % i HHI ≥ 5.000, n ≥ 5, ≥ 2 godine), jasno negativni, rubni (n = 4, jedna godina, ukupno malo ispod 50.000 €, dobavljač u vlasništvu naručitelja).
  - Postojeći test matrice proširi na F i C: 0 FP i 0 FN na jasnim slučajevima.
- **STANI i daj izvješće:** matrica po pravilu, broj stvarnih indikatora F i C po pojasu i naručitelju, te po 10 primjera za ručnu provjeru lažno pozitivnih. Naglasi slučajeve s prirodnim monopolom i komunalnim poduzećem.

## Faza 4: API i UI
- API: `GET /api/v1/indicators` (filtri: pravilo, naručitelj, pojas, uključi sintetiku), `GET /api/v1/buyers/{oib}` (profil: broj i iznos ugovora po godini i CPV kategoriji, indikatori, izvori i pokrivenost godina).
- UI prema `UI_UX.md`:
  - profil JLS (`/jls/:oib`) s grafom kategorija i tablicom uz graf zbog pristupačnosti, popisom indikatora i pokrivenošću podataka ("podaci za 2019.–2026., 2026. nepotpuna");
  - detalj indikatora F (vremenska os s točkama ugovora, crtom praga i zbrojem prozora) i C (udio dobavljača po godinama);
  - istraživač dobiva filter **"Vrsta indikatora"**: višestruki odabir po podvrsti pravila (D1 povećanje vrijednosti, D2 isplata iznad ugovorenog, F cjepkanje, C koncentracija), s brojem uz svaku opciju. Kombinira se s postojećim filterom statusa (vrijedi provjeriti / moguća greška u unosu / bez oznake) i s pojasom veličine odstupanja. Filteri se spremaju u URL (npr. `?vrsta=D1,F`).
  - API: `/contracts` i `/indicators` primaju `ruleCode` (lista) i `band`. Sažetak vraća broj indikatora po vrsti.
  - Naziv u UI-ju je "Vrsta indikatora", ne "vrsta sumnjivosti" (neutralni jezik, `AGENTS.md`).
- Koristi postojeće komponente i tokene iz POC-a.

## Izvan opsega
AI procjena, karta, Sudreg/pravilo B, DZS usporedne skupine, deploy. To ide u sljedeći prompt.

## Pravila rada
Ista kao u `work/prompts/poc-implementation.md`:
- ništa ne izmišljaj; razlike od docs zapiši u `decisions.md`; dokumentaciju ažuriraj u istom koraku;
- na kraju svake faze pokreni `./mvnw test`, `yarn build` i `yarn lint`, ažuriraj `tasks.md` i `log.md` i napravi lokalni commit;
- bez pusha.
