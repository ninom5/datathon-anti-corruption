# Dnevnik

## 2026-10-22
- Dogovoren način rada: modularni md fileovi, asistent kritički savjetuje kao tech poduzetnik/softver inženjer. Postavljen kostur foldera.
- Primljen pozivni mail: Datathon Portala otvorenih podataka 2026 (MPUDT). Zapisano u `context/competition.md` i `context/timeline.md`. Uočeno: rok za prijavu je 23.10. u 24:00 (sutra).
- Folder je premješten u `Desktop/other/datathon` (klon repa github.com/SimeJadrijev/datathon). Prvi commit s md fileovima je napravljen i pushan na `origin/main` na izričit zahtjev korisnika. Vidljivost repa NIJE provjerena (`gh` nije instaliran).
- Pročitani Javni poziv i Pravila (PDF-ovi premješteni u `context/izvori/`). `competition.md`, `timeline.md`, `tasks.md` i `decisions.md` prepisani.
- **Ispravak:** prijavu ne podnosi samo mentor, nego svaki član tima ispunjava obrazac. Kriteriji: max 60 b, 9 kriterija, težište na izvedivosti, UI/UX-u i videu. Video do 3 min, obavezan, bez njega se rješenje ne ocjenjuje.
- Korisnik dodao skill `skills/product-brainstorming/SKILL.MD` (generički PM skill: modovi, HMW, JTBD, assumption testing). Nije u `.claude/skills/`, pa nije aktivan. Razgovor o tome koje još skillove dodati u fazi brainstorminga (prijedlog: custom skill za istraživanje data.gov.hr kataloga + custom "žiri" skill s rubrikom od 60 bodova).
- Skill premješten u `.claude/skills/product-brainstorming/SKILL.md` (aktivan). Napisana još 2 skilla: `jury-simulator` i `data-scout`. Provjereno curlom da data.gov.hr CKAN API radi na `/ckan/api/3/action/` (3956 skupova), upisano u `context/data.md`. Ključan nalaz: samo ~55 skupova se osvježava dnevno/tjedno/kontinuirano.
- Prijedlog (još ne odlučeno): ideja mora biti zaključana oko 27.10. da ostane ~10 dana za izradu. Brainstorm iz dva smjera (od podataka i od problema).
- Nepushani/necommitani: PDF-ovi u `context/izvori/`, `.claude/skills/` i izmjene md fileova nakon prvog commita.
- Skillovi `video-script` (3 min) i UI/UX nisu napisani, treba ih tek u fazi izrade.

## 2026-11-01
- Korisnik je zadao brief za **JavniRadar** (izazov A). Napisana je dokumentacija: `docs/` (9 fileova), `AGENTS.md`; ažurirani `CLAUDE.md` i `work/decisions.md`. Ništa nije commitano (korisnik prvo pregledava).
- Provedene stvarne HTTP provjere (CKAN, JPT, Sudreg 401 + OpenAPI, SRDI, DZS, OCP OCDS, TED; preuzeto i klasificirano 148 općinskih datoteka). Nalazi su u `docs/DATA_SOURCES.md`.
- U ovom repou nema `poc/`. Reusable kod je u Javi u `Desktop/datathon`.
- Pregled starih md fileova: ažurirani README, timeline, team, ideas, tasks (arhiva prijave), competition, jury-simulator skill (3 osobe, rok). Ništa nije obrisano.
- Kopirani skillovi `react-web` i `spring-boot-api` iz `Desktop/datathon` (izvor netaknut) i prilagođeni JavniRadaru (rječnik domene, /api/v1, domenska pravila, UI pravila za indikatore i AI). `ARCHITECTURE.md` usklađen: camelCase JSON, format greške {statusCode,message,error}.

