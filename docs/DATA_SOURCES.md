# JavniRadar – izvori podataka

_Zadnja provjera: **2026-11-01**. Sve oznake **[VERIFICIRANO]** u ovom dokumentu temelje se na stvarnim HTTP zahtjevima napravljenima tog dana, osim ako je uz oznaku naveden drugi datum. Skripte i uzorci su u privremenom direktoriju i nisu u repou. Kako se provjere ponavljaju, opisano je u odjeljku 12._

Oznake: **[VERIFICIRANO]** = testirano (datum, rezultat) · **[SAMO DOKUMENTIRANO]** = postoji dokumentacija, endpoint nije pozvan · **[NEVERIFICIRANO]** = nije provjereno · **[NEDOSTUPNO]** = provjereno, ali ne radi bez registracije ili uopće · **[PRETPOSTAVKA]** = zaključak koji nije izravno provjeren.

---

## 0. Sažetak: što to znači za MVP

| # | Izvor | Uloga u MVP-u | Pristup | Status |
|---|---|---|---|---|
| 1 | **Općinski registri ugovora u standardnom EOJN XLSX formatu** (data.gov.hr) | **Glavni izvor** za pilot (~30 JLS) | CKAN API → izravan download | VERIFICIRANO |
| 2 | Nacionalni izvoz "Registar Ugovora – EOJN sustav" (data.gov.hr, JSON) | Benchmark kategorija i usporedna skupina; **nepotpun uzorak** | CKAN API → download | VERIFICIRANO |
| 3 | EOJN OCDS (preko OCP Data Registryja) | Povijest 2018–2024 i interoperabilnost; **nema OIB-a** | Bulk JSONL/CSV/XLSX | VERIFICIRANO |
| 4 | JPT – Popis javnopravnih tijela (MPUDT) | Šifrarnik naručitelja: OIB → tip, županija, osnivači | **Pravi REST API** | VERIFICIRANO |
| 5 | DZS Popis 2021 po gradovima/općinama | Broj stanovnika, iznosi po stanovniku, usporedne skupine | XLSX preko CKAN-a | VERIFICIRANO |
| 6 | Sudski registar (sudreg-data.gov.hr) | Datum osnivanja i NKD dobavljača (pravilo B, AI procjena) | REST API + OAuth2 | NEDOSTUPNO bez ključa (401) |
| 7 | Ostali registri uz općinske (formati B/C/D, PDF) | AI ekstrakcija (demo inovacije) | download + LLM | VERIFICIRANO (uzorak) |
| 8 | TED API | Ugovori iznad EU pragova (kasnije) | Anonimni REST | VERIFICIRANO |
| 9 | Registar birača (broj birača po adresi) | Rezervni proxy za stanovništvo | CSV 80 MB | SAMO METAPODACI |
| 10 | e-Oglasna ploča sudova API | Stečajevi dobavljača (Future) | REST, bez ključa u specifikaciji | SAMO DOKUMENTIRANO |
| 11 | SRDI | Nije potreban za MVP | REST export | VERIFICIRANO |

**Tri ključna nalaza koja mijenjaju prvotnu pretpostavku:**
1. Nacionalni JSON izvoz s data.gov.hr je **uzorak, ne cijeli registar**. Ima točno 20.000 redaka. Grad Osijek u njemu ima 32 ugovora, a u vlastitom EOJN izvozu 588, i nijedan ID se ne poklapa. **Ne smije se koristiti za tvrdnje o pojedinoj općini** ("općina X ima samo N ugovora").
2. EOJN **objavljuje OCDS** (mjesečno, od 2018.), ali taj izvoz **nema OIB-a, nema broja ponuda**, a 2025. i 2026. su gotovo prazne (13.415 i 132 releasea).
3. **Broj ponuditelja nije dostupan ni u jednom provjerenom strojno čitljivom izvoru.** Scenarij E (niska konkurencija) se zato može prikazati samo kroz proxy (vrsta postupka).

---

## 1. Općinski registri ugovora na data.gov.hr (GLAVNI IZVOR)

| Svojstvo | Vrijednost |
|---|---|
| URL | `https://data.gov.hr/ckan/api/3/action/package_search?q=registar ugovora` |
| Izdavatelji | pojedine JLS, županije i ustanove |
| Licenca | `open-license` (Otvorena dozvola RH), uz obvezno navođenje izvora |
| Pristup | CKAN API za otkrivanje (JSON, bez autentikacije), zatim izravan HTTP download resursa. `datastore_active=false`, dakle **nije DataStore API** |
| Svježina | nije deklarirana. `metadata_modified` je kod svih skupova 2026. godina, što je vjerojatno posljedica migracije portala, pa to nije pouzdan pokazatelj svježine **[PRETPOSTAVKA]** |

### 1.1 Pokrivenost (izmjereno CKAN API-jem, 2026-11-01) [VERIFICIRANO]

| Mjera | Vrijednost |
|---|---|
| Skupovi za `q=registar ugovora` | 139 skupova, 105 izdavatelja, od toga **90 JLS** |
| Skupovi s naslovom "registar ugovora" | 105 skupova, 81 izdavatelj, 69 JLS |
| Skupovi sa strojno čitljivim resursom (CSV/XLS/XLSX/JSON/XML) | 84 skupa, 70 izdavatelja, **61 JLS** |
| Formati resursa | XLSX 63, PDF 35, bez formata 15, CSV 14, DOCX 12, XLS 11, JSON 10, HTML 8, XML 5 |
| Ukupno JLS u RH | 556 (128 gradova + 428 općina; DZS Popis 2021, JPT) |
| Organizacije na portalu | 392 izdavatelja; od toga 90 "Grad…", 175 "Općina…", 24 "…županija" |

**Procjena: oko 11 % JLS (61/556) objavljuje strojno čitljiv registar ugovora na data.gov.hr.**

Preuzeli smo 166 strojno čitljivih resursa: 148 je vratilo HTTP 200, 5 HTTP 403, 5 HTTP 404, 1 HTTP 502 i 7 nije uspjelo (000). Klasifikacija po zaglavlju:

