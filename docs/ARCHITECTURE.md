# JavniRadar – arhitektura (MVP u 8 dana)

## 1. Odluka o stacku [PRIJEDLOG, čeka potvrdu tima]

Brief je predlagao Python + FastAPI. **Preporučujemo Javu/Spring Boot za ingest, pravila i API**, uz isti React frontend. Obrazloženje:

| Činjenica (provjereno u repou) | Posljedica |
|---|---|
| Ovaj repo već ima `apps/api` (Spring Boot 4, Java 25, JPA, PostgreSQL 17 u Dockeru) i `apps/web` (React 19, TS, Vite, wouter, TanStack Query) | Stack je već postavljen i tim ga je odabrao |
| Kod za ponovnu upotrebu (`PoliteHttpClient`, `RobotsRules`, `DiskCache`, `HostThrottle`, `RestSudregClient`, `FixtureSudregClient`, `PdfTextExtractor`, `AmountParser`, `CroatianDates`, AI paket s `LlmUsage`) postoji **samo u Javi**, u repou `Desktop/datathon` | Prepisivanje u Python stoji 1–2 dana, a nemamo ih |
| Tim je u tom repou isporučio 288 prolaznih testova u Javi | Java je dokazana brzina tima |
| Jedan jezik za ingest, pravila i API | Nema ljepila između jezika i nema dvostrukih modela |

**Python se koristi samo za istraživanje i evaluaciju** (`tools/notebooks`, skripte s `pandas`/`openpyxl`). Python **nije** u produkcijskom toku.
**Baza: PostgreSQL** (već je u `docker-compose.yml`). DuckDB ne treba: podataka je malo (< 100 k redaka), a Postgres daje `pg_trgm` za pretragu po nazivu i JSONB za dokaze.
**Rizik:** ako tim zaključi da je Python brži, data model i API ugovor iz ovih dokumenata vrijede bez promjene. Mijenja se samo §2.

## 2. Ponovna upotreba koda iz `Desktop/datathon` (drugi projekt, agregator natječaja)

U ovom repou **nema mape `poc/`**. Kod koji se može ponovno iskoristiti je u susjednom repou `C:\Users\Nino\Desktop\datathon` (branch `sime/ingest-other-sources`, AI na `origin/feature/pkg-5-ai`). Taj repo ostaje netaknut, a datoteke **kopiramo** uz bilješku o porijeklu u zaglavlju klase.

| Komponenta (paket u izvoru) | Što radi | Kako je koristimo |
|---|---|---|
| `ingest/http/PoliteHttpClient`, `RobotsRules`, `HostThrottle`, `DiskCache`, `TextDecoder` | robots.txt, razmak između zahtjeva po hostu, retry na 429/5xx, disk cache, dekodiranje cp1250/UTF-8 | sav download (CKAN, resursi, JPT, DZS). **User-Agent mijenjamo** u `JavniRadar/0.1 (studentski projekt; kontakt)` |
| `integration/sudreg/*` (`SudregClient`, `RestSudregClient`, `FixtureSudregClient`, `SudregCompany`, `NkdAreaMapper`) | OAuth2 client credentials; fixture ako ključ nije postavljen; poruke grešaka bez OIB-a | pravilo B, AI kontekst; fixture za sintetički demo |
| `ingest/extract/PdfTextExtractor`, `AmountParser`, `CroatianDates` | tekst iz PDF-a, parsiranje "1.500,00 kn", hrvatski datumi | parser klasa B/C/D, priprema za LLM |
| `ai/*` (`LlmUsage`, `LlmUsageRepository`, `*Schema`, `BatchRunner`, `*Merger`) | strukturirani izlaz, evidencija troška, batch | AI procjena i ekstrakcija |
| `.claude/skills/spring-boot-api`, `react-web` | konvencije koda | kopirati u `.claude/skills/` |

**Ne koristimo:** izvore natječaja (HZZ, APPRRR, MZOM, NN), modele domene natječaja.

## 3. Dijagram

