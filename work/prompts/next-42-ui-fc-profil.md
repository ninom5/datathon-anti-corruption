# Prompt: #42 UI: profil općine, filter "Vrsta indikatora", detalj indikatora F i C

_Napisano 2026-11-03, nakon što je #41 (pravila F i C, API indikatora i profila) završen na grani `41-pravila-f-c`. Kopirati u novu sesiju._

---

Nastavljamo JavniRadar: issue **#42** ("UI: profil JLS, filter Vrsta indikatora, prikaz F i C", rok 05.11.). Stanje je u `CLAUDE.md`, `work/tasks.md`, `log.md` (zapisi 2026-11-03 navečer) i `work/decisions.md` (odjeljci "#41").

## Pravila rada (obvezno)
- **Nikad ne commitaj, ne pushaj i ne otvaraj PR.** Promjene ostavi necommitane i na kraju napiši što je spremno za commit.
- **Grana:**
  - ako je #41 spojen u `main`: `git checkout main && git pull && git checkout -b 42-ui-fc-profil`;
  - ako nije: provjeri s korisnikom (`git branch -a`, `git log --oneline -5`) radi li se dalje od `41-pravila-f-c`.
- Ništa ne izmišljaj. Razlike od `docs/` zapiši u `work/decisions.md`. Dokumentaciju ažuriraj u istom koraku.
- Na kraju svake faze pokreni `yarn lint` i `yarn build` (u `apps/web`) te `./mvnw test` (u `apps/api`, treba Docker). Zatim ažuriraj `work/tasks.md` i `log.md`.
- Komunikacija je na hrvatskom.

## Prije koda
1. Pročitaj `CLAUDE.md`, `AGENTS.md`, `docs/UI_UX.md` (§1 tokeni, §2 rute, §3.2–3.4, §4a, §5), `docs/ANOMALY_DETECTION.md` (§C i §F s odlomcima "Implementacija", §4a pojas) i `docs/ARCHITECTURE.md` §5 (tablica implementiranih endpointa).
2. Učitaj skillove `react-web` i `dataviz` (prije prvog grafa), a za backend dio `spring-boot-api`.
3. **Lokalno okruženje:**
   - baza je `javniradar-postgres` na portu 5434 (`docker start javniradar-postgres`);
   - API se pokreće s `ADMIN_TOKEN=local-dev ./mvnw spring-boot:run`;
   - nakon pokretanja: `POST /api/admin/rules/run` i `POST /api/admin/synthetic/load` (header `X-Admin-Token: local-dev`).
   - **Port 8080 je bio zauzet tuđim procesom.** Prvo provjeri `netstat -ano | grep LISTEN | grep 80`. Vite proxy (`vite.config.ts`) gleda na `:8080`. Ako je port zauzet, pitaj korisnika smije li se proces ugasiti, ili privremeno pokreni API na drugom portu i proxy usmjeri preko env varijable. Ne mijenjaj trajno port u repou bez dogovora.
4. Daj kratak plan i čekaj potvrdu.

## Što je gotovo u #41, koristi to (ne mijenjaj oblik bez razloga)
- `GET /api/v1/indicators?ruleCode=&buyerOib=&band=&severity=&synthetic=&page=&size=`:
  - svaki indikator ima `subjectType` (`contract` za D1/D2, `group` za F, `buyer_cpv` za C), `contractIds`, `buyer`, `magnitude.band` i tri odvojene dimenzije;
  - `possibleEntryError` je `true` samo za pravilo D.