| Klasa | Datoteka | Izdavatelja | Opis | Upotreba |
|---|---|---|---|---|
| **A: standardni EOJN izvoz** | 53 | 34 (od toga ~30 JLS) | isti 33-stupčani format iz EOJN-a, s poveznicama `https://eojn.hr/contract-eo/{id}` | **parser** (MVP) |
| B: vlastita tablica s OIB-om | 25 | 11 | stupci se razlikuju po izdavatelju | parser po izdavatelju ili LLM |
| C: vlastita tablica bez OIB-a | 27 | 8 | samo naziv ugovorne strane | LLM + spajanje po nazivu |
| D: nestrukturirano | 34 | 17 | naslovi, spojene ćelije, tekst | LLM |
| XLS/XML (nije parsirano) | 18 | 14 | – | kasnije |

**Strogo parsirani podskup klase A** (zaglavlje točno u prvom retku): **2.425 jedinstvenih ugovora** (deduplicirano po EOJN URL-u, 659 duplikata među datotekama), 17 izdavatelja, godine 2024: 1.022, 2025: 1.337, 2026: 66.
Najviše zapisa imaju Grad Osijek 586, Općina Sveti Filip i Jakov 273, Grad Hvar 264, Grad Ozalj 263, Grad Ivanec 241, Grad Čakovec 223, Općina Mikleuš 130 i Općina Kloštar Ivanić 114.
Ostalih ~17 izdavatelja klase A ima zaglavlje pomaknuto za nekoliko redaka. Robustan parser ih mora naći tražeći red zaglavlja.

### 1.1a Ponovna provjera ingestom, 2026-11-02 [VERIFICIRANO]
Prvi stvarni run ingesta (`POST /api/admin/ingest/run`, `PoliteHttpClient`, 1 zahtjev/s po hostu, ~5 min):
- CKAN `package_search?q=registar ugovora&rows=1000` vraća HTTP 200 sa **139 skupova**. Formati resursa sada su XLSX 103, PDF 65, CSV 34, DOCX 24, bez formata 16, XLS 14, HTML 10, JSON 10, XML 5, ostalo 3. Brojke se razlikuju od 1.11. jer su se tada brojali samo skupovi s naslovom "registar ugovora".
- `https://data.gov.hr/robots.txt` vraća **HTML SPA s HTTP 200**, ne pravi robots.txt. `RobotsRules` u njemu ne nalazi zabrane.
- **Preuzeto 153 datoteke** (XLSX/XLS/CSV):
  - klasa A: 21 datoteka, 18 izdavatelja;
  - B: 52; C: 52; D: 2;
  - neuspio download: 20 (Grad Zagreb 403 ×6, Grad Rijeka 404 ×2, Grad Hvar SSL certifikat ×2, NEAKTIVNO ministarstvo ×6, ostalo 404 ×3, neispravan URL ×1);
  - nečitljivo: 6 (XLSX/XLS koje POI ne otvara, vjerojatno HTML ili stari format pod pogrešnim nastavkom).
- Ostalih 168 resursa (PDF, DOCX, HTML, JSON, XML, bez formata) u POC-u se ne preuzima.
- **Klasa A** (točno 33-stupčani EOJN izvoz s poveznicom `eojn.hr/contract-eo`):
  - 2.922 retka, **2.332 jedinstvena ugovora**; 590 redaka su duplikati iz novijih datoteka;
  - 19 naručitelja: **17 JLS**, 1 županija, 1 dječji vrtić;
  - datumi 2024-01-01 – 2026-04-14;
  - 0 odbačenih redaka; 7 ugovora s neispravnim OIB-om dobavljača (ne 11 znamenki).
  - To se slaže sa "strogo parsiranim podskupom" iz §1.1 (2.425 / 17 izdavatelja).
- **Novi nalaz: "stari EOJN izvještaj".** Zbroj od "53 datoteke klase A" iz §1.1 uključivao je i **stariji EOJN izvještaj registra**:
  - oko 20 XLSX datoteka od 14+ izdavatelja (npr. Korčula 2018–2022, Mikleuš 2021, Čakovec, Makarska, Zlatar, Rovinj);
  - ima 18 stupaca (`Evidencijski broj nabave`, `Naziv i OIB ugovaratelja` u **jednoj ćeliji**, `Datum sklapanja`, `Iznos bez PDV-a`, `Ukupni isplaćeni iznos s PDV-om`, `Obrazloženja`…);
  - zaglavlje je u 13.–14. retku, ispod naslova "REGISTAR UGOVORA";
  - **nema poveznice na eojn.hr ni stupca izmjene**, a godine su uglavnom do 2023.;
  - klasifikator ga sada vodi kao **B**. Odluka je u `work/decisions.md`.
- Testni uzorak: `apps/api/src/test/resources/fixtures/eojn/grad-osijek-registar-ugovora.xlsx` (HTTP 200, 161.561 B, 588 redaka; datum sklapanja je Excel serijski broj, npr. `46003` = 2025-12-12; zaglavlje `Iznos s PDV-om ` ima razmak na kraju).


**Provjera točnosti parsera klase A (2026-11-02) [VERIFICIRANO]:**
- 10 najvećih i 10 nasumičnih ugovora uspoređeno je s izvornim ćelijama (openpyxl, neovisno o POI-u). Uspoređeni su naručitelj, dobavljač, OIB, datum sklapanja, iznos bez PDV-a, iznos s PDV-om i ukupni iznos. **Svih 20 se podudara**; greške u parseru nema.
- Omjer iznosa s PDV-om i bez njega: 1,25 u 1.959 ugovora, 1,00 u 199, 1,13 u 39, 1,05 u 23, nestandardan u 112. Zbroj početnog iznosa i izmjene uvijek je jednak ukupnom.
- Nema ugovora prije 2023. ni HRK iznosa u klasi A.
- Neobični iznosi **vjerno su preneseni iz izvora** i nisu greška parsera:
  - Ozalj (32) i Mikleuš (11) objavljuju i narudžbe ispod 100 EUR;
  - Lepoglava: isplate 516902 i 4452369 bez decimalnog zareza; iznos s PDV-om 700 uz PDV −6.300;
  - Mikleuš: jedinična cijena 30,09 EUR upisana kao iznos ugovora.
- Posljedica: pravilo D ima zaštite za takve retke (`ANOMALY_DETECTION.md` §D, "Implementacija").

