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