## 2026-11-02 (POC, faza 1)
- Plan POC-a potvrđen. Tri preporuke prihvaćene (nečitljiv datum sklapanja odbacuje redak; sintetički OIB `000000000xx` je rezerviran; filter "prikaži sintetičke" umjesto `/demo`). Zapisano u `work/decisions.md`, `UI_UX.md` §4 ažuriran.
- Korisnik traži: stati nakon faze 1; pragovi samo iz `docs/` (NN 48/26) s `valid_from`; test zabranjenih riječi bez iznimki za negacije.
- Dodani Flyway, Apache POI 5.5.1 i Testcontainers. `V1__init.sql` je podskup `DATA_MODEL.md` (dodaci u §8).
- Kopirani `PoliteHttpClient` i ostale ingest/http klase, `AmountParser`, `CroatianDates` te dio `common/` iz `Desktop/datathon`, s bilješkom o porijeklu. User-Agent je `JavniRadar/0.1 (studentski projekt; kontakt)`.
- **Incident:** compose je imao isti naziv kontejnera kao drugi projekt, pa se pokrenula njihova baza. Ništa nije mijenjano. JavniRadar sada ima vlastiti kontejner `javniradar-postgres` na portu 5434.
- Stvarni ingest: 139 CKAN skupova, 153 preuzete datoteke (A 21, B 52, C 52, D 2, neuspjelo 20, nečitljivo 6). Klasa A daje **2.332 ugovora iz 17 JLS** + 1 županije + 1 vrtića; 590 duplikata; 0 odbačenih redaka. Nalaz: "stari EOJN izvještaj" je bio uračunat u "53 datoteke klase A".
- GDPR klasifikator pooštren nakon pregleda stvarnih naziva (osobna imena velikim slovima, OPG, uredi ovlaštenih inženjera, format naziva obrta).

## 2026-11-02 (POC, faza 2)
- Korisnik je potvrdio fazu 1. Odluke:
  - parser A2 je prvi zadatak nakon POC-a, prije F i C;
  - Hvar se preskače;
  - prije faze 2 provjeriti točnost parsera;
  - razlikovati tip naručitelja;
  - 2026. je nepotpuna godina.
- Provjera parsera: svih 20 uzoraka točno. Neobični iznosi su greške u izvornim registrima (Lepoglava, Mikleuš). Pravilo D zato ima zaštite kvalitete podataka (`ANOMALY_DETECTION.md` §D).
- Implementirani `thresholds.yaml`, pravila D1/D2, `RulesService` i admin endpointi `rules/run` i `synthetic/load`. Dodani `ForbiddenTerms` i test bez iznimki za negacije.
- Sintetički skup: 28 redaka u EOJN formatu (2 fiktivne općine) kroz isti XLSX parser. Rezultat: 0 FP, 0 FN.
  - jasni slučajevi: 7 TP, 7 TN;
  - kvaliteta: 4 TP, 1 odbačen;
  - rubni: 3 TP, 6 TN.
- Stvarni podaci: indikatori za 27 ugovora kod 11 naručitelja.
  - D1: 16 (11 do 30 %, 5 od 30 do 50 %);
  - D2: 12 (6 od 10 do 30 %, 1 od 30 do 50 %, 1 iznad 50 %, 4 informativna zbog greške u izvoru).
- `./mvnw test`: 22/22.

## 2026-11-02 (POC, faza 3)
- Odluke korisnika zapisane:
  - `sourceExplanation` za D2;
  - "Obrt (naziv skriven)";
  - kategorija mogućih grešaka u unosu;
  - lokalni commitovi na `poc/vertical-slice`.
- API (`/api/v1`): `contracts`, `contracts/summary`, `contracts/{id}`, `buyers`, `sources`, `sources/summary`. Filtri: naručitelj, dobavljač, godina, ima indikator, kategorija, sintetika (zadano isključena).
- Stvarni podaci preko API-ja:
  - 2.298 prikazivih ugovora; 34 ugovora s fizičkim osobama su skrivena;
  - sažetak bez obrta: 1.954 ugovora, 99,4 M EUR, 21 s indikatorom, 1 moguća greška u unosu (344 ugovora s obrtima izostavljeno).
- Testovi: 29/29 (novi su `ContractControllerTest`, `SourceControllerTest` i `ContractPrivacyTest`).

## 2026-11-02 (POC, faza 4)
- Frontend (`apps/web`):
  - Vite proxy `/api` → `:8080`;
  - tokeni iz UI_UX §1 i tamni način;
  - naslovnica, istraživač (filtri u URL-u, sažetak, tablica koja na mobitelu postaje kartice), detalj ugovora (tri dimenzije, objašnjenje naručitelja prije postotka, "Što ovo NE znači", dokazi, izvor s lokatorom) i izvori (pokrivenost i skupovi s datotekama);
  - ljubičasta traka za sintetiku; 4 stanja ekrana; 4xx se ne ponavlja.