### 1.1b Stari EOJN izvještaj (klasa A2), parser od 2026-11-03 [VERIFICIRANO]
- **Prepoznavanje:** red zaglavlja (u prvih 25 redaka) s barem 10 prepoznatih stupaca, među kojima moraju biti `Naziv i OIB ugovaratelja`, `Datum sklapanja…`, `Iznos bez PDV-a…` i `Predmet nabave`. Zaglavlja se uspoređuju po **početku teksta**, jer ih izdavatelji produljuju (`Datum sklapanja ugovora ili okvirnog sporazuma`, `Iznos bez PDV-a (kn)`). Stupci su u `LegacyColumn`.
- **Dvije inačice u stvarnim datotekama:**
  - EOJN izvoz "RPT_Ugovor": zaglavlje u 13.–14. retku, iznad njega "REGISTAR UGOVORA" i **"Naručitelj: GRAD X"**, a iznosi su tekst s valutom (`"198.338,34 HRK"`, `"4.000,00 EUR"`);
  - pretipkane godišnje tablice (npr. HERA, Korčula): zaglavlje u 1.–4. retku, valuta u zaglavlju (`(kn)`, `(EUR)`) i obični brojevi u ćelijama.
- **Naručitelj:** stupca nema. Uzima se iz retka "Naručitelj:", a ako ga nema, iz izdavatelja skupa na CKAN-u. OIB se traži u JPT-u po točnom normaliziranom nazivu koji mora biti jedinstven. Ako ga JPT ne nađe, koristi se postojeća organizacija s istim normaliziranim nazivom i OIB-om. Na stvarnim podacima svi A2 naručitelji (23) dobili su OIB.
- **Dobavljač:** naziv i OIB su u jednoj ćeliji (`"Crodux plin d.o.o. 50388109754"`, `"HRVATSKI TELEKOM d.d.\nOIB: 81793146560"`). Uzima se prvi 11-znamenkasti broj. Ako ih je više (zajednica ponuditelja), uzima se nositelj i dodaje oznaka `supplier_consortium`. Strani PDV brojevi (`SI82719241`) nisu OIB.
- **Iznosi:** nema iznosa izmjene ni ukupnog iznosa s izmjenama. Puni se samo početni iznos (`Iznos bez PDV-a`, `Ukupni iznos s PDV-om`), pa D1 preskače A2, a D2 uspoređuje isplatu s početnim iznosom s PDV-om. Valuta se određuje redom: oznaka u ćeliji, oznaka u zaglavlju, pa datum (prije 2023. = HRK).
- **Prvi run (2026-11-03):**
  - 32 datoteke A2 (prije su bile u klasi B), 4.562 retka;
  - **3.626 novih ugovora**: 2014: 1, 2017: 10, 2018: 421, 2019: 559, 2020: 500, 2021: 715, 2022: 802, 2023: 505, 2024: 70, 2025: 43;
  - 926 duplikata iz novijih godišnjih datoteka;
  - odbačeno 10 redaka (8 bez datuma sklapanja, 2 bez iznosa);
  - upozorenja: `unreadable_completed_on` 34, `cpv_invalid` 17, `supplier_oib_missing` 14, `supplier_consortium` 7, `amount_out_of_range` 7, ostalo ≤ 5;
  - **preklapanje s klasom A po (naručitelj, godina): 0 redaka**, jer nijedan naručitelj nema oba formata za istu godinu. Pravilo je pokriveno testom (`LegacyImportTest`);
  - ukupno sada **5.958 ugovora, 38 naručitelja, od toga 34 JLS** (prije 17).
- **Provjera točnosti (2026-11-03):** 10 najvećih i 10 nasumičnih A2 ugovora uspoređeno je s izvornim ćelijama (openpyxl, neovisno o POI-u): datum, OIB dobavljača, iznos bez PDV-a, s PDV-om i isplaćeni iznos, uključujući pretvorbu HRK→EUR. **Svih 20 se podudara.**
- **Greške u izvoru (vjerno prenesene, ne ispravljamo ih):**
  - iznos s PDV-om manji od iznosa bez PDV-a (npr. Rovinj 2023: 4.211.200 € bez PDV-a i 52.640 € s PDV-om). Dobivaju oznaku `gross_below_net`;
  - **zamjena kune i eura u isplati:** EOJN izvještaj sve iznose označava kao "EUR", a izdavatelji su za ugovore iz 2022. isplatu upisivali u kunama (Općina Bistra: ugovor `82.375,00 HRK`, isplata `82.375,00 EUR`). Pravilo D2 takve slučajeve (omjer ≈ 7,5345 ± 1,5 %) prikazuje kao informativne (`work/decisions.md`, 2026-11-03).

### 1.2 Polja standardnog EOJN izvoza (klasa A, stvarno zaglavlje iz `Grad Osijek / registar-ugovora.xlsx`) [VERIFICIRANO]

| Stupac | Primjer | Popunjenost (n = 2.425) | Napomena |
|---|---|---|---|
| Ugovor | `https://eojn.hr/contract-eo/730419` | 100 % | **trajni dokaz**, služi kao prirodni ključ |
| Postupak | `https://eojn.hr/tender-eo/63649` | visoka | poveznica na postupak |
| Na osnovu okvirnog sporazuma | URL ili prazno | – | služi za isključivanje okvirnih sporazuma iz pravila D |
| Naručitelj / OIB Naručitelja | `GRAD OSIJEK` / `30050049642` | 100 % | |
| Evid. Broj | `25-425` | | |
| Predmet nabave | tekst | 100 % | ulaz za AI |
| Vrsta predmeta | Robe / Usluge / Radovi | 100 % | |
| CPV | `64110000 - Poštanske usluge` | **100 %** | |
| Vrsta postupka | `Otvoreni postupak` | 100 % | |
| EU Financ. | DA/NE | | |
| Broj ugovora, Objava, Grupa br., Naziv grupe | | | |
| Naziv ugovaratelja / **OIB ugovaratelja** | `HP - Hrvatska pošta d.d.` / `87311810356` | **100 %** | ključ za spajanje |
| Datum sklapanja | datetime | 100 % | |
| Trajanje | `30 dana` | | slobodan tekst |
| Podugovaratelj/i, OIB podugovaratelja | | rijetko | |
| Iznos bez PDV-a, Iznos PDV-a, Iznos s PDV-om | `198400` | 100 % | **EUR** za ugovore od 2023. |
| Iznos izmjene bez PDV-a / s PDV-om | | **89/2.425 (3,7 %)** | ulaz za pravilo D |
| Ukupni iznos (sa izmjenama) bez PDV-a / s PDV-om | | 100 % | |
| Status ugovora | `Ugovoren` | | |
| Datum izvršenja | | 650/2.425 (27 %) | |
| Isplaćeni iznos | | **644/2.425 (27 %)** | ulaz za pravilo D |
| Obrazloženje iznosa plaćanja | tekst | 45/2.425 | ulaz za AI |
| Napomena | tekst | | |

