# Prompt: #44 AI procjena (backend, evaluacija, prikaz)

_Napisano 2026-11-03, nakon što su #40–#43 i `refactor-ui` spojeni u `main`. Kopirati u novu sesiju._

---

Nastavljamo JavniRadar: issue **#44** ("AI procjena: backend, evaluacija, prikaz", veličina XL, rok 06.11., feature freeze 07.11. 20:00). Stanje je u `CLAUDE.md`, `work/tasks.md`, `log.md` i `work/decisions.md`.

## Pravila rada (obvezno)
- **Nikad ne commitaj, ne pushaj i ne otvaraj PR.** Promjene ostavi necommitane i na kraju napiši što je spremno za commit.
- **Grana:** `git checkout main && git pull && git checkout -b 44-ai-procjena`. Ako `main` nije čist ili nedostaje PR #53, pitaj korisnika.
- Ništa ne izmišljaj. Razlike od `docs/` zapiši u `work/decisions.md`. Dokumentaciju ažuriraj u istom koraku.
- **Nikad ne objavljuj izmišljene metrike.** Brojke evaluacije upisuju se samo ako su stvarno izmjerene.
- Na kraju svake faze pokreni `./mvnw test` (u `apps/api`, treba Docker) te `yarn lint` i `yarn build` (u `apps/web`). Zatim ažuriraj `work/tasks.md` i `log.md`.
- Komunikacija je na hrvatskom.

