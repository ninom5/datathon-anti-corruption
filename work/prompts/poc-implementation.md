# Prompt: implementacija POC-a (vertikalni presjek)

_Napisano 2026-11-02. Kopirati u novu Claude Code sesiju u rootu repoa._

---

Implementiraj POC JavniRadara: jedan **vertikalni presjek od izvora do ekrana** na stvarnim podacima. Ne cijeli MVP.

Stack je potvrđen: **Java / Spring Boot** (`apps/api`) + React (`apps/web`).

## Prije koda (obavezno)
1. Pročitaj `CLAUDE.md`, `AGENTS.md`, `docs/PROJECT_OVERVIEW.md`, `docs/MVP_ROADMAP.md`, `docs/DATA_SOURCES.md`, `docs/DATA_MODEL.md`, `docs/ARCHITECTURE.md`, `docs/ANOMALY_DETECTION.md` (samo pravilo D i §AI za sada) i `docs/UI_UX.md` (samo istraživač ugovora).
2. Učitaj skillove `spring-boot-api` i `react-web`.
3. Pogledaj postojeći skelet u `apps/api` i `apps/web` i reusable kod u `C:\Users\Nino\Desktop\datathon` (samo čitaj, ne mijenjaj taj repo; kopirane klase dobivaju komentar o porijeklu).
4. Napiši mi kratak plan (datoteke, redoslijed, što namjerno izostavljaš) i **pričekaj potvrdu** prije pisanja koda.

## Opseg POC-a (gotovo kad sve ovo radi)
**Backend (`apps/api`):**
- Flyway `V1__init.sql` prema `docs/DATA_MODEL.md`: barem `source`/`provenance`, `buyer`, `supplier`, `contract`, `indicator`, s `is_synthetic`.
- Kopirati `PoliteHttpClient` (+ `RobotsRules`, `HostThrottle`, `DiskCache`, `TextDecoder`), `AmountParser`, `CroatianDates`; promijeniti User-Agent u `JavniRadar/0.1 (studentski projekt; kontakt)`.
- CKAN discovery na data.gov.hr: pronađi resurse "registar ugovora" i klasificiraj ih (A/B/C/D po `DATA_SOURCES.md`). U POC-u se parsira **samo klasa A** (standardni EOJN XLSX).
- Parser klase A: traženje reda zaglavlja, normalizacija iznosa/datuma/HRK→EUR, OIB checksum, dedup po EOJN URL-u. Svaki redak dobiva provenanciju (URL datoteke, list, redak, vrijeme dohvata, metoda, pouzdanost).
- Pravilo **D** (izmjene/preplate) prema `ANOMALY_DETECTION.md`. Pragovi idu u `thresholds.yaml` s datumom važenja. Tri dimenzije se vode odvojeno (kvaliteta podataka, veličina odstupanja, pouzdanost dokaza). Najmanje 3 jedinična testa: pozitivan, negativan, rubni.
- REST: `GET /api/v1/contracts` (filtri: JLS, godina, dobavljač, ima indikator; paginacija), `GET /api/v1/contracts/{id}` (s provenancijom i indikatorima), `GET /api/v1/sources`.
- Ingest se pokreće komandom ili admin endpointom, ne pri svakom startu. Demo mora raditi iz baze kad je data.gov.hr nedostupan.
- Automatski test koji pretražuje stringove u kodu/resursima za zabranjene riječi iz `AGENTS.md`.

**Sintetički testni skup s poznatim ishodom (ground truth):**
- Cilj: provjeriti radi li pravilo D zaista. Znamo unaprijed koji zapisi **moraju** biti označeni, a koji **ne smiju**.
- Napravi seed (npr. `V2__synthetic_demo.sql` ili `src/main/resources/synthetic/*.xlsx` u **istom EOJN formatu kao klasa A**). Tako sintetika prolazi **isti parser i normalizaciju** kao stvarni podaci, a ne samo izravan unos u bazu.
- Pravila iz `AGENTS.md`: fiktivni nazivi ("Općina Primjerovo", "Primjer d.o.o."), OIB-i `000000000xx`, `is_synthetic = true`, izvor/provenancija označena kao sintetička. Ne ulaze u statistike stvarnih JLS. U UI-ju imaju traku **"SINTETIČKI PODACI ZA DEMONSTRACIJU"**.
- Sadržaj, barem 20 ugovora u 2 fiktivne općine:
  - **jasno pozitivni** (≥ 5): npr. izmjena iznad praga iz `thresholds.yaml`, isplaćeno znatno više od ugovorenog, više uzastopnih izmjena istog ugovora;
  - **jasno negativni** (≥ 5): uredni ugovori bez izmjena, isplata ≤ ugovorenog;
  - **rubni** (≥ 5): točno na pragu, malo ispod/iznad, nedostaje iznos isplate, HRK iznos prije 2023., izmjena s istim iznosom;
  - **loša kvaliteta podataka** (≥ 2): neispravan OIB checksum, nečitljiv datum. Ovi moraju sniziti dimenziju kvalitete podataka, a ne napuhati veličinu odstupanja.