### 1.3 Što registar ugovora zakonski mora sadržavati [VERIFICIRANO 2026-11-01, izvor: istraživački agent, tekstovi NN]

- **ZJN čl. 28** (NN 120/16, 114/22, **48/26**, konsolidirano na zakon.hr, na snazi od 16.05.2026.):
  - (2) naručitelj ažurno vodi registar ugovora;
  - **(5) registar i sve promjene objavljuje u EOJN RH**. Prije izmjene stajalo je "na internetskim stranicama";
  - (6) upisuju se predmeti nabave procijenjene vrijednosti **≥ 5.000 EUR bez PDV-a** (prije 2.650 EUR);
  - (7) upisuju se i izuzeća iz čl. 33./34., uz obrazloženje.
- **Novi Pravilnik o planu nabave, registru ugovora i pripremi postupka javne nabave (NN 86/2026)** stavlja izvan snage NN 101/17, 144/20 i 30/23. **Čl. 5 propisuje 31 obvezno polje**, među njima:
  - CPV;
  - vrsta postupka;
  - naziv i OIB ugovaratelja, podugovaratelja i subjekta na čiju se sposobnost oslanja;
  - datum sklapanja, rok, iznos bez PDV-a, PDV, ukupno;
  - EU financiranje;
  - datum izvršenja i ukupno isplaćeno;
  - **obrazloženje ako je isplaćeno više od ugovorenog**;
  - status izvršenja (uredno / naplaćeno jamstvo / kašnjenje / ugovorna kazna / raskid);
  - veza na stavku plana nabave;
  - razlozi raskida.
- **Čl. 6–7:** upis u roku od 15 dana; izmjena ugovora u roku od 8 dana; EOJN čuva podatke najmanje 10 godina.
- **Posljedica za nas:** zakonski je EOJN od 16.05.2026. **jedino mjesto** gdje registar mora biti objavljen. Općinske kopije na data.gov.hr su dobrovoljne i mogu prestati. To je jak argument za budući izravni EOJN API (vidi `ARCHITECTURE.md`, Interoperabilnost).
- **Usporedba zakona i prakse:** polja "broj ponuda" **nema** ni u zakonskom popisu ni u izvozima. "Veza na plan nabave" je zakonski obvezna, ali je u izvozima klase A ne vidimo. Plan → ugovor zato spajamo heuristički (CPV + predmet + godina).

### 1.4 Ograničenja
- Male općine (klase C/D) u registar upisuju i **ugovore s fizičkim osobama**: ugovore o djelu, najam stana, darovanje nekretnine (uzorak: Općina Kamanje, Velika Ludina). To ima izravne posljedice za **GDPR**, vidi `ARCHITECTURE.md` §9.
- Iznosi u kunama za ugovore prije 2023. (`"1.500,00 kn mjesečno"`), kao i iznosi tipa "mjesečno" i "okvirno", ne smiju se zbrajati bez normalizacije.
- CSV-ovi su u cp1250 ili UTF-8 s BOM-om, a ćelije sadrže prijelome redaka (npr. Grad Dugo Selo: OIB je unutar ćelije s adresom).

---

## 2. Nacionalni izvoz "Registar Ugovora – EOJN sustav" (data.gov.hr)

| Svojstvo | Vrijednost [VERIFICIRANO 2026-11-01] |
|---|---|
| Skup | https://data.gov.hr/dataset/registar-ugovora-eojn-sustav (izdavatelj: **Grad Zlatar**) |
| Resurs | JSON, 28.738.101 B, `datastore_active=false`. Download URL: `https://data.gov.hr/ckan/dataset/7fd9ab5a-5bee-4034-81fa-be6ffe337c6b/resource/fe4cd062-809a-4f7e-a803-4aaa44c893de/download/pegistar-ugovora-eojn-sustav_2026-4-11-13-40-17.json` (HTTP 200, 3,7 s) |
| Licenca / svježina | `open-license` / deklarirano `monthly`, a zadnja izmjena je **2026-05-14** ("do svibnja 2026."), dakle **5,5 mjeseci bez osvježavanja** |
| Redaka | **točno 20.000**; 1.337 naručitelja, 6.611 izvođača |
| Datumi | `ContractDate` 2024: 1.508, 2025: 6.807, 2026: 11.635; `InitialPublishTimestamp` od 2024-01-04 do 2026-05-11 |

**Polja** (popunjenost od 20.000):

| Polje | Popunjeno | Primjer |
|---|---|---|
| `Id` | 20.000 | `1054289` (**ne odgovara** `contract-eo/{id}` iz klase A) |
| `CAName`, `CAIdentificationNumber` | 20.000 | `GRAD ZAPREŠIĆ`, `92840587889` |
| `ContractorName`, `ContractorIdentificationNumber` | 20.000 | `URBANE TEHNIKE d.o.o.`, `52201457254` |
| `TenderName`, `Cpv` | 20.000 | `71220000 - Usluge projektiranja u arhitekturi` |
| `ProcedureTypeName` | 20.000 | Nabava izuzeta od primjene ZJN 9.878; Otvoreni postupak 8.892; Jednostavna nabava (razne) ~690; Pregovarački bez objave 213; … |
| `ContractDate` | 20.000 | |
| `InitialValue`, `InitialValueVAT` | 19.991 | |
| `TotalValue`, `TotalValueVat` | 19.991 | median 5.660 EUR, max 278,8 M EUR |
| `PayedAmount` | 7.684 | |
| `ContractTerminationInfo` | 249 | slobodni tekst, npr. "Isplaćen je veći iznos u odnosu na ugovorni temeljem stvarno izvršene usluge…" |
| `ContractEUFinanc` | 20.000 | |
| `FrameworkAgreementId` | 6.829 | |
| `AgreementTypeId`, `ContractTypeId`, `ContractStatusId`, `ExemptionLegalBaseId` | 20.000 / 10.767 | **šifrarnici nisu objavljeni** [NEVERIFICIRANO značenje] |
| `DurationFrom/To` | ~7.300 | |
| `SubcontractorIds/Names` | 327 | |

