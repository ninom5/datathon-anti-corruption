# Prompt: #41 pravila F (cjepkanje) i C (koncentracija) + API indikatora

_Napisano 2026-11-03, nakon što je #40 (A2, JPT, DZS) spojen u `main` (PR #49). Kopirati u novu sesiju._

---

Nastavljamo JavniRadar: issue **#41** ("Pravila F i C + API indikatora", rok 04.11.). Stanje je u `CLAUDE.md`, `work/tasks.md`, `log.md` (zapis 2026-11-03) i `work/decisions.md` (odjeljak "2026-11-03: #40").

## Pravila rada (obvezno)
- **Nikad ne commitaj, ne pushaj i ne otvaraj PR.** Promjene ostavi necommitane na grani i na kraju napiši što je spremno za commit. To je izričito pravilo korisnika.
- Grana: `git checkout main && git pull` (lokalni `main` zaostaje za `origin/main`, a #40 je u `a2f4d50`), zatim `git checkout -b 41-pravila-f-c`.
- Ništa ne izmišljaj; razlike od `docs/` zapiši u `work/decisions.md`. Dokumentaciju ažuriraj u istom koraku.
- Na kraju svake faze pokreni `./mvnw test` (u `apps/api`, treba Docker), `yarn lint` i `yarn build` (u `apps/web`), pa ažuriraj `work/tasks.md` i `log.md`.
- Komunikacija je na hrvatskom.

## Prije koda
1. Pročitaj `CLAUDE.md`, `AGENTS.md`, `docs/ANOMALY_DETECTION.md` (§0, §2, scenariji C i F, §5), `docs/DATA_MODEL.md` (§3, §8a) i `docs/DATA_SOURCES.md` (§1.1b A2, §5 JPT, §6 DZS, §10 pragovi).
2. Učitaj skill `spring-boot-api` (i `react-web` ako diraš `apps/web`).
3. Lokalna baza je `javniradar-postgres` na portu 5434 (`docker start javniradar-postgres`). API se pokreće s `ADMIN_TOKEN=local-dev ./mvnw spring-boot:run`, a ingest s `POST /api/admin/ingest/run` (header `X-Admin-Token`).
4. Daj kratak plan i čekaj potvrdu.

## Što je već gotovo (#40), koristi to
- 5.958 ugovora (2.332 klase A za 2024.–2026., 3.626 klase A2 za 2018.–2025.), 38 naručitelja, 34 JLS.
- **A2 ugovori:**
  - `eojn_contract_url`, `total_value_eur` i `amendments_value_eur` su NULL; iznos je u `initial_value_eur`;
  - `subject_type` je NULL (A2 nema "Vrsta predmeta"), pa se za F vrsta predmeta mora izvesti iz CPV-a (45 = radovi);
  - za zbrojeve koristi `coalesce(total, initial)`, kao `ContractTotals`.
- **JPT:** `JptDirectory.isSupplierFoundedByBuyer(supplierOib, buyerOib)` daje kontekst "informativno" za C i F. Na stvarnim podacima ima 15 takvih parova (npr. Varaždinske Toplice → Forum Toplice d.o.o., 16 ugovora).
- **DZS:** tablica `municipality` (OIB, `population_2021`, `peer_group` P1–P7) za svih 556 JLS.
- Oznake kvalitete koje snižavaju pouzdanost: `gross_below_net`, `supplier_consortium`, `amount_out_of_range`, `hrk_after_euro`.

## Kritično prije pravila F: pragovi prije 2023.
- `thresholds.yaml` → `simple-procurement` ima pragove samo od 2023-01-01. HRK pragovi (200.000 / 500.000 kn) su u `DATA_SOURCES.md` §10 označeni kao **[PRETPOSTAVKA]**, pa su namjerno izostavljeni.
- Posljedica: F zasad ne može ocijeniti 2018.–2022., a to je većina A2 podataka.
- Prvo **provjeri pragove u izvoru** (ZJN 2016, NN 120/16, čl. 12) i zapiši izvor u `DATA_SOURCES.md` §10. Tek onda ih dodaj u `thresholds.yaml` s `valid-from`/`valid-to`.
- Ako ih ne možeš provjeriti, F radi samo od 2023. Ograničenje zapiši i pokaži u UI-ju ("cjepkanje se procjenjuje za ugovore od 2023.").

## Faza 1: pravila F i C
- Implementiraj točno prema `ANOMALY_DETECTION.md`: formule, pragovi, pouzdanost, lažno pozitivni i "Što NE znači".
- Pragovi idu u `thresholds.yaml` s izvorom i `valid-from`. Za F, prozor koji prelazi 16.05.2026. koristi niži prag.
- Tri odvojene dimenzije, `rule_version`, tekstovi bez zabranjenih riječi (`ForbiddenWordingTest`).
- C i F su pravila nad skupinom ugovora, ne nad jednim ugovorom. `indicator.subject_type` je `buyer_cpv` ili `group`, `subject_ref` je JSON, a `evidence` navodi sve ugovore u skupini. Postojeći `ContractRule` je za jedan ugovor, pa predloži sučelje za skupna pravila.
- **Dobavljač u vlasništvu naručitelja (JPT)** daje `informativno` s objašnjenjem, ne `vrijedi_provjeriti`.
- **Proširi sintetički ground truth** (isti XLSX tok, fiktivne općine, OIB-i `000000000xx`, očekivani ishodi u `fixtures/synthetic/expected.csv`):
  - F: jasno pozitivni (3 ugovora istom dobavljaču, ista CPV3, unutar 90 dana, svaki ≥ 70 % praga), jasno negativni (različiti dobavljači, razmak > 90 dana), rubni (točno 90 dana, prozor preko 16.05.2026., CPV podudaranje samo na 2 znamenke, CPV 09/65);
  - C: jasno pozitivni (top1 ≥ 70 % i HHI ≥ 5.000, n ≥ 5, ≥ 2 godine), jasno negativni, rubni (n = 4, jedna godina, ukupno malo ispod 50.000 €, dobavljač u vlasništvu naručitelja).
  - `SyntheticGroundTruthTest` proširi na F i C: 0 FP i 0 FN na jasnim slučajevima.
- **STANI i daj izvješće:**
  - matrica TP/FP/TN/FN po pravilu;
  - broj stvarnih F i C indikatora po pojasu i naručitelju;
  - po 10 primjera za ručnu provjeru lažno pozitivnih, s istaknutim komunalnim poduzećima i prirodnim monopolima (voda, struja, pošta).

## Otvoreno pitanje iz #40 (riješi uz ručni pregled)
D2 slučajevi s omjerom isplate i ugovorenog iznosa 9–10× (npr. Grad Ivanec: 11.010,44 € ugovoreno, 110.101,44 € isplaćeno) izgledaju kao pomaknut decimalni zarez. Sada su ispod `implausible-ratio: 10`, pa ostaju "vrijedi provjeriti". Pokaži korisniku 5–10 takvih primjera s izvornim ćelijama i predloži odluku. Prag ne mijenjaj bez potvrde.

## Faza 2: API
- `GET /api/v1/indicators`: filtri `ruleCode` (lista: `D_VALUE_INCREASE`, `D_OVERPAYMENT`, `F_SPLITTING`, `C_CONCENTRATION`), `buyerOib`, `band`, `synthetic`; paginacija prema skillu.
- `GET /api/v1/buyers/{oib}` (profil):
  - broj i iznos ugovora po godini i CPV kategoriji;
  - indikatori;
  - izvori i pokrivenost godina ("2018.–2026., 2026. nepotpuna");
  - broj stanovnika i usporedna skupina iz `municipality`;
  - JPT podaci (vrsta, županija).
  - Obrti ulaze u brojke, ali ne i u liste s nazivima.
- `/contracts` prima `ruleCode` (lista) i `band`. Sažetak vraća broj indikatora po vrsti (D1, D2, F, C).
- Odluka iz POC-a koju treba provesti i zapisati: obrti ulaze u **brojke** sažetaka, a isključeni su samo iz lista s nazivima. Prilagodi `excludedCraftContracts` i testove.
- Za svaki novi endpoint `@WebMvcTest` (happy path + 404/400) i točan JSON oblik za `apps/web/src/types/`.

## Izvan opsega
UI za F/C i profil JLS (#42), karta (#43), AI procjena (#44), Hvar i ostali izvori (#46), deploy (#47).