- Provjera: `yarn lint` i `yarn build` zeleno; `./mvnw test` 29/29. Ekrani su provjereni headless screenshotovima (Edge), jer proširenje za Chrome nije instalirano.

## 2026-11-02 (večer): POC spojen u main, raspisani zadaci
- `poc/vertical-slice` spojen u `main` fast-forwardom (0ca7dfb). Uključuje i `nmilas-poc`. Ništa nije pushano.
- `work/tasks.md` prepisan u backlog T01–T29 + P1–P4 po ulogama DATA/BACK/FRONT, s rokovima, ovisnostima i promptovima. Arhiva prijave je skraćena.
- Remote repo `ninom5/datathon-anti-corruption` je PRIVATE (provjereno `gh`).

## 2026-11-03: #40 stari EOJN (A2), JPT, DZS (grana `40-a2-jpt-dzs`, necommitano)
- A2 parser: 32 datoteke, **3.626 novih ugovora (2014–2025)**, 926 duplikata, 10 odbačenih; preklapanje s klasom A 0. Ukupno 5.958 ugovora, **34 JLS (prije 17)**.
- Točnost A2: 20/20 (10 najvećih + 10 nasumičnih) s izvornim ćelijama.
- JPT: 4.678 tijela, 3.897 s osnivačima; 15 parova u kojima je naručitelj osnivač dobavljača. DZS: 556/556 JLS spojeno, usporedne skupine P1–P7.
- Nalaz: EOJN izvještaj označava kune kao "EUR" kod isplata za 2022.; D2 to sada prikazuje informativno (6 slučajeva).
- `./mvnw test` 41/41; `yarn lint` i `yarn build` zeleno. Korisnik: asistent nikad ne commita.

## 2026-11-03 (večer): #41 faza 1, pravila F i C (grana `41-pravila-f-c`, necommitano)
- Pragovi jednostavne nabave za 2017.–2022. provjereni u NN 120/16 (čl. 12 st. 1, čl. 452) i dodani u `thresholds.yaml`, pa F pokriva i A2.
- Novo: `GroupRule`, `SplittingRule` (F), `ConcentrationRule` (C), `MagnitudeBands` i migracija `V3` (`indicator.buyer_id`, `magnitude_band`, `indicator_contract`).
- Sintetika: 89 novih redaka (Općina Uzorkovo za F, Općina Probnik za C). Ground truth: F i C imaju 0 FP i 0 FN na jasnim i rubnim slučajevima.
- Stvarni podaci: F 164 (nizak 91, srednji 55, visok 18), C 22 (nizak 15, srednji 3, visok 4), D1 16, D2 88.
- Ručni pregled je našao dvije sustavne vrste lažno pozitivnih:
  - F: grupe (lotovi) otvorenog postupka (16 skupina);
  - C: udio koji nosi jedan veliki ugovor (oko 7 od 22).
  - Prijedlozi čekaju potvrdu korisnika.
- `./mvnw test` 41/41.

## 2026-11-03 (kasna večer): #41 popravci lažno pozitivnih i faza 2 (API)
- Korisnik je prepustio odluku asistentu ("radi što misliš da je najbolje"). Uvedeno, uz zapis u `work/decisions.md`:
  - F bez ugovora iz postupaka po ZJN-u (lotovi otvorenog postupka);
  - C traži ≥ 2 ugovora najvećeg dobavljača;
  - D2 bez obrazloženja ima granicu 5×.
- Stvarni podaci nakon popravaka: F 150 (nizak 88, srednji 49, visok 13), C 17 (11/2/4), D2 informativnih 28 (prije 23).
- API:
  - `GET /api/v1/indicators`, `GET /api/v1/buyers/{oib}`;
  - `/contracts` prima `ruleCode` i `band`;
  - sažetak ima `contractsByRule` i `craftContracts` (obrti sada ulaze u brojke).
  - "Moguća greška u unosu" vrijedi samo za pravilo D.
- Frontend: tipovi usklađeni sa stvarnim JSON-om (`BuyerProfile`, `Indicator`, `ContractSummary`), API funkcije `getIndicators` i `getBuyerProfile`. Oznaka u tablici prikazuje F kao "2,49×", a C kao "HHI 8.580". Informativni F i C nisu "greška u unosu".
- Provjera: `./mvnw test` 46/46; `yarn lint` i `yarn build` zeleno; endpointi provjereni `curl`-om na lokalnoj bazi (port 8092, jer su 8080 i 8091 zauzeti).