**Spoj s JPT-om po OIB-u naručitelja** [VERIFICIRANO]:
- 12.472/20.000 redaka i 976/1.337 naručitelja se spaja;
- 2.381 redak je od 257 JLS;
- **89 JLS ima samo 1 ugovor** u uzorku.

**Pristranost:** najviše redaka imaju KBC Zagreb 874, OB Pula 865 i OB Bjelovar 629.

**Upotreba u MVP-u:**
- usporedni benchmark kategorija na nacionalnoj razini, jasno označen kao "uzorak od 20.000 ugovora";
- skup za evaluaciju pravila D i F.

**Ne koristi se** za profile pojedinih općina.

---

## 3. EOJN RH (eojn.hr / eojn.nn.hr)

| Stavka | Nalaz |
|---|---|
| `https://eojn.hr/robots.txt` | HTTP 200: `User-agent: * / Allow: /$ / Disallow: /`. **Strugati se smije samo naslovnica.** [VERIFICIRANO] |
| `https://eojn.nn.hr/robots.txt` | 302 → `/Oglasnik/robots.txt`, HTTP 200: `Disallow: /administrator`, `Allow: /` [VERIFICIRANO] |
| Javni izvještaji | `eojn.nn.hr/.../RptRegistarUgovora.aspx`, `RptPlanoviNabave.aspx` (ASP.NET) [SAMO DOKUMENTIRANO] |
| **OCDS izvoz** | `https://eojn.nn.hr/SPIN/application/ipn/Oglasnik/PreuzimanjeUgovoraOCD.aspx`: "Otvoreni podaci registra ugovora (OCDS) … mjesečno se ažuriraju". Linkovi su ASP.NET `__doPostBack` (POST), pa ih nismo automatizirali. [VERIFICIRANO postojanje] |
| REST API | **nije pronađen** [NEVERIFICIRANO da ne postoji] |
| Uvjeti korištenja | link "Opći uvjeti poslovanja" vodi na dev.azure.com wiki, **nije pročitano** [NEVERIFICIRANO] |

**Odluka:** **ne strugamo** eojn.hr (robots.txt to zabranjuje). Ne automatiziramo ni ASP.NET postback na eojn.nn.hr, jer uvjeti nisu provjereni, a OCP registar nudi isti OCDS kao statičan bulk download. Poveznice `https://eojn.hr/contract-eo/{id}` u UI-ju prikazujemo **samo kao link za korisnika** (dokaz). Ne dohvaćamo ih automatski.

## 4. EOJN OCDS preko Open Contracting Data Registryja

| Svojstvo | Vrijednost |
|---|---|
| URL | https://data.open-contracting.org/en/publication/80 ("Croatia: Narodne Novine d.d.") |
| Download | `…/publication/80/download?name={full|2018..2026|undated}.{jsonl.gz|xlsx|csv.tar.gz}`: 2024 (5,0 MB) HTTP 200, 2025 (1,5 MB) HTTP 200, 2026 (18 KB) HTTP 200 [VERIFICIRANO] |
| Licenca | registar prikazuje "-" (nije navedena) [NEVERIFICIRANO], pa je izvorna licenca EOJN/NN otvoreno pitanje |
| Opseg | 302.465 ugovora (2018 – srpanj 2026). Releasei po godinama: 2024: **56.866**, 2025: **13.415**, 2026: **132**. Svi su tag `compiled` |
| Polja (2024, provjereno) | `buyer{id,name}`, `tender{title,description,value,procurementMethod,procurementMethodDetails,mainProcurementCategory}`, `awards[{date,value,suppliers}]`, `contracts[{value,status,dateSigned,description}]`, `parties[{identifier{id,legalName},address}]` |
| **Nedostaje** | **OIB** (`identifier.id` je interni EOJN ID; 0/113.732 stranaka ima 11-znamenkasti ID), `numberOfTenderers` (0), `bids` (0), CPV stavke, `amendments` (registar: 0) |
| Kvaliteta | adresa dobavljača je u uzorku identična adresi naručitelja (greška u izvozu); OCP napomena: "~11 % JSON datoteka nevaljano" (pregled iz 2021.) |

**Upotreba:**
- Should-have: povijesni kontekst za scenarij G (prosinac i izbori, više godina), spojen na JLS **po normaliziranom nazivu** (bez OIB-a, slabiji dokaz);
- dokaz interoperabilnosti (OCDS ulaz).

## 5. JPT – Popis brojčanih oznaka javnopravnih tijela (MPUDT)

**Ponovna provjera i ingest (2026-11-03) [VERIFICIRANO]:**
- `GET https://jpt.gov.hr/Backend_Prod/api/PopisOznakaNaDanasnjiDan` vraća HTTP 200, `application/json`, 4.527.638 B i **4.678 tijela**, svako s OIB-om (jedinstveni OIB-i);
- `https://jpt.gov.hr/robots.txt` vraća HTTP 500 (ASP.NET greška), pa `PoliteHttpClient` primjenjuje "sve dopušteno";
- `VrstaJPT` je dugi tekst, npr. `Jedinica lokalne i područne (regionalne) samouprave - Općina` (428), `… - Grad` (127), `… - Grad Zagreb` (1), `… - Županija` (20), te pravne osobe s javnim ovlastima ili javnom službom;
- **`Osnivaci` je popunjen kod 3.897 tijela** (npr. `PODUZETNIČKA ZONA KRIŽ D.O.O.` → `[{"OIB":"94115544733","Naziv":"OPĆINA KRIŽ"}]`). Komunalna društva su u registru, pa se može utvrditi je li dobavljač u vlasništvu naručitelja;
- `Zupanija` ponekad ima dvostruki razmak (`PRIMORSKO-GORANSKA  ŽUPANIJA`), koji se normalizira;
- na stvarnim ugovorima je **15 parova naručitelj–dobavljač u kojima je naručitelj osnivač dobavljača** (npr. Grad Varaždinske Toplice → Forum Toplice d.o.o., 16 ugovora; Općina Bistra → Komunalno gospodarstvo Bistra, 6). To je kontekst "informativno" za pravila C i F (`JptDirectory.isSupplierFoundedByBuyer`);
- spremljeno u `jpt_body` (svako tijelo ima `raw_record` i provenanciju `api`). Organizacije iz ugovora dobivaju `jpt_code`, `jpt_type`, `founders`, `county` i tip `jls`/`zupanija`. Obogaćene su 94 organizacije.

