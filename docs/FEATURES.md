# JavniRadar – funkcionalnosti

Prioritet: **M** = Must (MVP do 9.11.), **S** = Should (ako stignemo do 7.11.), **C** = Could / Future.
Svaka funkcionalnost ovisi o podacima iz `DATA_SOURCES.md` i tablicama iz `DATA_MODEL.md`.

| ID | Funkcionalnost | Prioritet |
|---|---|---|
| F1 | Pretraga i filtri ugovora | M |
| F2 | Profil općine/grada (JLS) | M |
| F3 | Profil dobavljača | M (bez Sudrega), S (sa Sudregom) |
| F4 | Usporedba općina (kategorija po stanovniku) | M |
| F5 | Indikatori (pravila C, D, F) s objašnjenjem | M |
| F6 | AI procjena | M (prvi prolaz Haiku), S (Sonnet provjera) |
| F7 | Karta županije/Hrvatske | M |
| F8 | Dokaz i izvor uz svaki broj | M |
| F9 | Vremenska crta ugovora | S |
| F10 | AI ekstrakcija iz nestrukturiranih registara | S |
| F11 | Pretraga prirodnim jezikom | S |
| F12 | Ispravak i pravo na odgovor | M (obrazac), C (moderacija) |
| F13 | Praćenje i obavijesti (watchlist) | C (S: samo RSS/e-mail sažetak) |
| F14 | Stranica "Izvori i metodologija" | M |
| F15 | Pravilo B (nova tvrtka) | S (uvjetno o Sudreg ključu) |
| F16 | Pravilo G (vremenski obrasci) | S |

---

### F1 Pretraga i filtri — M
- **User story:** Kao lokalna novinarka, želim filtrirati ugovore svoje općine po CPV kategoriji i iznosu da brzo nađem najveće nabave.
- **Podaci:** `contract`, `organization`.
- **Logika:**
  - filteri: naručitelj, OIB dobavljača, CPV (2 ili 8 znamenki), vrsta postupka, raspon iznosa, datum, "ima indikator";
  - pretraga po tekstu predmeta i nazivima (`pg_trgm`);
  - paginacija po 50 zapisa.
- **UI:**
  - tablica gustog prikaza (datum, naručitelj, dobavljač, predmet, CPV, iznos, oznake);
  - filteri su u URL-u, pa se pretraga može podijeliti;
  - izvoz CSV-a.
- **Rubni slučajevi:**
  - iznos `NULL` prikazuje se kao "nije objavljeno";
  - HRK ugovori dobivaju oznaku "preračunato iz kn";
  - ugovori s fizičkim osobama se ne prikazuju;
  - bez rezultata: prijedlog da se proširi filter i napomena o pokrivenosti podataka.

### F2 Profil JLS — M
- **User story:** Kao vijećnik, želim vidjeti koliko moja općina troši po kategorijama i po stanovniku, u usporedbi sa sličnim općinama.
- **Podaci:** `municipality` (DZS, JPT), `contract`, `indicator`.
- **Logika:**
  - ukupna vrijednost po godini i CPV2;
  - € po stanovniku;
  - top 10 dobavljača;
  - broj indikatora po pravilu;
  - **pokrivenost podacima** ("Podaci iz registra ugovora za 2024–2025, izvor: …").
- **UI:** zaglavlje s brojem stanovnika i usporednom skupinom; tri kartice (potrošnja, dobavljači, indikatori); graf kategorija; tablica ugovora.
- **Rubni slučajevi:**
  - JLS bez podataka: "Ova općina ne objavljuje strojno čitljiv registar ugovora na data.gov.hr." To je i samo po sebi informacija o transparentnosti;
  - samo jedna godina podataka: nema trenda.

### F3 Profil dobavljača — M/S
- **User story:** Kao istraživač, želim vidjeti s kojim sve naručiteljima dobavljač ima ugovore.
- **Podaci:** `contract`, `organization`, `company_snapshot` (S).
- **Logika:** ugovori po naručitelju, ukupno po godini, udio u svakom naručitelju. Sa Sudregom: datum osnivanja, NKD, pravni oblik, stečaj.
- **UI:** "Podaci iz Sudskog registra, dohvaćeno {datum}". Bez ključa: "Registarski podaci nisu dostupni."
- **Rubni slučajevi:**
  - obrt: nema Sudrega, prikazuje se samo naziv iz ugovora; ime vlasnika se ne ističe niti se po njemu pretražuje;
  - strani dobavljač bez OIB-a.

### F4 Usporedba općina — M
- **User story:** Kao građanin, želim usporediti potrošnju svoje općine na ceste sa sličnim općinama.
- **Podaci:** usporedna skupina (P1–P7), `contract` agregati.
- **Logika:** scenarij A, razina 2 (`ANOMALY_DETECTION.md` §3A). Usporedba samo unutar JLS koje **imaju podatke**; n se prikazuje.
- **UI:** dot plot i tablica; uz graf trajna napomena "Usporedba kategorija, ne jediničnih cijena".
- **Rubni slučajevi:** manje od 8 usporednih JLS s podacima daje "Nedovoljno podataka za pouzdanu usporedbu".

### F5 Indikatori pravila — M
- **User story:** Kao revizor, želim popis ugovora s neuobičajenim obrascima i transparentnim objašnjenjem.
- **Podaci i logika:** `ANOMALY_DETECTION.md` (C, D, F).
- **UI:**
  - značka s nazivom pravila, ne s "ocjenom";
  - tri dimenzije kao tri zasebne oznake;
  - detalj indikatora (vidi `UI_UX.md`).