```mermaid
flowchart LR
  subgraph Izvori
    CKAN[data.gov.hr CKAN API]
    RES[Resursi: XLSX/CSV/JSON registri ugovora]
    JPT[JPT REST API]
    DZS[DZS Popis 2021 XLSX]
    OCDS[OCP registar: EOJN OCDS]
    SUD[Sudski registar API OAuth2]
  end
  subgraph Ingest[apps/api – ingest modul]
    DISC[Discovery] --> DL[PoliteHttpClient + cache]
    DL --> PARSE[Parseri: EOJN-format / CSV / LLM ekstrakcija]
    PARSE --> VAL[Validacija + normalizacija]
    VAL --> MATCH[Entity matching OIB / naziv]
  end
  subgraph Analiza
    RULES[Pravila C, D, F (+B, G)]
    AI[AI procjena Haiku → Sonnet + validator]
  end
  DB[(PostgreSQL)]
  API[REST /api/v1]
  WEB[React web]
  CKAN --> DISC; RES --> DL; JPT --> DL; DZS --> DL; OCDS --> DL; SUD --> MATCH
  MATCH --> DB --> RULES --> DB
  DB --> AI --> DB
  DB --> API --> WEB
```

## 4. Ingest pipeline

| Korak | Ulaz | Izlaz | Napomene |
|---|---|---|---|
| 1. Discovery | CKAN `package_search?q=registar ugovora&rows=1000`, `organization` | `source_dataset`, `source_resource` | spremamo `metadata_modified`, licencu i format |
| 2. Download | URL resursa | `source_file` (sha256, veličina, `retrieved_at`, HTTP status) | `PoliteHttpClient`; 1 zahtjev/s po hostu; preskok ako se sha256 nije promijenio |
| 3. Klasifikacija | prvih 20 redaka | `format_class` A/B/C/D | A ako zaglavlje sadrži `OIB ugovaratelja` + `CPV` ili URL `eojn.hr/contract-eo` |
| 4. Parsiranje | A: Apache POI, traži red zaglavlja (prvih 15 redaka); CSV: detekcija kodiranja i separatora; B/C/D: LLM ekstrakcija (Should) | `raw_contract_row` (JSONB izvornog retka + `row_number`) | redak se nikad ne gubi; neparsirano ide u `ingest_issue` |
| 5. Normalizacija | raw | `contract` | iznos: `"1.500,00"` → 1500.00; HRK → EUR /7,53450 za datume < 2023-01-01; datum: Excel serial / `dd.MM.yyyy.`; CPV: prvih 8 znamenki; OIB: 11 znamenki + kontrolna znamenka (ISO 7064 MOD 11,10) |
| 6. Validacija | contract | `ingest_issue` | vidi §6 |
| 7. Matching | OIB / naziv | `organization` | vidi `DATA_MODEL.md` §4 |
| 8. Pravila | contract, reference | `indicator` | deterministički, verzionirano |
| 9. AI | contract + indikatori | `ai_assessment` | samo ako je `ANTHROPIC_API_KEY` postavljen i budžet nije potrošen |

Pokretanje: `POST /api/admin/ingest/run` (zaštićeno tokenom) i cron. Za MVP je dovoljan **ručni run + GitHub Actions `workflow_dispatch`** ili Spring `@Scheduled` noću. Svaki run je zapis u `ingest_run`.

## 5. REST API (`/api/v1`, JSON, samo čitanje osim povratnih informacija)

| Metoda | Putanja | Opis |
|---|---|---|
| GET | `/search?q=&type=` | brza pretraga naručitelja, dobavljača i ugovora (`pg_trgm`) |
| GET | `/contracts?buyer=&supplier=&cpv=&procedure=&min=&max=&from=&to=&has_indicator=&page=` | istraživač ugovora |
| GET | `/contracts/{id}` | ugovor + indikatori + AI procjena + dokazi |
| GET | `/buyers/{oib}` | profil naručitelja (JLS): potrošnja po CPV, po stanovniku, indikatori |
| GET | `/buyers/{oib}/peers?cpv=` | usporedba sa skupinom |
| GET | `/suppliers/{oib}` | profil dobavljača (ugovori, naručitelji, Sudreg snapshot) |
| GET | `/indicators?rule=&county=&buyer=` | popis indikatora |
| GET | `/indicators/{id}` | detalj indikatora (3 dimenzije, dokazi, "što ovo NE znači") |
| GET | `/map/counties/{code}` | JLS u županiji, broj indikatora, pokrivenost podacima |
| GET | `/sources` | izvori, licence, zadnji uspješni dohvat, broj zapisa |
| GET | `/methodology` | verzije pravila, pragovi, evaluacija AI-ja |
| POST | `/feedback` | ispravak ili pravo na odgovor (`target_type`, `target_id`, `role`, `text`, `email`) |
| POST | `/ask` | (Should) pretraga prirodnim jezikom → strukturirani filter |