| Svojstvo | Vrijednost [VERIFICIRANO 2026-11-01] |
|---|---|
| Skup | https://data.gov.hr/dataset/popis-javnopravnih-tijela, licenca otvorena |
| API | `GET https://jpt.gov.hr/Backend_Prod/api/PopisOznakaNaDanasnjiDan` vraća HTTP 200, 4,53 MB, **4.678 tijela**. Pojedinačno: `/api/Oznaka/{oib}`, `/api/JavnoPravnoTijelo/{oznaka}` [SAMO DOKUMENTIRANO, provjereno 2026-10-22] |
| Polja | `Oznaka, DatumVazenjaOd/Do, VrstaJPT, OIB, Naziv, DatumOsnivanja, DatumUkidanja, Zupanija, GradOpcina, Naselje, PostanskiBroj, Ulica, KucniBroj, Osnivaci[{OIB,Naziv}], Email, PravniSlijednici, CijeJeTijeloPravniSlijednik` |
| `VrstaJPT` | Općina 428, Grad 127, Grad Zagreb 1, Županija 20, … |
| Primjer | `{"Oznaka":"2189-70","OIB":"82943640239","Naziv":"JAVNA USTANOVA ZA UPRAVLJANJE ZAŠTIĆENIM DIJELOVIMA PRIRODE…","Zupanija":"VIROVITIČKO-PODRAVSKA ŽUPANIJA","GradOpcina":"ČAĐAVICA"}` |

**Uloga:**
- autoritativni popis 556 JLS s OIB-om i županijom;
- tip naručitelja;
- lanac osnivača: ustanove i komunalna poduzeća u vlasništvu općine. To je **legitimno objašnjenje** za koncentraciju (scenarij C) ako je dobavljač u vlasništvu općine.

Ovo je **pravi API**, što podiže bodove za kriterij "Upotreba otvorenih podataka".

## 6. DZS Popis stanovništva 2021

**Ingest (2026-11-03) [VERIFICIRANO]:** resurs vraća HTTP 200 i 18.279.747 B. `podaci.dzs.hr/robots.txt` vraća 404. Čita se samo list `1.`, strujanjem (POI SAX, `DzsCensusReader`), jer učitavanje cijele radne knjige troši stotine MB. Spajanje s JPT-om po (županija, vrsta, naziv), normalizirano: **556/556 JLS spojeno, 0 nespojenih**, pa tablica `municipality_name_alias` nije potrebna. Usporedne skupine: P1 202, P2 193, P3 33, P4 70, P5 51, P6 6, P7 1. Gradske četvrti Zagreba i zbrojni retci županija se preskaču.

| Svojstvo | Vrijednost [VERIFICIRANO] |
|---|---|
| Skup | `statistika-u-nizu-popis-stanovnistva-kucanstava-i-stanova-2021` (DZS, `open-license`) |
| Resurs | https://podaci.dzs.hr/media/td3jvrbu/popis_2021-stanovnistvo_po_gradovima_opcinama.xlsx (HTTP 200, 18,3 MB) |
| Struktura | list `1.`, zaglavlje u retku 8: `Županija, Jedinica lokalne samouprave (Grad/Općina), County of, Local self-government unit, Grad/općina, Ukupno, …`. **555 redaka JLS** (127 Grad + 428 Općina); Grad Zagreb je u retku županije |
| Ključ | **nema OIB-a ni šifre**. Spaja se s JPT-om po (normalizirana županija, normalizirani naziv JLS) |
| Licenca | "Molimo korisnike da pri korištenju podataka navedu izvor." |

## 7. Sudski registar (sudreg-data.gov.hr)

| Svojstvo | Vrijednost |
|---|---|
| OpenAPI | `GET https://sudreg-data.gov.hr/api/javni/dokumentacija/open_api` vraća HTTP 200, `openapi 3.0.1`, "Sudski registar api – javni" **v3.0.4** [VERIFICIRANO] |
| Autentikacija | `securitySchemes: Oauth2 clientCredentials, tokenUrl https://sudreg-data.gov.hr/api/oauth/token` |
| Test bez ključa | `GET /api/javni/detalji_subjekta?tip_identifikatora=oib&identifikator=…` vraća **HTTP 401** `{"code":"Unauthorized"}` [VERIFICIRANO], dakle **NEDOSTUPNO bez registracije** |
| Relevantni endpointi | `/detalji_subjekta`, **`/subjekti` (bulk, `offset/limit`, `snapshot_id`)**, `/pretezite_djelatnosti`, `/pravni_oblici`, `/postupci` (stečaj/likvidacija), `/snapshots`, `/promjene` |
| Polja `subjekti` | `mbs, oib, mb, status, datum_osnivanja, datum_brisanja, glavna_djelatnost, stecajna_masa, likvidacijska_masa, …` |
| Licenca | [NEVERIFICIRANO], treba pročitati uvjete pri registraciji |
| Postojeći kod | `RestSudregClient` (OAuth2, token u memoriji) i `FixtureSudregClient` u repou `Desktop/datathon`, vidi `ARCHITECTURE.md` §2 |

**Praznina:** obrti i fizičke osobe **nisu** u Sudskom registru. Obrtni registar nema provjeren otvoren API [NEVERIFICIRANO].

## 8. TED (Tenders Electronic Daily)

| Svojstvo | Vrijednost [VERIFICIRANO, agent, 2026-11-01] |
|---|---|
| Endpoint | `POST https://api.ted.europa.eu/v3/notices/search`, **bez ključa**, HTTP 200 |
| Tijelo | `{"query":"buyer-country=HRV","fields":["publication-number","notice-title","buyer-name"],"limit":3}` daje `totalNoticeCount: 91185` |
| Licenca | "procurement notices … can be freely reused, for commercial or non-commercial purposes" (Odluka Komisije od 12.12.2011.) |
| Upotreba | Future: broj ponuda i iznosi za ugovore iznad EU pragova (TED obavijesti o dodjeli mogu sadržavati broj primljenih ponuda **[NEVERIFICIRANO za HR obavijesti]**) |