## 2026-11-03 (noć): #42 započet (grana `42-ui-fc-profil`, necommitano)
- #41 je spojen u `main` (PR #50), pa je #42 grana iz `main`.
- Faza 0 (backend) gotova: `GET /api/v1/indicators/{id}` (ravni `IndicatorResponse` + `contracts` + `subject`), `threshold` u `subject_ref` pravila F. `./mvnw test` 48/48.
- Portovi 8080/8091/8092 zauzeti tuđim `ApiApplication` procesima; API lokalno na 8093, `vite.config.ts` čita `API_URL`.
- Frontend (faze 1–3) nije započet. Detaljan popis gotovog i preostalog: `work/status-42.md`.

## 2026-11-03 (noć): #43 karta i usporedba općina (IvanMosa, grana `43-karta-usporedba`, necommitano)
- #42 ostaje Ninu; #43 je dodijeljen IvanMosi na GitHubu.
- Lokalno: nova baza (Docker), ingest i sintetika. Ovaj ingest ima 2.295 novih ugovora i 33 JLS s podacima; 21 datoteka se nije preuzela. API je radio na portu 8093, jer je na 8080 radio drugi projekt.
- Izvor granica: geoBoundaries HRV ADM2 (OSM). DGU i Overpass su odbačeni (razlozi u `DATA_SOURCES.md` §9a). Rezultat je `public/geo/jls.geojson` (580 KB) s 542/556 JLS; 14 JLS nema granicu i navedeni su uz kartu.
- Backend, paket `municipality`:
  - `GET /api/v1/municipalities` (svih 556);
  - `GET /api/v1/municipalities/{oib}/peers?year=&cpv2=` (medijan, rang, n, `sufficient`);
  - `CpvDivisions` (nazivi CPV odjeljaka) i `PeerGroups.labelOf`.
- Frontend:
  - `/karta`: SVG karta, legenda, bočna ploča, tablica s pretragom;
  - `/usporedba`: dot plot, tablica, "Što ovo NE znači";
  - poveznice u navigaciji.
- Provjera: `./mvnw test` 50/50; `yarn lint` i `yarn build` zeleno. U pregledniku su provjereni svijetli i tamni način, 1280 px, mobitel, tooltip, klik, prazno stanje te 404 i 400 iz API-ja.
- Nalaz: usporedba skoro uvijek ima n < 8, pa prikazuje "Nedovoljno podataka" (vidi `decisions.md`). Iznimka za demo: P1 2025. ima n = 8 (npr. Stankovci, CPV 45).
- Pregled prije PR-a: godina iz URL-a bez podataka sada ima svoju opciju; tooltip karte se otvara ulijevo u desnoj polovici; oznake medijana i odabrane JLS se više ne preklapaju.

## 2026-11-03 – refactor-ui
- Nova grana `refactor-ui` (od `43-karta-usporedba`). Frontend prebačen na dizajn sustav OtvoreniRadara: tokeni, fontovi, shell s mobilnim izbornikom, komponente (Button, Badge, PageHeader, stanja, Select, TextInput…), sve stranice na `PageHeader`/`BackLink`. Dodan `DESIGN_SYSTEM.md`, ažuriran `docs/UI_UX.md` §1. Tamni način uklonjen. Build i lint prolaze, provjereno u pregledniku (1280 px i mobitel). Na zahtjev korisnika commitano i otvoren PR #53 (baza `43-karta-usporedba`).

## 2026-11-03 – prompt za #44
- Napisan `work/prompts/next-44-ai-procjena.md`: faze 1–3 (shema, kontekst, validator, fixture klijent, cache, budžet, admin run s `dryRun`, API i blok u UI-ju) rade bez `ANTHROPIC_API_KEY`; faza 4 (evaluacija 40+60) traži ključ i potvrdu korisnika; faza 5 Sonnet je Should.
- Ploča: #42 je spojen (PR #52), ali issue je otvoren i u "In progress"; treba ga ručno zatvoriti.