**Primjer** `GET /api/v1/contracts/123`:
```json
{
  "id": 123,
  "buyer": {"oib": "00000000002", "name": "Općina Primjerovo", "population": 3120, "peerGroup": "P2"},
  "supplier": {"oib": "00000000001", "name": "Primjer d.o.o.", "type": "LLC"},
  "subject": "Rekonstrukcija nerazvrstane ceste",
  "cpv": {"code": "45233140", "label": "Radovi na cestama"},
  "procedureType": "Otvoreni postupak",
  "dateSigned": "2025-03-10",
  "values": {"initialValue": 200000.00, "amendmentsValue": 150000.00, "totalValue": 350000.00, "paidAmount": null, "currency": "EUR", "vatIncluded": false},
  "indicators": [{"id": 1, "ruleCode": "D_VALUE_INCREASE", "magnitude": {"value": 0.75, "unit": "udio", "label": "+75 %"},
                  "dataQuality": "visoka", "evidenceStrength": "srednja"}],
  "aiAssessment": {"label": "WORTH_CHECKING", "badge": "AI procjena – zahtijeva ljudsku provjeru", "model": "claude-haiku-5-5"},
  "provenance": {"sourceUrl": "https://data.gov.hr/…/registar-ugovora.xlsx", "locator": "sheet=1;row=41", "retrievedAt": "2026-11-03T09:12:00Z",
                 "method": "PARSER", "confidence": 1.0, "eojnUrl": "https://eojn.hr/contract-eo/000000"},
  "synthetic": true
}
```
JSON polja su camelCase na engleskom, prema rječniku u skillu `spring-boot-api`. Liste vraćaju `{content, totalElements, page, size}` (zadano `size = 20`, najviše 100).

**Greške:** jedinstveni oblik koji frontend već očekuje (`apps/web/src/types/ErrorResponse.type.ts`): `{statusCode, message, error}`, s porukom na hrvatskom. Odgovori ne sadrže stack traceove, OIB-ove ni URL-ove vanjskih servisa (obrazac iz `RestSudregClient`).

## 6. Validacija i greške
- **Redak se odbacuje** ako nema naručitelja, iznosa ili datuma. Zapis ide u `ingest_issue(severity=error)`.
- **Upozorenje** (redak ostaje, `data_quality` pada): neispravna kontrolna znamenka OIB-a, iznos ≤ 0 ili > 100 M €, datum u budućnosti, CPV bez 8 znamenki, iznos u kunama nakon 2023.
- **Vanjski izvor nedostupan:** run označava izvor kao `failed`, a stari podaci ostaju (`last_successful_fetch` u `/sources`). Demo radi iz baze, nikad uživo s vanjskog izvora.
- **LLM greška ili timeout:** zapis bez AI procjene; UI prikazuje "AI procjena nije dostupna". Pravila ne ovise o tome.

## 7. Opservabilnost
- Strukturirani logovi (JSON) s `run_id` i `source`, bez OIB-a fizičkih osoba.
- `ingest_run`: broj preuzetih, parsiranih, odbačenih i spojenih zapisa po izvoru.
- `llm_usage`: tokeni, trošak, model, cache hit.
- `/actuator/health` i `/api/v1/sources`, koji služi i kao javna stranica "Stanje podataka".

## 8. Sigurnost
- Nema korisničkih računa u MVP-u. Admin endpointi traže `X-Admin-Token` iz env varijable.
- Tajne (`ANTHROPIC_API_KEY`, `SUDREG_CLIENT_ID/SECRET`) samo u env varijablama. `.env` je u `.gitignore`.
- Rate limit na `/feedback` i `/ask` (npr. Bucket4j; 10/min po IP-u). Captcha se ne koristi.
- Escape svog teksta iz izvora u UI-ju (React to radi po zadanim postavkama; bez `dangerouslySetInnerHTML`).
- Prompt injection: tekst iz registara ide u prompt **samo kao podatak** unutar JSON polja. Validator odbacuje izlaz koji citira polja ili URL-ove izvan konteksta.
- CORS samo za domenu frontenda.

## 9. Pravni, organizacijski i tehnički preduvjeti