## 9. Ostali provjereni izvori

| Izvor | URL | Status | Upotreba |
|---|---|---|---|
| Registar birača – broj birača po adresi (MPUDT) | `…/bb_po_adresi_13_05_2026.csv` (79,6 MB, izmjena 2026-05-14) | samo metapodaci [VERIFICIRANO], sadržaj nije preuzet | rezervni proxy za stanovništvo |
| Popis županija, gradova i općina (MPUDT) | `popis-zupanija-gradova-i-opcina` (XLS na mpu.gov.hr) | metapodaci [VERIFICIRANO] | nije potreban, JPT je bolji |
| NKD 2025 (DZS KLASUS) | `web.dzs.hr/App/klasus/Download/GenericFillerFrm.aspx?id=16989477…` (JSON/XML) | metapodaci [VERIFICIRANO], sadržaj [NEVERIFICIRANO] | šifrarnik djelatnosti (AI: nesklad predmeta i djelatnosti) |
| Registar udruga (MPUDT) | `registar-udruga`, JSON, `daily` | metapodaci [VERIFICIRANO] | Future: ugovori o sufinanciranju udruga |
| Registar proračunskih i izvanproračunskih korisnika (MFin) | XLSX na data.gov.hr | metapodaci [VERIFICIRANO] | Future |
| e-Oglasna ploča sudova | `https://e-oglasna.pravosudje.hr/dokumenti/api` (HTTP 200, OpenAPI `e-oglasna_API_v1.yaml`; `/api/v1/court-notice/legal-person-bankruptcy` …; nema security scheme; dokumentiran HTTP 429) | [SAMO DOKUMENTIRANO] | Future: stečaj dobavljača |
| SRDI nekretnine | `https://srdi.gov.hr/api/nekretnine/export?format=JSON` vraća HTTP 200, 2,28 MB, 10.000 redaka (moguće ograničeno); polja `Obveznik, Naziv nekretnine, …` | [VERIFICIRANO] | nije relevantno za MVP |
| "Isplate iz proračuna" općina | 28 skupova na data.gov.hr, uglavnom HTML aplikacije (`*.otvorenaopcina.hr`, `kipos.hr`); obveza po Naputku NN 59/23 | metapodaci [VERIFICIRANO] | **Future**: stvarne isplate po dobavljaču (zatvara krug plan → ugovor → isplata) |
| Planovi nabave | 211 skupova (`q=plan nabave`), 59 JLS strojno čitljivo | [VERIFICIRANO] | Should: planirana vrijednost |
| Izbori | lokalni izbori 1. krug **18.05.2025.** (NN 69/2025) [VERIFICIRANO]; 2. krug 01.06.2025. [NEVERIFICIRANO primarnim izvorom] | | scenarij G |
| HNB tečaj | 7,53450 HRK/EUR (NN 85/2022, Uredba Vijeća (EU) 2022/1208) [VERIFICIRANO] | | konverzija |

## 9a. Granice JLS za kartu (#43, provjereno 2026-11-03)

| Kandidat | Što je provjereno | Odluka |
|---|---|---|
| DGU **Upravne jedinice INSPIRE** (`upravne-jedinice-inspire` na data.gov.hr, "Otvorena dozvola") | WFS `geoportal.dgu.hr/services/inspire/au/wfs` vraća `Service WFS is disabled`. ATOM `geoportal.dgu.hr/services/atom/au/xml` nudi samo GML ZIP od **219 MB** (ETRS89/LAEA), a polje `rights` kaže "ograničenje javnog pristupa prema članku 13 stavku 1 točki (e) INSPIRE Direktive". Postoji i WMS, ali to su slike, ne granice. | ne koristi se: licenca na portalu i u ATOM-u se razlikuju, a pretvorba GML-a od 219 MB ne stane u rok |
| **geoBoundaries HRV ADM2** (gbOpen, `9469f09`) | API `geoboundaries.org/api/current/gbOpen/HRV/ADM2/`: izvor "Open Street Map, geoBoundaries", licenca **CC BY-SA 2.0** (`licenseSource` openstreetmap.org/copyright), 560 jedinica, godina 2021, ažurirano 19.01.2023. Pojednostavljeni GeoJSON ima 2,2 MB. | **koristi se** |
| Overpass API (OSM) | `robots.txt` ima `Disallow: /api/` | ne koristi se (AGENTS.md: poštivanje robots.txt-a) |

- **Obrada:** `apps/web/scripts/build_jls_geojson.py`. Skripta pojednostavljuje oblike (Douglas–Peucker 0,002°, 4 decimale) i dodaje OIB iz naše tablice `municipality` (JPT + DZS) spajanjem po imenu. Pritom miče prefiks i dijakritike, a od dvojezičnih imena uzima hrvatski dio. Istoimene JLS razdvaja po vrsti (Novigrad, Otok, Sveta Nedelja), a Privlaku po geografskoj dužini. Rezultat je `apps/web/public/geo/jls.geojson`, 580 KB.
- **Ručne ispravke** su u `NAME_FIXES` u skripti:
  - tipfeleri u OSM-u: Muter-Kornati, Opicina Pirovac, Hvratska Dubica, Donji Kukuzari, Magdenovac, Veliki Pisanica;
  - "Grad Lobor" je zapravo Općina Lobor;
  - otoci Lošinj, Unije, Susak, Ilovik, Vele Srakane i Vele Orjule pripadaju Gradu Malom Lošinju.
- **Pokrivenost:** 542 od 556 JLS ima granicu.
  - **Nemaju granicu (14):** Cres, Krk, Baška, Dobrinj, Punat, Vrbnik, Ivanska, Lišane Ostrovičke, Ljubešćica, Rovišće, **Stankovci** (ima podatke), Tisno, Tribunj i Zrinski Topolovac. U izvoru su otoci Krk i Cres jedan oblik za više JLS, a kopnene JLS nedostaju. Karta ih navodi ispod legende, a tablica ih sadrži.
  - **13 oblika bez JLS** (Krk, Cres, otoci Kornata, Žut, Sit, Plavnik, Zeča) crtaju se iscrtkano kao "granice nisu dostupne".
- **Atribucija** je na karti: "© OpenStreetMap suradnici (ODbL), geoBoundaries (CC BY-SA 2.0)". Izvedena datoteka je pod istim uvjetima (share-alike).