## Ključ (bitno za opseg)
- `ANTHROPIC_API_KEY` **možda još nije postavljen** (#39). Provjeri na početku i reci korisniku.
- **Sve se mora moći napraviti i testirati bez ključa:**
  - `LlmClient` sučelje s dvije implementacije: `AnthropicLlmClient` (pravi) i `FixtureLlmClient` (vraća unaprijed pripremljene odgovore iz `src/test/resources` ili `resources/synthetic`);
  - bez ključa aplikacija normalno radi, AI run vraća jasnu poruku "AI nije konfiguriran", a UI prikazuje "AI procjena nije dostupna";
  - pravila ne ovise o AI-ju (`docs/MVP_ROADMAP.md`: "Rule-based dio radi bez `ANTHROPIC_API_KEY`").
- Stvarni pozivi (faza 4) rade se tek kad korisnik potvrdi da je ključ postavljen **s limitom potrošnje u konzoli**.

## Prije koda
1. Pročitaj:
   - `CLAUDE.md`, `AGENTS.md` (AI procjena, neutralni jezik, fizičke osobe);
   - `docs/ANOMALY_DETECTION.md` **§4 cijeli** (4.1 tok, 4.2 kontekst, 4.3 shema, 4.4 validator, 4.5 prompt `ai-flag-v1` s few-shot primjerima, 4.6 evaluacija);
   - `docs/DATA_MODEL.md` (tablice `ai_assessment`, `llm_usage`, `feedback`);
   - `docs/ARCHITECTURE.md` (pipeline korak 9, rukovanje greškama, `llm_usage`, tajne);
   - `docs/UI_UX.md` §3.4 (blok AI procjene na dnu detalja) i `DESIGN_SYSTEM.md`.
2. Učitaj skillove `spring-boot-api`, `claude-api` (prije bilo kakvog koda za Anthropic SDK: model ID-jevi, tool use, cijene) i `react-web` za frontend.
3. **Kod za ponovnu upotrebu** je u `Desktop/datathon/apps/api/src/main/java/com/datathon/api/ai/` (`LlmUsage`, `LlmUsageRepository`, `*Schema`, `*Service`, `BatchRunner`, `AdminAiController`) i u njegovom `pom.xml` (`com.anthropic:anthropic-java`). Kopiraj i prilagodi, **izvorni repo ne mijenjaj**.
   - **Cijene modela ne kopiraj slijepo** iz `application.yaml` tog repoa. Provjeri ih u skillu `claude-api` i zapiši izvor u komentar.
4. Postojeći kod u ovom repou koji se koristi:
   - `wording/ForbiddenTerms.java` i `resources/wording/forbidden_terms_hr.txt` (validator ih koristi, ne radi drugi popis);
   - paketi `indicator`, `contract`, `organization`, `municipality` (DZS, usporedna skupina), `rules`;
   - `resources/config/thresholds.yaml` (dodaj blok `ai`).
5. **Lokalno okruženje:**
   - baza je `javniradar-postgres` na portu 5434 (`docker start javniradar-postgres`);
   - API se pokreće s `ADMIN_TOKEN=local-dev ./mvnw spring-boot:run`;
   - nakon pokretanja: `POST /api/admin/rules/run` i `POST /api/admin/synthetic/load` (header `X-Admin-Token: local-dev`);
   - port 8080 je ranije bio zauzet tuđim procesom: provjeri `netstat -ano | grep LISTEN | grep 80`. Ne gasi tuđe procese bez pitanja i ne mijenjaj trajno port u repou (vidi `work/status-42.md`).
6. Daj kratak plan i čekaj potvrdu.

## Faza 1: shema, kontekst i validator (bez ključa)
- **Migracija `V4__ai.sql`:** `ai_assessment` i `llm_usage` točno prema `DATA_MODEL.md`. Ako tablica `feedback` već postoji (radi se u #45), ne diraj je. Inače je ne dodaj, jer pripada #45.
- **Odabir ugovora za AI** (`AiCandidateSelector`), prema §4.1:
  - ugovor ima barem jedan indikator **ili** vrijednost ≥ 20.000 € **ili** nejasan predmet (kraći od 4 riječi ili s popisa generičkih fraza; popis u `resources/wording/`);
  - **isključeni:** obrti, fizičke osobe i ugovori bez OIB-a dobavljača (GDPR, `AGENTS.md`);
  - gornja granica `ai.max_contracts_per_run` (zadano 300); poredak deterministički (prvo s indikatorima, pa po vrijednosti);
  - sintetički ugovori se mogu uključiti zastavicom (za evaluaciju), u produkciji ne.
- **Kontekst** (`AiContextBuilder`), oblik iz §4.2:
  - samo podaci iz baze, s `izvor_url` za svaki izvorni redak;
  - pravila iz postojećih indikatora (rule code, veličina kao tekst, `evidence_strength`);
  - `dobavljac`: Sudreg ključa nema, pa `nkd` i `datum_osnivanja` ostaju `null`, a `izvor: "nedostupno"`. **Ne izmišljaj NKD.** Ako postoji JPT podatak (dobavljač u vlasništvu naručitelja), dodaj ga;
  - `statistika_skupine` iz postojeće logike usporednih skupina (#43). Ako `sufficient=false`, polje je `null`;
  - funkcija koja iz konteksta vadi skup dopuštenih putanja polja (`ugovor.predmet`…) i skup URL-ova. Validator koristi oba;
  - kanonski JSON (sortirani ključevi) i `input_hash = sha256(prompt_version + model + kontekst)`.
- **Validator** (`AiAssessmentValidator`), točno §4.4:
  - svaka putanja u `polja` mora postojati u kontekstu, svaki URL u `izvori` mora biti iz konteksta;
  - zabranjene riječi preko `ForbiddenTerms`;
  - za obrt i fizičku osobu: odbij razlog koji sadrži naziv dobavljača;
  - prazni `moguca_legitimna_objasnjenja` odbija cijeli odgovor;
  - ako su svi razlozi odbijeni: `nema_oznake`, status `odbaceno_validacijom`;
  - odbačeni odgovori se spremaju s `rejection_reason`, ali se **nikad ne prikazuju**.
- **Testovi:** jedinični testovi validatora za svaki slučaj iz §4.4 (izmišljeno polje, tuđi URL, zabranjena riječ, naziv obrta, prazna objašnjenja, `visoka_pozornost` bez razloga) i test konteksta (hash stabilan, nema polja koja nisu u bazi).

## Faza 2: klijent, prompt, cache, budžet, run (bez ključa, uz fixture)
- **Prompt `ai-flag-v1`:** sistemski prompt i 3 few-shot primjera **doslovno iz §4.5**, u `resources/ai/ai-flag-v1.txt`. Verzija je konstanta. Bilo koja promjena teksta = nova verzija.
- **Tool `zabiljezi_procjenu`** s JSON shemom iz §4.3. Model odgovara isključivo pozivom alata (`tool_choice` forsiran).
- **`AnthropicLlmClient`:**
  - modeli iz konfiguracije: `ai.model` = `claude-haiku-5-5`, `ai.recheck-model` = `claude-sonnet-5-5`;
  - ključ samo iz env-a (`ANTHROPIC_API_KEY`), nikad u logu ni u repou;
  - timeout i jedan retry za 429/5xx; greška znači "nema procjene" za taj ugovor, run ide dalje.
- **Cache:** prije poziva provjeri postoji li `ai_assessment` s istim `(contract_id, model, prompt_version, input_hash)`. Ako postoji, ne zovi model i u `llm_usage` zapiši `cache_hit=true`.
- **Budžet:** `ai.max_usd_per_run` (zadano 2 USD) i `ai.max_calls_per_run`. Trošak se računa iz tokena i cijena u konfiguraciji. Kad se budžet potroši, run staje i vraća koliko je obrađeno.
- **Admin endpoint:** `POST /api/admin/ai/run?limit=&synthetic=&dryRun=` (header `X-Admin-Token`).
  - `dryRun=true` ne zove model, nego vraća broj kandidata, primjer konteksta i procjenu troška. **Ovo pokaži korisniku prije prvog pravog runa.**
  - Odgovor: obrađeno / iz cachea / prihvaćeno / odbačeno validacijom / greške / trošak.
- **Testovi:** run s `FixtureLlmClient` (prihvaćen, odbačen, cache hit, prekid zbog budžeta), te test da aplikacija starta i pravila rade bez ključa.

## Faza 3: API i prikaz u UI-ju
- **API:**
  - `GET /api/v1/contracts/{id}` dobiva `aiAssessment` (ili `null`): `label`, `confidence`, `reasons` (s poljima i izvorima), `missingData`, `legitExplanations`, `model`, `promptVersion`, `createdAt` i `badge: "AI procjena – zahtijeva ljudsku provjeru"`. Vraća se samo zadnja procjena sa statusom `prihvaceno`;
  - ako postoji Sonnet procjena (faza 5), ona ima prednost, a Haiku je `zamijenjeno`;
  - za obrte se uvijek vraća `null` i razlog "AI procjena se ne radi za obrte i fizičke osobe".
  - Ažuriraj tablicu endpointa u `docs/ARCHITECTURE.md` §5.
- **UI (`apps/web`, dizajn sustav iz `DESIGN_SYSTEM.md`):**
  - blok na dnu `ContractPage`, odvojen od tri okvira pravila, točno kao `UI_UX.md` §3.4;
  - natpis **"AI procjena – zahtijeva ljudsku provjeru"** je uvijek vidljiv, s nazivom modela i verzijom prompta;
  - oznaka (`nema_oznake` / `vrijedi_provjeriti` / `visoka_pozornost`) s riječima, ne samo bojom; jantarna (`--signal`), nikad crvena;
  - svaki razlog s poljima i poveznicama na izvor; "Moguća legitimna objašnjenja" su uvijek vidljiva; "Što nedostaje";
  - stanja: nema procjene ("AI procjena nije dostupna"), obrt, sintetički ugovor (ljubičasta traka već postoji);
  - **gumbi "Korisno / Pogrešno":** samo ako `/feedback` iz #45 već postoji u `main`. Inače ih izostavi i zapiši u `work/tasks.md` kao preostalo za #45.
  - Na `IndicatorPage` (F/C) dovoljno je kratko "AI procjena postoji za N od M ugovora skupine" s poveznicama, ako je jednostavno. Inače izvan opsega.
- Tipovi u `apps/web/src/types/`, prijevodi oznaka u `utils/labels.ts`.

## Faza 4: evaluacija (traži ključ i potvrdu korisnika)
- **Prije prvog pravog poziva:** pokaži `dryRun` (broj kandidata, procijenjeni trošak) i čekaj potvrdu.
- **Sintetički skup (40):** generiraj iz predložaka A–G + nesklad djelatnosti, kao sintetičke ugovore (`synthetic=true`, izmišljene organizacije, oznaka u svakom zapisu). Za svaki je očekivana oznaka zapisana unaprijed.
- **Stvarni skup (60):** nasumičan, ponovljiv (fiksni seed) odabir iz klase A. Izvezi CSV predložak u `work/eval/ai-flag-v1-real.csv` (id, naručitelj, predmet, iznos, poveznica, prazni stupci `oznaka_osoba1`, `oznaka_osoba2`). **Ručne oznake upisuju članovi tima, ti ih ne popunjavaš.**
- **Skripta / test za metrike** (preciznost i odziv za `oznaka ≠ nema_oznake`, stopa lažno pozitivnih na neutralnim, stopa odbacivanja u validatoru, Cohenova kappa prema pravilima). Računa se samo na onome što je stvarno označeno.
- Rezultate upiši u `docs/EVALUATION.md` (novi file) s datumom, modelom, verzijom prompta, n i troškom. Ako ručnih oznaka još nema, napiši točno to.
- Pregledaj s korisnikom 5–10 stvarnih procjena (dobra i loša) prije nego što ih UI prikaže u demu.

## Faza 5 (Should): Sonnet ponovna provjera
- Sve `visoka_pozornost` iz Haiku prolaza idu na `claude-sonnet-5-5` s istim promptom i validatorom. Haiku zapis postaje `zamijenjeno`.
- Radi se samo ako faze 1–4 stoje i ima vremena prije 06.11.

## Provjera prije završetka
- `./mvnw test`, `yarn lint` i `yarn build` prolaze, **i bez `ANTHROPIC_API_KEY`**.
- `git grep -i "sk-ant"` ne vraća ništa; `.env` je u `.gitignore`.
- U pregledniku: detalj ugovora s prihvaćenom procjenom, bez procjene, s obrtom i sintetički ugovor; 1280 px i mobitel.
- Ažuriraj `docs/ANOMALY_DETECTION.md` §4 (ako se išta razlikuje), `docs/DATA_MODEL.md` (makni `ai_assessment` i `llm_usage` s popisa "Još nije"), `docs/ARCHITECTURE.md` §5, `work/tasks.md`, `log.md`.

## Izvan opsega
- obrazac za ispravak, `/feedback` i stranica Metodologija (#45; metrike evaluacije samo predaj #45 kroz `docs/EVALUATION.md`);
- Sudreg i pravilo B, LLM ekstrakcija klase B/C (#46);
- pretraga prirodnim jezikom (F-feature iz `FEATURES.md`);
- deploy (#47).