- `GET /api/v1/buyers/{oib}`: `coverage.label`, `byYear` (s `incomplete`), `byCpv` (CPV2), `topSuppliers` (najviše 10, bez obrta), `craftContracts`, `indicatorCounts`, `indicators`, `sources`, `jpt`, `municipality` (stanovništvo 2021, `peerGroup`).
- `/contracts` i `/contracts/summary` primaju `ruleCode` (lista, šalje se zarezom; to radi `toQueryParams` u `apps/web/src/api/params.ts`) i `band`.
- Sažetak vraća `contractsByRule` (broj ugovora po pravilu uz filtar bez vlastitog izbora pravila) i `craftContracts`. **Obrti sada ulaze u brojke**, a isključeni su samo iz lista s nazivima.
- **Frontend je već pripremljen:**
  - tipovi: `BuyerProfile.type.ts`, `Indicator.type.ts` (`RuleCode`, `MagnitudeBand`, `IndicatorFilter`), `Contract.type.ts`;
  - API funkcije: `getIndicators`, `getBuyerProfile`;
  - `IndicatorTag` prikazuje F kao "2,49×" i C kao "HHI 8.580", a informativni F/C ima neutralnu oznaku (nije "greška u unosu").
- **Stvarni podaci:** F 150, C 17, D1 16, D2 88 (od toga 28 informativnih). Dobri demo primjeri:
  - F: Kloštar Ivanić, 3 dječja igrališta po oko 24.800 € uz prag 26.540 €, dokaz "jaka";
  - F: Sv. Filip i Jakov, "Kulturno ljeto", 5 ugovora, 4,34× prag;
  - C: Rovinj ili Bistra, HEP-Opskrba, uz napomenu o mogućem prirodnom monopolu (dobar primjer za "Što ovo NE znači");
  - informativni F: Varaždinske Toplice → Forum Toplice (vlastito komunalno društvo).

## Faza 0: mali backend dodatak (nužan za detalj indikatora)
- **`GET /api/v1/indicators/{id}`:** isti `IndicatorResponse` plus:
  - `contracts`: kratki popis ugovora skupine (`id`, `dateSigned`, `subject`, `supplier` kao `OrganizationRef`, `initialValue`, `totalValue`, `procedureType`). Ugovori s fizičkim osobama ne izlaze (GDPR);
  - `subject`: parsirani `subject_ref` (F: `cpv`, `cpvLevel`, `from`, `to`; C: `cpv2`, `years`, `topSupplierOib`).
- **Prag za F:** vremenska os treba crtu praga, a prag se sada nalazi samo u tekstu. Dodaj `threshold` (EUR) u `subject_ref` u `SplittingRule` i ponovno pokreni pravila. Prag ne računaj ponovno u frontendu.
- **Testovi:** `@WebMvcTest` (happy path, 404, 400) i provjera da sintetika i dalje prolazi (`SyntheticGroundTruthTest`).
- Točan JSON oblik dodaj u `apps/web/src/types/`.

## Faza 1: istraživač, filter "Vrsta indikatora"
- **Filter je višestruki odabir:**
  - opcije su D1 "Povećanje vrijednosti", D2 "Isplata iznad ugovorenog", F "Moguće cjepkanje", C "Koncentracija dobavljača";
  - uz svaku opciju je broj iz `contractsByRule`.
- Kombinira se s postojećim filterom oznaka (vrijedi provjeriti / moguća greška u unosu / bez oznake) i s filterom **pojasa** (nizak / srednji / visok, uz kratko objašnjenje da je to heuristika, a ne ocjena).
- **URL je na hrvatskom**, kao postojeći filtri: `?vrsta=D1,F&pojas=visok`. Mapiranje D1/D2/F/C na `RuleCode` drži na jednom mjestu (`utils/labels.ts`).
- Naziv u UI-ju je "Vrsta indikatora" (neutralni jezik, `AGENTS.md`).
- **Sažetak:**
  - ažuriraj `docs/UI_UX.md` §4a, gdje još piše "bez obrta";
  - u sažetku ili uz filter prikaži broj po vrsti.