## 10. Pragovi (moraju biti u konfiguraciji, s datumom važenja)

| Prag | Do 15.05.2026. | Od 16.05.2026. | Izvor |
|---|---|---|---|
| Jednostavna nabava (ZJN se ne primjenjuje), robe/usluge | 26.540 EUR | **50.000 EUR** | ZJN čl. 12 st. 1, NN 48/26 [VERIFICIRANO] |
| Jednostavna nabava, radovi | 66.360 EUR | **100.000 EUR** | isto |
| Obvezni upis u registar ugovora | 2.650 EUR | **5.000 EUR** | ZJN čl. 28 st. 6 [VERIFICIRANO]; točan datum početka primjene stavka [PRETPOSTAVKA: 16.05.2026.] |
| Modul jednostavne nabave u EOJN-u | – | > 15.000 EUR (od 01.09.2026.) | ZJN čl. 15 st. 5 |
| Izmjena de minimis | 10 % (robe/usluge) / 15 % (radovi) prvotne vrijednosti | isto | ZJN čl. 320 |
| Dodatni radovi/usluge/robe | ≤ 30 % prvotne vrijednosti | isto | ZJN čl. 316 st. 2 (prije izmjene vrijednost nije provjerena) |
| Nepredviđene okolnosti | ≤ 50 % prvotne vrijednosti | isto | ZJN čl. 317 |
| Jednostavna nabava 01.01.2017.–31.12.2022. (HRK) | robe/usluge < 200.000,00 kn (= 26.544,56 EUR po 7,53450), radovi < 500.000,00 kn (= 66.361,40 EUR) | | ZJN 2016 čl. 12 st. 1, NN 120/16; stupa na snagu 01.01.2017. (čl. 452) [VERIFICIRANO 2026-11-03, narodne-novine.nn.hr/clanci/sluzbeni/2016_12_120_2607.html] |
| Registar ugovora 2017.–2022. (HRK) | ≥ 20.000,00 kn | | ZJN 2016 čl. 28 st. 5, NN 120/16 [VERIFICIRANO 2026-11-03] |

**Pravilo:** prag se uvijek bira prema **datumu sklapanja ugovora**, nikad prema današnjem datumu.

## 11. Postojeći alati (konkurencija i diferencijacija) [VERIFICIRANO postojanje, agent 2026-11-01]

| Alat | Što radi | Razlika od JavniRadara |
|---|---|---|
| MFin "Upit o trošenju sredstava državnog proračuna" (drzavna-riznica.hr/trosenje_sredstava_DP/) | isplate iz državne riznice s OIB-om primatelja, dnevno | nema JLS, nema indikatora |
| IJF "Proračunska transparentnost" (ijf.hr/transparentnost/) | ocjena 0–5 objave proračunskih dokumenata za 576 jedinica (zadnji ciklus 2023.) | mjeri objavu dokumenata, ne nabavu |
| Imamo pravo znati (imamopravoznati.org) | zahtjevi za pristup informacijama | nije analitika, ali je dobar **komplement** ("zatraži dokument") |
| Integrity Watch Red Flags (redflags.integritywatch.eu) | red-flag analiza TED obavijesti | samo iznad EU pragova, ne vidi male općine; je li HR uključen [NEVERIFICIRANO] |
| opentender.eu (DIGIWHIST) | indikatori integriteta; HR OCDS do 12/2024 (preko OCP registra) | stranica vraća 403, sadržaj [NEVERIFICIRANO]; nema lokalnih registara ugovora ni objašnjenja na hrvatskom |
| GONG, TI Hrvatska | zagovaranje; TI ima stranicu o javnoj nabavi | alat s indikatorima nije pronađen [NEVERIFICIRANO] |

## 12. Kako ponoviti provjere

```bash
B="https://data.gov.hr/ckan/api/3/action"
curl -s -G "$B/package_search" --data-urlencode "q=registar ugovora" --data-urlencode "rows=1000"   # pokrivenost
curl -s -G "$B/package_show" --data-urlencode "id=registar-ugovora-eojn-sustav"                       # nacionalni izvoz
curl -s "https://jpt.gov.hr/Backend_Prod/api/PopisOznakaNaDanasnjiDan" | head -c 500                  # JPT
curl -s -o /dev/null -w "%{http_code}" "https://sudreg-data.gov.hr/api/javni/detalji_subjekta?tip_identifikatora=oib&identifikator=00000000001"  # očekivano 401
curl -s -o /dev/null -w "%{http_code}" "https://data.open-contracting.org/en/publication/80/download?name=2024.jsonl.gz"
curl -s -X POST https://api.ted.europa.eu/v3/notices/search -H "Content-Type: application/json" \
  -d '{"query":"buyer-country=HRV","fields":["publication-number"],"limit":1}'
```
Napomena: CKAN upiti s dijakriticima (`š`, `ž`) povremeno vraćaju prazan odgovor ili 502. Treba koristiti `--retry 4 --retry-all-errors` ili inačice bez dijakritika.

## 13. Popis skupova za predaju (obvezni dio dokumentacije)

| Skup | Izdavatelj | Poveznica |
|---|---|---|
| Registri ugovora JLS (klase A i A2, 38 naručitelja; točan popis generira ingest u `/api/sources`) | pojedine JLS | https://data.gov.hr (CKAN `q=registar ugovora`) |
| Registar Ugovora – EOJN sustav | Grad Zlatar | https://data.gov.hr/dataset/registar-ugovora-eojn-sustav |
| Popis javnopravnih tijela | MPUDT | https://data.gov.hr/dataset/popis-javnopravnih-tijela |
| Popis 2021 – stanovništvo po gradovima/općinama | DZS | https://data.gov.hr (skup `statistika-u-nizu-popis-stanovnistva-kucanstava-i-stanova-2021`) |
| EOJN OCDS | Narodne novine d.d. / MPGI (EOJN) | https://data.open-contracting.org/en/publication/80 |
| Sudski registar (ako stigne ključ) | Ministarstvo pravosuđa | https://sudreg-data.gov.hr |
| ZJN, Pravilnik NN 86/2026 (pravila, ne podaci) | NN | https://narodne-novine.nn.hr |
| Granice JLS (karta), izvedeno iz OpenStreetMapa | geoBoundaries / OSM suradnici | https://www.geoboundaries.org (HRV ADM2, gbOpen) |