| Područje | Preduvjet | Kako ga ispunjavamo |
|---|---|---|
| **Ponovna uporaba** | Zakon o pravu na pristup informacijama (ZPPI) + Pravilnik o dozvolama za ponovnu uporabu (NN 67/17): otvorena dozvola traži **navođenje izvora** | izvor i poveznica uz svaki broj; stranica "Izvori i licence" |
| **GDPR** | Registri malih općina sadrže **fizičke osobe** (ugovori o djelu, najam, darovanje) i **obrte** (ime i prezime vlasnika u nazivu) | MVP **ne prikazuje** ugovore s fizičkim osobama (`supplier.type = fizicka_osoba` → filtrirano). Obrti se prikazuju kao poslovni subjekti, ali **bez** AI procjene i bez "profila osobe". Pravna osnova je javni interes / legitimni interes transparentnosti [PRETPOSTAVKA, treba pravnu provjeru]. Postoji zahtjev za uklanjanje preko `/feedback` |
| **Kleveta / odgovornost** | Netočna ili tendenciozna oznaka može naštetiti ugledu tvrtke ili općine | politika neutralnog jezika (popis zabranjenih riječi, test); svaki indikator ima "što ovo NE znači"; **pravo na odgovor** (§ `FEATURES.md` F12); vidljiva verzija pravila i datum izračuna |
| **Sudski registar** | Registracija za OAuth2 ključ; uvjeti korištenja [NEVERIFICIRANO] | zahtjev odmah (vidi `MVP_ROADMAP.md`); do tada `FixtureSudregClient` samo sa sintetičkim OIB-ima |
| **EOJN** | robots.txt zabranjuje struganje eojn.hr; uvjeti nisu pročitani | ne strugamo; OCDS iz OCP registra; budućnost: službeni API (vidi §10) |
| **Organizacijski** | Tko održava podatke, tko odgovara na prigovore | MVP: tim, rok odgovora 7 dana; budućnost: partner (udruga, fakultet) ili MPUDT |
| **Licenca koda** | OSS (boduje se) | prijedlog: **EUPL-1.2** (kompatibilna s javnim sektorom EU) ili MIT. Javna objava ovisi o sigurnosnoj politici organizacije (otvoreno, `work/decisions.md`) |
| **AI** | Pravila natjecanja traže navođenje AI alata | odjeljak "Korišteni alati" u README-u (Claude API, Claude Code) |

## 10. Interoperabilnost i integracija
- **Ulaz OCDS:** `contract` je mapiran na OCDS (`ocid`, `buyer`, `awards[].suppliers`, `contracts[].value`, `tender.procurementMethodDetails`). Adapter `OcdsReleaseMapper` čita JSONL (Should). Isti adapter radi za bilo koji OCDS izvor (npr. budući izravni EOJN feed).
- **Izravni EOJN:** od 16.05.2026. ZJN čl. 28 st. 5 traži da je **registar ugovora objavljen u EOJN-u**. Prijedlog MPUDT-u/MPGI-ju: javni REST/OCDS endpoint s OIB-om (`parties[].identifier.scheme = "HR-OIB"`) i brojem ponuda (`tender.numberOfTenderers`). JavniRadar tada može zamijeniti ingest općinskih XLSX-ova jednim izvorom.
- **Izlaz:** normalizirani skup `javniradar-ugovori` (CSV + JSON + OCDS JSONL) s metapodacima **DCAT-AP HR**, objavljen natrag na data.gov.hr preko CKAN-a (`package_create` traži API ključ izdavatelja, što je organizacijski preduvjet).
- **Identifikatori:** OIB (`HR-OIB`), JPT brojčana oznaka, CPV 2008, NKD 2025, NUTS/LAU za karte.
- **Integracije:** poveznica "Zatraži dokument" generira predložak ZPPI zahtjeva (moguća integracija s imamopravoznati.org); izvoz CSV-a za Državni ured za reviziju i unutarnje revizore.

## 11. Deploy
- **MVP:** jedan VM ili Render/Railway: `api` (jar) + `postgres` + statički `web` (Vite build iza istog Nginxa ili Spring static).
- Seed baze iz zamrznutog snapshota (`db/seed/*.sql.gz`), da demo ne ovisi o vanjskim izvorima.
- Prije predaje mora proći: `yarn build && yarn lint` (web), `./mvnw test` (api).