## Faza 2: profil općine (`/opcina/:oib`, kao u `UI_UX.md` §2 i §3.3)
- **Zaglavlje:** naziv · županija · broj stanovnika (DZS 2021) · usporedna skupina, te `coverage.label` s ⓘ izvorom (`sources`: skup, klasa formata, broj datoteka).
- **Ugovori po godini:** graf, s nepotpunom godinom vidljivo označenom (ne samo bojom). Ispod je **"Prikaži kao tablicu"**.
- **Potrošnja po CPV skupini:** graf plus tablica. Ako je `municipality.population2021` poznat, prikaži i € po stanovniku. **Medijan usporedne skupine za sada ne prikazuj**, jer ga API ne vraća. Usporedba je #43.
- **Najveći dobavljači:** bez obrta, uz napomenu "N ugovora s obrtima uključeno je u brojke, ali bez naziva".
- **Indikatori:** brojke po vrsti i popis kartica s tri dimenzije i poveznicom na detalj indikatora.
- **Poveznice:** iz istraživača (naziv naručitelja) i s detalja ugovora na profil. Ruta ide u `routes.ts` kao konstanta.
- **Sva 4 stanja ekrana:**
  - 404 prikazuje "Za ovog naručitelja nema objavljenih ugovora u podacima";
  - ako `synthetic: true`, prikazuje se ljubičasta traka "SINTETIČKI PODACI ZA DEMONSTRACIJU".

## Faza 3: detalj indikatora (`/indikator/:id`, `UI_UX.md` §3.4, najvažniji ekran za povjerenje)
- **Zajedničko:**
  - tri jednako velika okvira (kvaliteta podataka, veličina odstupanja, pouzdanost dokaza), bez zbroja;
  - "Što ovo NE znači" je uvijek vidljivo;
  - "Što nedostaje" (`missingData`);
  - dokazi s poveznicom na redak izvora;
  - `rules-v1` i datum izračuna.
- **F:** vremenska os s točkom za svaki ugovor (iznos), vodoravnom crtom praga (`subject.threshold`) i trakom zbroja prozora. Ispod je tablica ugovora s poveznicama na detalj ugovora. Ako je dokaz "slaba" zbog podudaranja samo na CPV2, to treba reći riječima.
- **C:** udio dobavljača u vrijednosti ćelije (složeni stupac po godinama ili jednostavan vodoravni stupac po dobavljaču), uz tablicu ugovora.
  - Ako tekst sadrži napomenu o prirodnom monopolu ili vlasništvu naručitelja (JPT), istakni je kao kontekst, ne kao upozorenje.
  - Obrti se ne imenuju.
- **D1/D2:** postojeća kartica s detalja ugovora može se ponovno iskoristiti. Izvuci je u `components/` jer je sad koriste dvije stranice.
- `IndicatorCard` na detalju ugovora treba poveznicu "Otvori indikator", a za F i C poruku "Ovaj ugovor je dio skupine od N ugovora".
- **Popravi tekst** u `utils/labels.ts` → `evidenceStrengthHints` ako ne odgovara pravilima F i C. Izmijenjen je u #41, provjeri ga.

## Grafovi
- **Bez nove biblioteke**, osim ako korisnik izričito odobri. `UI_UX.md` §6 Observable Plot navodi samo kao prijedlog. Preporuka: ručni SVG ili CSS stupci (nekoliko jednostavnih grafova, bez ovisnosti, potpuna kontrola nad pristupačnošću). Ako ipak treba biblioteka, pitaj i zapiši odluku u `decisions.md`.
- Svaki graf ima `aria-label` sa sažetkom i alternativu "Prikaži kao tablicu". Boja nikad nije jedini nosilac značenja. Signal je jantarna boja (`--signal`), a ne crvena.
- Provjeri ekrane na 1280 px (video) i na mobilnoj širini.

## Provjera prije završetka
- `yarn lint`, `yarn build` i `./mvnw test` prolaze.
- U pregledniku (`yarn dev`) prođi:
  - istraživač s `?vrsta=F&pojas=visok`;
  - profil Grada Ivanca (`84121580205`) i jedne sintetičke općine (Općina Uzorkovo, `00000000003`);
  - detalj jednog F, jednog C i jednog informativnog indikatora;
  - prazno stanje i stanje greške (zaustavljen API).
- Ako preglednik nije dostupan, napravi headless screenshotove (Edge), kao u POC-u.
- Ažuriraj `docs/UI_UX.md` §4a (rute, stanje), `work/tasks.md` i `log.md`.

## Izvan opsega
- karta i usporedba općina (medijan skupine, #43);
- AI procjena (#44);
- profil dobavljača;
- Hvar i ostali izvori (#46);
- deploy (#47).