## 2026-11-03 – #44 AI procjena, faze 1–3 (grana `44-ai-procjena`, necommitano)
- Backend: `V4__ai.sql` (`ai_assessment`, `llm_usage`), paket `assessment` (odabir kandidata, kontekst §4.2 s kanonskim JSON-om i hashom, validator §4.4 + strože dopune, run s cacheom, budžetom po runu i paralelnim pozivima, `POST /api/admin/ai/run`), `integration/anthropic` (`AnthropicLlmClient` sa strukturiranim izlazom, `NoopLlmClient` bez ključa). Prompt `ai-flag-v1` i shema su resursi u `resources/ai/`. Nova ovisnost `anthropic-java` 3.3.0.
- `GET /contracts/{id}` vraća `aiAssessment` ili razlog zašto ga nema; UI ima blok "AI procjena – zahtijeva ljudsku provjeru" na dnu detalja ugovora (oznaka riječima, jantarna, razlozi s poljima i poveznicama, legitimna objašnjenja uvijek vidljiva, što nedostaje).
- Testovi: 72 prolaze bez ključa (validator 15 slučajeva, kontekst, run s `FixtureLlmClient`: prihvaćen, odbačen, cache, budžet, API nedostupan, prikaz u API-ju; start bez ključa). `yarn lint` i `yarn build` prolaze.
- `dryRun` nad lokalnom bazom: 300 kandidata (gornja granica), gruba procjena 0,32 USD s Haikuom. Primjer konteksta pregledan; `statistika_skupine` je za većinu ugovora `null` (n < 8, isti nalaz kao #43).
- UI provjeren headless Chromeom (proširenje za Chrome nije bilo spojeno) na 1280 i 500 px s privremenim ručnim zapisom na sintetičkom ugovoru, koji je zatim obrisan. Stanja "obrt" i "nije dostupna" provjerena.
- Sljedeće: faza 4 (evaluacija) nakon potvrde ključa s limitom potrošnje.

## 2026-11-03 – #44 faza 4, prvi pravi pozivi (korisnik potvrdio ključ s limitom)
- 10 sintetičkih + 21 stvarni ugovor s `claude-haiku-5-5`, `ai-flag-v1`: 30 prihvaćeno, 0 odbačeno, 1 prolazna greška (restart devtoolsa usred runa; ponovljeno uspješno). Trošak ~0,0014–0,0016 USD po pozivu (≈ 5.400 ulaznih, 2.200 izlaznih tokena). Svih 2.212 prihvatljivih stvarnih ugovora ≈ 3 USD.
- Validator je u 9 od 30 odgovora odbacio pojedine razloge (citirano `null` polje, tvrdnja > 300 znakova); nijedan cijeli odgovor.
- **Ispravljeno:** pravilo C veže indikator na sve ugovore ćelije (naručitelj + CPV2), i one drugih dobavljača. Model je to primijetio kao nesklad. AI kontekst sada uzima C samo za ugovore najvećeg dobavljača (`topSupplierOib`). Isti prikaz postoji i u UI-ju (#41/#42), nije mijenjan.
- **Nalazi pregleda (čeka odluku korisnika):** (1) 17 od 20 stvarnih ugovora s indikatorima dobilo je "vrijedi provjeriti", a razlozi uglavnom ponavljaju pravila; (2) jedan razlog kaže "odstupa od usporednih općina" bez `statistika_skupine` u kontekstu; (3) korisni novi signali: CPV koji ne odgovara predmetu (sintetika), 1,78 M € "jednostavnom nabavom" (Bistra, ugovor 5007).

## 2026-11-04 – #44 faza 4: prompt v2/v3 i evaluacija
- Prompt `ai-flag-v2` (ne ponavljati pravila, usporedba samo s podacima) i `ai-flag-v3` (izuzeta nabava ispod praga je uobičajena, nedostajući podaci nisu razlog). Kontekst dobio prag jednostavne nabave i vrijednost s PDV-om.
- Popravljeno: `ConcurrentModificationException` u cacheu usporednih skupina (prolazna greška "null"); validator više ne odbija citiranje null polja (§4.4 doslovno); novo pravilo: usporedba s općinama bez `statistika_skupine` se odbacuje.
- Evaluacija (`docs/EVALUATION.md`): sintetika 40 slučajeva, v3 dva runa: preciznost 1,00, odziv 0,905/0,952, 0 lažno pozitivnih. Stvarni uzorak 60 (seed 44): 12/60 oznaka, kappa prema pravilima 0,444; ručne oznake još nema → metrike prema ljudima nisu izmjerene. CSV i upute u `work/eval/`.
- Novi endpointi `/api/admin/ai/eval/*` i `contractIds` na `/api/admin/ai/run`. Ukupni trošak do sada 0,66 USD.
- Lokalno: port 8080 drži tuđi stari `ApiApplication` (PID 19120, kao u `status-42.md`), 8091–8094 također zauzeti; moj API radi na **8095** s `SPRING_DEVTOOLS_RESTART_ENABLED=false` (devtools restart usred runa je rušio pozive).

## 2026-11-05 – #46 Sudreg, pravilo B, GDPR prikaz (grana `44-ai-procjena`)
- Sudreg ključ provjeren stvarnim zahtjevom (token 200, `detalji_subjekta` 200). Djelatnost dolazi u dva oblika (NKD 2025 u `pretezita_djelatnost` ili samo broj `glavna_djelatnost` kod starijih upisa); `DATA_SOURCES.md` §7.
- `V5__company_snapshot.sql`, `RestSudregClient` (preuzet iz `Desktop/datathon` i prilagođen), dohvat u pozadini `POST /api/admin/sudreg/run` + `GET /status` (1 zahtjev/s, samo `doo/jdoo/dd/javno_tijelo`, spremljen samo dio odgovora). Pun dohvat 1.577 dobavljača pokrenut 05.11. 18:28.
- AI kontekst: NKD, naziv djelatnosti, datum osnivanja, stečaj/likvidacija s izvorom i datumom dohvata. AI kandidati više ne uključuju `ostalo`/`nepoznato`.
- Pravilo B (`B_YOUNG_COMPANY`): ≤ 180 dana, ≥ 50.000 €, pojas po danima; sintetička fixture snimka za demo; UI filter "B".
- GDPR: nazivi tipa `ostalo`/`nepoznato` skriveni osim institucija i JPT tijela (u podacima ≥ 10 osoba); klasifikator prepoznaje ",vl.", "član", "Ured ovl.".
- Testovi: 88/88, `yarn lint` i `yarn build` prolaze. Popravljena ne-daemon dretva koja je usporavala kraj testova.
- Sudreg dohvat gotov: 1.568 od 1.577 pronađeno (9 × HTTP 400). Snimke: 1.448 s datumom osnivanja, 1.005 s NKD-om (289 NKD 2025), 17 u stečaju ili likvidaciji.
- Pravilo B na stvarnim podacima: 0 indikatora (31 mlada tvrtka, svi ugovori < 50.000 €); sintetički demo 1.
- AI stvarni uzorak sa Sudregom: 13/60 oznaka (9 na neskladu NKD-a), kappa prema pravilima 0,057, 0,069 USD. Upisano u `EVALUATION.md`.

## 2026-11-03 – #45 ispravak i Metodologija
- Ploča: #44 je In progress (Nino), pa je naš sljedeći #45. Grana `45-ispravak-metodologija` od `refactor-ui`.
- Backend: `V4__feedback.sql` (pri spajanju preimenovana u `V6`), `POST /api/v1/feedback` (validacija, 404 za skrivene ciljeve, 10/min po IP-u), 7 novih testova; `./mvnw test` zeleno. Lokalni API na 8093 ponovno pokrenut s novim jarom (migracija V4 primijenjena).
- Frontend: `/ispravak` (obrazac), `/metodologija` (pravila D1, D2, F, C s pragovima i izvorima), poveznice s indikatora i ugovora, Metodologija u navigaciji. Build i lint prolaze, provjereno u pregledniku. Testne prijave obrisane iz lokalne baze. Commitano, PR #54 prema `main` (#51 i #53 su u međuvremenu spojeni).

## 2026-11-06 – #45 spojen s `main`
- `main` (s #44 AI i #46 Sudreg) spojen u `45-ispravak-metodologija`. Konflikti u dokumentaciji riješeni zadržavanjem oba sadržaja. Migracija `V4__feedback.sql` preimenovana u `V6__feedback.sql`, jer je `V4` zauzeo `V4__ai.sql`, a `V5` `V5__company_snapshot.sql`. Lokalna baza koja već ima staru `V4` (feedback) treba se ponovno stvoriti.