- Svaki sintetički zapis ima polje/oznaku **očekivanog ishoda** (`expected_flag`, razlog). Drži je u fixture datoteci ili testu, ne u produkcijskoj tablici.
- Integracijski test (Testcontainers ili H2 ako skill tako kaže) učita skup kroz parser i pokrene pravilo D. Zatim usporedi stvarne i očekivane oznake i ispiše matricu: TP / FP / TN / FN + popis promašaja s razlogom. Test pada ako postoji ijedan FN ili FP među jasnim slučajevima. Rubni slučajevi se samo izvještavaju.
- Test provjerava i da sintetički zapisi ne ulaze u agregate stvarnih podataka.
- Istraživač ugovora ima filter "prikaži sintetičke" (zadano isključeno), da se demo slučajevi mogu pokazati.

**Frontend (`apps/web`):**
- Istraživač ugovora prema `docs/UI_UX.md`: tablica s filtrima, oznaka indikatora D, detalj ugovora s poveznicom na izvor i lokatorom retka.
- Stranica Izvori (popis skupova, izdavatelj, poveznica, vrijeme dohvata).
- Neutralan jezik u svim tekstovima.

## Izvan opsega POC-a (ne raditi sada)
Pravila F i C, AI procjena, Sudreg, JPT/DZS, karta, profil JLS, deploy. Sintetički skup za F i C dolazi uz ta pravila. To dolazi nakon POC-a po `MVP_ROADMAP.md`. Ako ti nešto od toga treba za presjek, pitaj.

## Pravila rada
- Ne izmišljaj endpointe, nazive stupaca ni pragove. Prvo napravi stvarni zahtjev ili otvori stvarnu datoteku. Datum, URL, status i uzorak upiši u `docs/DATA_SOURCES.md`.
- Ako se stvarnost razlikuje od `docs/`, ne prilagođavaj je potiho. Zapiši razliku i odluku u `work/decisions.md`, s obrazloženjem.
- Ako odstupiš od `DATA_MODEL.md` ili `ARCHITECTURE.md`, ažuriraj taj dokument u istom koraku.
- Radi u fazama: (1) shema + ingest + parser, (2) pravilo D + sintetički skup s očekivanim ishodima + testovi, (3) API, (4) UI. Na kraju svake faze pokreni testove, ukratko javi stanje i ažuriraj `work/tasks.md` i `log.md`. Tek onda kreni dalje.
- Prije predaje mora proći `./mvnw test` u `apps/api` te `yarn build` i `yarn lint` u `apps/web`. Ako nešto ne prolazi, reci to s outputom.
- Ne commitaj i ne pushaj bez mog izričitog zahtjeva. Tajne samo u env varijablama.

## Prihvatni kriteriji POC-a
- [ ] Baza sadrži stvarne ugovore iz **≥ 10 JLS** klase A, i svaki zapis ima provenanciju.
- [ ] Pravilo D radi na stvarnim podacima. Rezultat: popis 10 primjera indikatora za ručnu provjeru lažno pozitivnih.
- [ ] Istraživač ugovora u browseru prikazuje stvarne podatke s filtrima i poveznicom na izvor.
- [ ] Sintetički skup: svi jasni slučajevi točno klasificirani (0 FP, 0 FN). Rubni slučajevi su izvješteni s objašnjenjem. Sintetika je vidljivo označena i isključena iz stvarnih statistika.
- [ ] Testovi, build i lint prolaze.
- [ ] Kratko izvješće na kraju: broj JLS, broj ugovora, broj odbačenih redaka i razlozi, broj indikatora D, matrica rezultata na sintetičkom skupu, poznati problemi.