- **Rubni slučajevi:**
  - indikator osporen preko F12 dobiva oznaku "osporeno" i vidljiv odgovor;
  - promjena verzije pravila: stari indikator se ne prikazuje.

### F6 AI procjena — M/S
- **User story:** Kao novinarka, želim da me alat upozori na kombinacije signala koje pojedinačna pravila ne vide, uz razloge koje mogu provjeriti.
- **Podaci i logika:** `ANOMALY_DETECTION.md` §4.
- **UI:**
  - zaseban blok ispod indikatora pravila, s natpisom **"AI procjena – zahtijeva ljudsku provjeru"**;
  - oznaka, razlozi s poveznicama na polja i izvore, "moguća legitimna objašnjenja" (uvijek vidljiva, nisu skrivena), nedostajući podaci;
  - model i datum;
  - gumbi "Korisno / Pogrešno".
- **Rubni slučajevi:**
  - nema API ključa ili budžeta: blok "AI procjena nije izračunata";
  - odgovor odbačen u validatoru: ne prikazuje se ništa;
  - obrt ili fizička osoba: AI se ne poziva.

### F7 Karta — M
- **User story:** Kao gledatelj videa, u jednoj slici želim vidjeti gdje postoje podaci i gdje su indikatori.
- **Podaci:** granice JLS (GeoJSON) [NEVERIFICIRANO izvor; kandidat: DGU/INSPIRE WFS na data.gov.hr ili `geoBoundaries` CC-BY], `municipality`, `indicator`.
- **Logika:** boja = broj indikatora na 100 ugovora; **sivo = nema strojno čitljivih podataka** (posebna kategorija, ne nula).
- **UI:** klik na JLS otvara bočnu ploču sa sažetkom i poveznicom na profil. Legenda ima i varijantu prilagođenu daltonizmu.
- **Rubni slučajevi:** ako se GeoJSON ne nađe do 4.11., fallback je karta županija ili mreža (tile grid) općina.

### F8 Dokaz uz svaki broj — M
- Svaki iznos i indikator ima ikonu ⓘ s izvorom: naziv skupa, izdavatelj, poveznica na datoteku, redak ili stranica, datum dohvata, metoda (parser/LLM s pouzdanošću), te poveznica na `eojn.hr/contract-eo/…` kad postoji.
- **Prihvatni kriterij:** nijedan broj u UI-ju ne postoji bez `provenance_id`.

### F9 Vremenska crta ugovora — S
- Plan nabave (ako je spojen) → sklapanje → izmjene → izvršenje i isplata.
- Veza plan → ugovor je heuristička (isti naručitelj, godina, CPV3, sličnost predmeta ≥ 0,5) i označena "moguća veza".
- Nedostajući korak se prikazuje isprekidano: "nije objavljeno".

### F10 AI ekstrakcija iz registara klase B/C/D — S
- **User story:** Kao tim, želimo uključiti male općine koje objavljuju neuredne tablice ili PDF.
- **Logika:**
  - ulaz: redak ili stranica (tekst);
  - izlaz po shemi: `{subject, supplier_name, supplier_oib, date_signed, value_net, value_gross, currency, is_periodic, contract_kind}`, uz `field_confidence` za svako polje;
  - zapis s pouzdanošću < 0,8 ide u ručni pregled i ne ulazi u pravila;
  - uz zapis se uvijek čuva poveznica na izvorni redak ili stranicu.
- **Model:** `claude-haiku-5-5`, cache po `sha256(redak)`.
- **Rubni slučajevi:**
  - "mjesečno": `is_periodic = true`, bez množenja;
  - fizička osoba: `contract_kind` = osobni ugovor, pa se zapis isključuje iz prikaza.

### F11 Pretraga prirodnim jezikom — S
- Primjer upita: "koliko je Općina X potrošila na asfalt 2025."
- LLM **ne odgovara izravno**, nego prevodi upit u filter: `{buyer:"Općina X", cpv_prefix:["45233"], year:2025, metric:"sum"}`. Upit izvršava baza.
- UI prikazuje primijenjeni filter, koji korisnik može ispraviti, i rezultat iz baze.
- Ako je upit nejasan, nudi izbor.
- Bez LLM-a odmah pada na običnu pretragu.

### F12 Ispravak i pravo na odgovor — M (obrazac)
- **User story:** Kao pročelnik općine, želim prijaviti grešku ili dodati kontekst uz indikator.
- **Logika:** `POST /feedback`. Odgovor naručitelja ili dobavljača, nakon ručne moderacije, prikazuje se uz indikator ("Odgovor Općine X, {datum}"). Status indikatora se mijenja u `osporen` ili `ispravljen`.
- **Rubni slučajevi:** zahtjev za uklanjanje osobnih podataka obrađuje se u roku od 7 dana.

### F13 Praćenje — C
- Praćenje općine, dobavljača ili CPV kategorije. MVP: RSS feed po JLS-u (`/api/v1/buyers/{oib}/feed.xml`), bez korisničkih računa.
- Future: e-mail obavijesti uz double opt-in.

### F14 Izvori i metodologija — M
- Popis izvora s licencama, datumima i pokrivenošću (556 JLS: koliko ih ima podatke).
- Pravila s formulama i pragovima, evaluacija AI-ja, politika jezika, ograničenja.

### F15 Pravilo B — S
- Vidi `ANOMALY_DETECTION.md` §3B. Uključuje se samo ako Sudreg ključ stigne do 4.11. Inače postoji samo u sintetičkom demu.

### F16 Pravilo G — S
- Vidi §3G. Prikaz na profilu JLS (graf po mjesecima), bez indikatora ako nema bazne linije.
